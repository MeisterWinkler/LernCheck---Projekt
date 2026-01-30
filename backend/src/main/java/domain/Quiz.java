// Summary: Quiz-Entity inkl. Join-PIN, Aktiv-Status und Dauer (Timer).
package domain;

import jakarta.persistence.*;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "school_class_id")
    private SchoolClass schoolClass;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "join_pin", nullable = false, length = 12, unique = true)
    private String joinPin;

    @Column(nullable = false)
    private boolean active = false;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes = 10;

    protected Quiz() {}

    public Quiz(SchoolClass schoolClass, String title, String joinPin, int durationMinutes) {
        this.schoolClass = schoolClass;
        this.title = title;
        this.joinPin = joinPin;
        this.durationMinutes = durationMinutes;
        this.active = false;
    }

    public Long getId() { return id; }
    public SchoolClass getSchoolClass() { return schoolClass; }
    public String getTitle() { return title; }
    public String getJoinPin() { return joinPin; }
    public boolean isActive() { return active; }
    public int getDurationMinutes() { return durationMinutes; }

    public void setActive(boolean active) { this.active = active; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setJoinPin(String joinPin) { this.joinPin = joinPin; }
}