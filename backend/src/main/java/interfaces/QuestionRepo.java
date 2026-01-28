// Summary: Repository für Fragen eines Quiz.
package interfaces;

import domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepo extends JpaRepository<Question, Long> {
    List<Question> findByQuizId(Long quizId);
}