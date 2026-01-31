package interfaces;

import domain.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepo extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUsername(String username);
}