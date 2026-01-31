package domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "quiz_runs")
public class QuizRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "school_class_id")
    private SchoolClass schoolClass;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private QuizTemplate template;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes = 10;

    @Column(name = "join_pin", length = 12, unique = true)
    private String joinPin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RunStatus status = RunStatus.DRAFT;

    private Instant startedAt;
    private Instant endsAt;
    private Instant finishedAt;

    protected QuizRun() {}

    public QuizRun(SchoolClass schoolClass, QuizTemplate template, int durationMinutes) {
        this.schoolClass = schoolClass;
        this.template = template;
        this.durationMinutes = durationMinutes;
        this.status = RunStatus.DRAFT;
    }

    public Long getId() { return id; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public QuizTemplate getTemplate() { return template; }
    public int getDurationMinutes() { return durationMinutes; }
    public String getJoinPin() { return joinPin; }
    public RunStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndsAt() { return endsAt; }
    public Instant getFinishedAt() { return finishedAt; }

    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setJoinPin(String joinPin) { this.joinPin = joinPin; }
    public void setStatus(RunStatus status) { this.status = status; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public void setEndsAt(Instant endsAt) { this.endsAt = endsAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
}