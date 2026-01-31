package domain;

import jakarta.persistence.*;

@Entity
@Table(name = "school_classes")
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Teacher owner;

    @Column(nullable = false, length = 80)
    private String name;

    protected SchoolClass() {}

    public SchoolClass(Teacher owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public Long getId() { return id; }
    public Teacher getOwner() { return owner; }
    public String getName() { return name; }
}