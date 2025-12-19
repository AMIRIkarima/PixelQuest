package com.pixelquest.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Party {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double score;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "start_x")),
            @AttributeOverride(name = "y", column = @Column(name = "start_y"))
    })
    private Point startPoint;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "target_x")),
            @AttributeOverride(name = "y", column = @Column(name = "target_y"))
    })
    private Point targetPoint;

    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL)
    private List<PointSample> samples;

    public Party() {}

    //getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    public Player getPlayer() {
        return player;
    }
    public void setPlayer(Player player) {
        this.player = player;
    }
    public Point getStartPoint() {
        return startPoint;
    }
    public void setStartPoint(Point startPoint) {
        this.startPoint = startPoint;
    }
    public Point getTargetPoint() {
        return targetPoint;
    }
    public void setTargetPoint(Point targetPoint) {
        this.targetPoint = targetPoint;
    }
    public List<PointSample> getSamples() {
        return samples;
    }
    public void setSamples(List<PointSample> samples) {
        this.samples = samples;
    }

}