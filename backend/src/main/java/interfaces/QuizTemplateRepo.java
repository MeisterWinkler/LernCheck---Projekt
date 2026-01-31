package interfaces;

import domain.QuizTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizTemplateRepo extends JpaRepository<QuizTemplate, Long> {
    List<QuizTemplate> findByOwnerId(Long ownerId);
}