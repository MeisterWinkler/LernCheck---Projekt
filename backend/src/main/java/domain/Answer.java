package domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "answers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attempt_id", "template_question_id"})
)
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    private Attempt attempt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "template_question_id")
    private TemplateQuestion templateQuestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "chosen_option", nullable = false, length = 1)
    private AnswerOption chosenOption;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private Instant answeredAt = Instant.now();

    protected Answer() {}

    public Answer(Attempt attempt, TemplateQuestion templateQuestion, AnswerOption chosenOption, boolean correct) {
        this.attempt = attempt;
        this.templateQuestion = templateQuestion;
        this.chosenOption = chosenOption;
        this.correct = correct;
    }

    public Long getId() { return id; }
    public Attempt getAttempt() { return attempt; }
    public TemplateQuestion getTemplateQuestion() { return templateQuestion; }
    public AnswerOption getChosenOption() { return chosenOption; }
    public boolean isCorrect() { return correct; }
    public Instant getAnsweredAt() { return answeredAt; }

    public void setChosenOption(AnswerOption chosenOption) { this.chosenOption = chosenOption; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}