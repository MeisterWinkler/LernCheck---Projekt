// Summary: Antwort je Frage innerhalb eines Attempts; Basis für Statistik & adaptive Logik.
package domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "answers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "question_id"})
)
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Attempt attempt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Question question;

    @Column(nullable = false, length = 1)
    private String chosenOption;

    @Column(nullable = false)
    private boolean correct;

    protected Answer() {}

    public Answer(Attempt attempt, Question question, String chosenOption, boolean correct) {
        this.attempt = attempt;
        this.question = question;
        this.chosenOption = chosenOption;
        this.correct = correct;
    }

    public Long getId() { return id; }
    public Attempt getAttempt() { return attempt; }
    public Question getQuestion() { return question; }
    public String getChosenOption() { return chosenOption; }
    public boolean isCorrect() { return correct; }
}