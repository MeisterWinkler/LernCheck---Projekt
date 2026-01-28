// Summary: Adaptive Wiederholungsfragen: liefert die "schwierigsten" Fragen eines Quiz.
package api;

import domain.Question;
import interfaces.QuizRepo;
import service.AdaptiveService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/adaptive")
public class AdaptiveController {

    private final AdaptiveService adaptiveService;
    private final QuizRepo quizRepo;

    public AdaptiveController(AdaptiveService adaptiveService, QuizRepo quizRepo) {
        this.adaptiveService = adaptiveService;
        this.quizRepo = quizRepo;
    }

    public record PublicQuestion(long id, String text, String optionA, String optionB, String optionC, String optionD) {}

    @GetMapping("/{quizId}")
    public List<PublicQuestion> repeat(@PathVariable long quizId, @RequestParam(defaultValue = "5") int n) {
        quizRepo.findById(quizId).orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        List<Question> picked = adaptiveService.pickRepeatQuestions(quizId, n);

        return picked.stream()
                .map(q -> new PublicQuestion(q.getId(), q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD()))
                .toList();
    }
}