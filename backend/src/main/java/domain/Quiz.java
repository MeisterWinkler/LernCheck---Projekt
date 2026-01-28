package domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SchoolClass schoolClass;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 12, unique = true)
    private String joinPin;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Quiz() {}

    public Quiz(SchoolClass schoolClass, String title, String joinPin) {
        this.schoolClass = schoolClass;
        this.title = title;
        this.joinPin = joinPin;
    }

    public Long getId() { return id; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public String getTitle() { return title; }
    public String getJoinPin() { return joinPin; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }

    public void setActive(boolean active) { this.active = active; }
}