package domain;

import jakarta.persistence.*;

@Entity
@Table(name = "questions")
public class Question {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Quiz quiz;

    @Column(nullable = false, length = 800)
    private String text;

    @Column(nullable = false, length = 300)
    private String optionA;

    @Column(nullable = false, length = 300)
    private String optionB;

    @Column(nullable = false, length = 300)
    private String optionC;

    @Column(nullable = false, length = 300)
    private String optionD;

    @Column(nullable = false, length = 1)
    private String correctOption; // "A","B","C","D"

    protected Question() {}

    public Question(Quiz quiz, String text, String a, String b, String c, String d, String correctOption) {
        this.quiz = quiz;
        this.text = text;
        this.optionA = a;
        this.optionB = b;
        this.optionC = c;
        this.optionD = d;
        this.correctOption = correctOption;
    }

    public Long getId() { return id; }
    public Quiz getQuiz() { return quiz; }
    public String getText() { return text; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectOption() { return correctOption; }
}