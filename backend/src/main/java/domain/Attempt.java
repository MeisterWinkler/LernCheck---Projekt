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
    private Quiz quiz;

    @Column(nullable = false, length = 64, unique = true)
    private String anonToken;

    @Column(nullable = false)
    private Instant startedAt = Instant.now();

    @Column(nullable = false)
    private boolean finished = false;

    protected Attempt() {}

    public Attempt(Quiz quiz, String anonToken) {
        this.quiz = quiz;
        this.anonToken = anonToken;
    }

    public Long getId() { return id; }
    public Quiz getQuiz() { return quiz; }
    public String getAnonToken() { return anonToken; }
    public Instant getStartedAt() { return startedAt; }
    public boolean isFinished() { return finished; }

    public void setFinished(boolean finished) { this.finished = finished; }
}