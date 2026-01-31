package interfaces;

import domain.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttemptRepo extends JpaRepository<Attempt, Long> {
    Optional<Attempt> findByAnonToken(String anonToken);
    List<Attempt> findByRunId(Long runId);
}