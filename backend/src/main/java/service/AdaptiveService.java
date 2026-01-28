// Summary: Adaptive Logik (MVP): bevorzugt Fragen mit hoher Fehlerquote als Wiederholungsfragen.
package service;

import domain.Question;
import interfaces.AnswerRepo;
import interfaces.QuestionRepo;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdaptiveService {

    private final QuestionRepo questionRepo;
    private final AnswerRepo answerRepo;

    public AdaptiveService(QuestionRepo questionRepo, AnswerRepo answerRepo) {
        this.questionRepo = questionRepo;
        this.answerRepo = answerRepo;
    }

    public List<Question> pickRepeatQuestions(long quizId, int n) {
        List<Question> questions = questionRepo.findByQuizId(quizId);
        Map<Long, Double> wrongRate = computeWrongRate(questions);

        return questions.stream()
                .sorted((a, b) -> Double.compare(
                        wrongRate.getOrDefault(b.getId(), 0.0),
                        wrongRate.getOrDefault(a.getId(), 0.0)
                ))
                .limit(Math.max(0, n))
                .collect(Collectors.toList());
    }

    private Map<Long, Double> computeWrongRate(List<Question> questions) {
        Map<Long, Double> map = new HashMap<>();
        for (Question q : questions) {
            var answers = answerRepo.findByQuestionId(q.getId());
            if (answers.isEmpty()) {
                map.put(q.getId(), 0.0);
                continue;
            }
            long wrong = answers.stream().filter(a -> !a.isCorrect()).count();
            map.put(q.getId(), wrong / (double) answers.size());
        }
        return map;
    }
}