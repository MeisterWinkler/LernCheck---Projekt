package domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "template_questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"template_id", "pos"})
)
public class TemplateQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private QuizTemplate template;

    @Column(nullable = false)
    private int pos;

    @Column(nullable = false, length = 800)
    private String text;

    @Column(name = "option_a", nullable = false, length = 300)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 300)
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 300)
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 300)
    private String optionD;

    @Enumerated(EnumType.STRING)
    @Column(name = "correct_option", nullable = false, length = 1)
    private AnswerOption correctOption;

    protected TemplateQuestion() {}

    public TemplateQuestion(QuizTemplate template, int pos, String text,
                            String optionA, String optionB, String optionC, String optionD,
                            AnswerOption correctOption) {
        this.template = template;
        this.pos = pos;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
    }

    public Long getId() { return id; }
    public QuizTemplate getTemplate() { return template; }
    public int getPos() { return pos; }
    public String getText() { return text; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public AnswerOption getCorrectOption() { return correctOption; }
}