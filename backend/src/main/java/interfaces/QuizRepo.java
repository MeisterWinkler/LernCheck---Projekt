// Summary: Repository für Quizze (PIN-Join, Filter per Klasse).
package interfaces;

import domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepo extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByJoinPin(String joinPin);
    List<Quiz> findBySchoolClassId(Long classId);
}