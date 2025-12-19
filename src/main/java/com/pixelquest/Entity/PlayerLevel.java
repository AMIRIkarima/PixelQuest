package com.pixelquest.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class PlayerLevel {

    @Column(name = "total_score")
    private int totalScore = 0;

    @Column(name = "current_rank") // Renamed to avoid SQL keyword issues
    private String level = "Beginner";

    public PlayerLevel() {}

    public PlayerLevel(int totalScore) {
        this.totalScore = totalScore;
        this.level = computeRank(totalScore);
    }

    // Standard Getters
    public int getTotalScore() { return totalScore; }
    public String getLevel() { return level; }

    // Business Logic: The only way to change the score
    public void addScore(int score) {
        this.totalScore += score;
        this.level = computeRank(this.totalScore);
    }

    // Logic for Hibernate to use during reconstruction
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
        this.level = computeRank(totalScore);
    }

    // Private because we don't want anyone to manually set the rank
    // without changing the score.
    private void setLevel(String level) {
        this.level = level;
    }

    private String computeRank(int score) {
        if (score < 100) return "Beginner";
        if (score < 200) return "Intermediate";
        if (score < 400) return "Advanced";
        return "Pro";
    }
}