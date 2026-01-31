package api;

import domain.*;
import interfaces.*;
import service.PinTokenService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final QuizRunRepo runRepo;
    private final AttemptRepo attemptRepo;
    private final TemplateQuestionRepo tqRepo;
    private final AnswerRepo answerRepo;
    private final FeedbackRepo feedbackRepo;
    private final PinTokenService pinTokenService;

    public StudentController(
            QuizRunRepo runRepo,
            AttemptRepo attemptRepo,
            TemplateQuestionRepo tqRepo,
            AnswerRepo answerRepo,
            FeedbackRepo feedbackRepo,
            PinTokenService pinTokenService
    ) {
        this.runRepo = runRepo;
        this.attemptRepo = attemptRepo;
        this.tqRepo = tqRepo;
        this.answerRepo = answerRepo;
        this.feedbackRepo = feedbackRepo;
        this.pinTokenService = pinTokenService;
    }

    public record JoinReq(@NotBlank String pin) {}
    public record JoinRes(String anonToken, long runId, String title, int durationMinutes, Instant endsAt) {}

    public record PublicQuestion(long id, int pos, String text, String optionA, String optionB, String optionC, String optionD) {}
    public record QuizRes(long runId, String title, int durationMinutes, Instant endsAt, List<PublicQuestion> questions) {}

    public record SubmitAnswer(long templateQuestionId, @NotBlank String chosenOption) {}
    public record SubmitReq(@NotBlank String anonToken, List<SubmitAnswer> answers, String feedback, boolean finalizeAttempt) {}
    public record SubmitRes(int totalQuestions, int answered, int correct) {}

    @PostMapping("/join")
    public JoinRes join(@Valid @RequestBody JoinReq req) {
        QuizRun run = runRepo.findByJoinPin(req.pin().trim()).orElseThrow(() -> new IllegalArgumentException("Invalid PIN"));

        if (run.getStatus() != RunStatus.ACTIVE) throw new IllegalStateException("Quiz not active");
        if (run.getEndsAt() != null && Instant.now().isAfter(run.getEndsAt())) throw new IllegalStateException("Quiz expired");

        String token = pinTokenService.newAnonToken(32);
        attemptRepo.save(new Attempt(run, token));

        return new JoinRes(token, run.getId(), run.getTemplate().getTitle(), run.getDurationMinutes(), run.getEndsAt());
    }

    @GetMapping("/run/{runId}")
    public QuizRes getRunQuiz(@PathVariable long runId) {
        QuizRun run = runRepo.findById(runId).orElseThrow();
        if (run.getStatus() != RunStatus.ACTIVE) throw new IllegalStateException("Quiz not active");

        List<TemplateQuestion> qs = tqRepo.findByTemplateIdOrderByPosAsc(run.getTemplate().getId());
        List<PublicQuestion> out = new ArrayList<>();
        for (TemplateQuestion q : qs) {
            out.add(new PublicQuestion(q.getId(), q.getPos(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()));
        }
        return new QuizRes(run.getId(), run.getTemplate().getTitle(), run.getDurationMinutes(), run.getEndsAt(), out);
    }

    @PostMapping("/submit")
    public SubmitRes submit(@Valid @RequestBody SubmitReq req) {
        Attempt attempt = attemptRepo.findByAnonToken(req.anonToken().trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (attempt.isFinished()) throw new IllegalStateException("Attempt already finished");

        QuizRun run = attempt.getRun();
        List<TemplateQuestion> qs = tqRepo.findByTemplateIdOrderByPosAsc(run.getTemplate().getId());
        Map<Long, TemplateQuestion> byId = new HashMap<>();
        for (TemplateQuestion q : qs) byId.put(q.getId(), q);

        int correct = 0;
        int answered = 0;

        if (req.answers() != null) {
            for (SubmitAnswer a : req.answers()) {
                TemplateQuestion q = byId.get(a.templateQuestionId());
                if (q == null) continue; // ignorieren

                AnswerOption chosen;
                try {
                    chosen = AnswerOption.valueOf(a.chosenOption().trim().toUpperCase());
                } catch (Exception e) {
                    continue; // ungültig ignorieren
                }

                boolean isCorrect = (chosen == q.getCorrectOption());

                // UPSERT per unique(attempt, question)
                var existing = answerRepo.findByAttemptIdAndTemplateQuestionId(attempt.getId(), q.getId());
                if (existing.isPresent()) {
                    var ans = existing.get();
                    ans.setChosenOption(chosen);
                    ans.setCorrect(isCorrect);
                    answerRepo.save(ans);
                } else {
                    answerRepo.save(new domain.Answer(attempt, q, chosen, isCorrect));
                }

                answered++;
                if (isCorrect) correct++;
            }
        }

        // optional feedback (nur wenn finalize)
        if (req.finalizeAttempt()) {
            attempt.setFinished(true);
            attempt.setSubmittedAt(Instant.now());
            attemptRepo.save(attempt);

            String fb = req.feedback();
            if (fb != null && !fb.isBlank()) {
                feedbackRepo.save(new Feedback(attempt, fb.trim()));
            }
        }

        return new SubmitRes(qs.size(), answered, correct);
    }
}