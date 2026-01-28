// Summary: Repository für Antworten (Auswertung, Heatmap, adaptive Logik).
package interfaces;

import domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepo extends JpaRepository<Answer, Long> {
    List<Answer> findByAttemptId(Long attemptId);
    List<Answer> findByQuestionId(Long questionId);
}