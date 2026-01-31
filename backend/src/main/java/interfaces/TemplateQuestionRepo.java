package interfaces;

import domain.TemplateQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateQuestionRepo extends JpaRepository<TemplateQuestion, Long> {
    List<TemplateQuestion> findByTemplateIdOrderByPosAsc(Long templateId);
}