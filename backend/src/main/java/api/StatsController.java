// Summary: Heatmap/Statistik: Fehlerquote pro Frage.
package api;

import interfaces.AnswerRepo;
import interfaces.QuestionRepo;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/stats")
public class StatsController {

    private final QuestionRepo questionRepo;
    private final AnswerRepo answerRepo;

    public StatsController(QuestionRepo questionRepo, AnswerRepo answerRepo) {
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
    }

    public record HeatmapPoint(long questionId, double wrongRate, long totalAnswers) {}
    public record HeatmapRes(long quizId, Map<Long, HeatmapPoint> byQuestionId) {}

    @GetMapping("/heatmap/{quizId}")
    public HeatmapRes heatmap(@PathVariable long quizId) {
        var questions = questionRepo.findByQuizId(quizId);
        Map<Long, HeatmapPoint> map = new HashMap<>();

        for (var q : questions) {
            var answers = answerRepo.findByQuestionId(q.getId());
            long total = answers.size();
            long wrong = answers.stream().filter(a -> !a.isCorrect()).count();
            double wrongRate = total == 0 ? 0.0 : (wrong / (double) total);
            map.put(q.getId(), new HeatmapPoint(q.getId(), wrongRate, total));
        }
        return new HeatmapRes(quizId, map);
    }
}