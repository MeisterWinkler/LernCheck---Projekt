// Summary: Schüler-Endpunkte: PIN-Join, Quiz laden, Antworten abgeben, Ergebnis.
package api;

import domain.*;
import interfaces.*;
import service.PinTokenService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final QuizRepo quizRepo;
    private final QuestionRepo questionRepo;
    private final AttemptRepo attemptRepo;
    private final AnswerRepo answerRepo;
    private final PinTokenService pinTokenService;

    public StudentController(QuizRepo quizRepo, QuestionRepo questionRepo, AttemptRepo attemptRepo,
                             AnswerRepo answerRepo, PinTokenService pinTokenService) {
        this.quizRepo = quizRepo;
        this.questionRepo = questionRepo;
        this.attemptRepo = attemptRepo;
        this.answerRepo = answerRepo;
        this.pinTokenService = pinTokenService;
    }

    public record JoinReq(@NotBlank String pin) {}
    public record JoinRes(String anonToken, long quizId, String quizTitle, int durationMinutes) {}

    public record PublicQuestion(long id, String text, String optionA, String optionB, String optionC, String optionD) {}
    public record QuizRes(long quizId, String title, int durationMinutes, List<PublicQuestion> questions) {}

    public record SubmitAnswer(long questionId, @NotBlank String chosenOption) {}
    public record SubmitReq(@NotBlank String anonToken, List<SubmitAnswer> answers) {}

    public record SubmitRes(int total, int correct, List<Long> wrongQuestionIds) {}

    @PostMapping("/join")
    public JoinRes join(@Valid @RequestBody JoinReq req) {
        Quiz quiz = quizRepo.findByJoinPin(req.pin().trim()).orElseThrow(() -> new IllegalArgumentException("Invalid PIN"));
        if (!quiz.isActive()) throw new IllegalStateException("Quiz not active");

        String token = pinTokenService.newAnonToken(32);
        attemptRepo.save(new Attempt(quiz, token));
        return new JoinRes(token, quiz.getId(), quiz.getTitle(), quiz.getDurationMinutes());
    }

    @GetMapping("/quiz/{quizId}")
    public QuizRes getQuiz(@PathVariable long quizId) {
        Quiz quiz = quizRepo.findById(quizId).orElseThrow();
        List<PublicQuestion> qs = questionRepo.findByQuizId(quizId).stream()
                .map(q -> new PublicQuestion(q.getId(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()))
                .toList();
        return new QuizRes(quiz.getId(), quiz.getTitle(), quiz.getDurationMinutes(), qs);
    }

    @PostMapping("/submit")
    public SubmitRes submit(@Valid @RequestBody SubmitReq req) {
        Attempt attempt = attemptRepo.findByAnonToken(req.anonToken()).orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (attempt.isFinished()) throw new IllegalStateException("Attempt already finished");

        List<Question> questions = questionRepo.findByQuizId(attempt.getQuiz().getId());

        int correctCount = 0;
        var wrongIds = new java.util.ArrayList<Long>();

        for (SubmitAnswer a : req.answers()) {
            Question q = questions.stream().filter(x -> x.getId().equals(a.questionId())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Question not in quiz"));

            String chosen = a.chosenOption().trim().toUpperCase();
            boolean correct = chosen.equals(q.getCorrectOption());
            if (correct) correctCount++; else wrongIds.add(q.getId());

            answerRepo.save(new Answer(attempt, q, chosen, correct));
        }

        attempt.setFinished(true);
        attemptRepo.save(attempt);

        return new SubmitRes(questions.size(), correctCount, wrongIds);
    }
}