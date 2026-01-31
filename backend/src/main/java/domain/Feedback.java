package domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    private Attempt attempt;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Feedback() {}

    public Feedback(Attempt attempt, String comment) {
        this.attempt = attempt;
        this.comment = comment;
    }

    public Long getId() { return id; }
    public Attempt getAttempt() { return attempt; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}