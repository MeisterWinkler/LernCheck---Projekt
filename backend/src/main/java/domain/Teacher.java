// Summary: Lehrkraft-Account (Login). Keine Schülerdaten im System.
package domain;

import jakarta.persistence.*;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    protected Teacher() {}

    public Teacher(String email, String displayName, String passwordHash) {
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
}