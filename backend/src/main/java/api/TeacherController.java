// Summary: Lehrer-Endpunkte: Klassen/Quiz, Fragen-Upload, Start/Stop, PIN refresh.
package api;

import domain.*;
import interfaces.*;
import service.PinTokenService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final ClassRepo classRepo;
    private final QuizRepo quizRepo;
    private final QuestionRepo questionRepo;
    private final TeacherRepo teacherRepo;
    private final PinTokenService pinTokenService;

    public TeacherController(ClassRepo classRepo, QuizRepo quizRepo, QuestionRepo questionRepo,
                             TeacherRepo teacherRepo, PinTokenService pinTokenService) {
        this.classRepo = classRepo;
        this.quizRepo = quizRepo;
        this.questionRepo = questionRepo;
        this.teacherRepo = teacherRepo;
        this.pinTokenService = pinTokenService;
    }

    private long teacherId(Authentication auth) { return (long) auth.getPrincipal(); }

    public record CreateClassReq(@NotBlank String name) {}

    public record CreateQuizReq(
            @NotBlank String title,
            @Min(1) @Max(180) int durationMinutes
    ) {}

    public record QuizInfoRes(long id, String title, String joinPin, boolean active, int durationMinutes) {}

    public record UploadQuestionReq(
            @NotBlank String text,
            @NotBlank String optionA,
            @NotBlank String optionB,
            @NotBlank String optionC,
            @NotBlank String optionD,
            @NotBlank String correctOption
    ) {}

    @GetMapping("/classes")
    public List<SchoolClass> listClasses(Authentication auth) {
        return classRepo.findByOwnerId(teacherId(auth));
    }

    @PostMapping("/classes")
    public SchoolClass createClass(Authentication auth, @Valid @RequestBody CreateClassReq req) {
        Teacher t = teacherRepo.findById(teacherId(auth)).orElseThrow();
        return classRepo.save(new SchoolClass(t, req.name()));
    }

    @GetMapping("/classes/{classId}/quizzes")
    public List<Quiz> listQuizzes(@PathVariable long classId) {
        return quizRepo.findBySchoolClassId(classId);
    }

    @PostMapping("/classes/{classId}/quizzes")
    public Quiz createQuiz(@PathVariable long classId, @Valid @RequestBody CreateQuizReq req) {
        SchoolClass sc = classRepo.findById(classId).orElseThrow();
        String pin = pinTokenService.newPin(6);
        return quizRepo.save(new Quiz(sc, req.title(), pin, req.durationMinutes()));
    }

    @GetMapping("/quizzes/{quizId}")
    public QuizInfoRes quizInfo(@PathVariable long quizId) {
        Quiz q = quizRepo.findById(quizId).orElseThrow();
        return new QuizInfoRes(q.getId(), q.getTitle(), q.getJoinPin(), q.isActive(), q.getDurationMinutes());
    }

    @PutMapping("/quizzes/{quizId}/active")
    public QuizInfoRes setActive(@PathVariable long quizId, @RequestParam boolean value) {
        Quiz q = quizRepo.findById(quizId).orElseThrow();
        q.setActive(value);
        quizRepo.save(q);
        return new QuizInfoRes(q.getId(), q.getTitle(), q.getJoinPin(), q.isActive(), q.getDurationMinutes());
    }

    @PutMapping("/quizzes/{quizId}/pin")
    public QuizInfoRes regeneratePin(@PathVariable long quizId) {
        Quiz q = quizRepo.findById(quizId).orElseThrow();
        q.setJoinPin(pinTokenService.newPin(6));
        quizRepo.save(q);
        return new QuizInfoRes(q.getId(), q.getTitle(), q.getJoinPin(), q.isActive(), q.getDurationMinutes());
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public List<Question> uploadQuestions(@PathVariable long quizId, @Valid @RequestBody List<UploadQuestionReq> questions) {
        Quiz quiz = quizRepo.findById(quizId).orElseThrow();
        return questionRepo.saveAll(
                questions.stream()
                        .map(q -> new Question(
                                quiz, q.text(), q.optionA(), q.optionB(), q.optionC(), q.optionD(),
                                q.correctOption().trim().toUpperCase()
                        ))
                        .toList()
        );
    }
}