package domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "attempts")
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private QuizRun run;

    @Column(name = "anon_token", nullable = false, length = 64, unique = true)
    private String anonToken;

    @Column(nullable = false)
    private Instant startedAt = Instant.now();

    private Instant submittedAt;

    @Column(nullable = false)
    private boolean finished = false;

    protected Attempt() {}

    public Attempt(QuizRun run, String anonToken) {
        this.run = run;
        this.anonToken = anonToken;
    }

    public Long getId() { return id; }
    public QuizRun getRun() { return run; }
    public String getAnonToken() { return anonToken; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public boolean isFinished() { return finished; }

    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public void setFinished(boolean finished) { this.finished = finished; }
}