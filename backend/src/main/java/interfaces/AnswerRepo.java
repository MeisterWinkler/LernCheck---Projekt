package interfaces;

import domain.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepo extends JpaRepository<Answer, Long> {
    List<Answer> findByAttemptId(Long attemptId);
    List<Answer> findByAttemptRunId(Long runId);
    Optional<Answer> findByAttemptIdAndTemplateQuestionId(Long attemptId, Long templateQuestionId);
}