package com.pixelquest.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double score;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    private LocalDateTime creationDate;

    @ManyToOne
    @JoinColumn(name = "player_id")
    @JsonBackReference
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

//    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
//    private List<PointSample> samples;
    private Double movementTime;   // To store 'elapsed' from ESP32
    private Double indexDifficulty; // To store log2(D + 1)
    public Game() {}

    //getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIndexDifficulty(Double indexDifficulty) {
        this.indexDifficulty = indexDifficulty;
    }
    public void setMovementTime(Double movementTime) {
        this.movementTime = movementTime;
    }
    public Double getMovementTime() {
        return movementTime;
    }
    public Double getIndexDifficulty() {
        return indexDifficulty;
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

    @JsonProperty("playerId")
    public Long getPlayerId() {
        return (player != null) ? player.getId() : null;
    }

}