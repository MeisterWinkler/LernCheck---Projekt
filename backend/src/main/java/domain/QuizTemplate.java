package domain;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_templates")
public class QuizTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Teacher owner;

    @Column(nullable = false, length = 120)
    private String title;

    protected QuizTemplate() {}

    public QuizTemplate(Teacher owner, String title) {
        this.owner = owner;
        this.title = title;
    }

    public Long getId() { return id; }
    public Teacher getOwner() { return owner; }
    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }
}