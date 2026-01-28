// Summary: Repository für Klassen/Kurse einer Lehrkraft.
package interfaces;

import domain.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassRepo extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findByOwnerId(Long ownerId);
}