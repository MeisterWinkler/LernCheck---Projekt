package interfaces;

import domain.QuizRun;
import domain.RunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRunRepo extends JpaRepository<QuizRun, Long> {
    List<QuizRun> findBySchoolClassIdOrderByIdDesc(Long classId);
    Optional<QuizRun> findByJoinPin(String joinPin);
    List<QuizRun> findByTemplateOwnerIdAndSchoolClassId(Long ownerId, Long classId);
    List<QuizRun> findByStatus(RunStatus status);
}