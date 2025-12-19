package com.PixelQuest.Entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class PlayerLevel {

    private int totalScore = 0;
    private String rank = "";

    public PlayerLevel() {}

    public PlayerLevel(int totalScore) {
        this.totalScore = totalScore;
        this.rank = computeRank(totalScore);
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void addScore(int score) {
        this.totalScore += score;
        this.rank = computeRank(this.totalScore);
    }

    public String getRank() {
        return rank;
    }

    // Needed so Player.setLevel(...) works
    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
        this.rank = computeRank(totalScore);
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    // Your own logic for rank
    private String computeRank(int score) {
        if (score < 100) return "Beginner";
        if (score < 200) return "Intermediate";
        if (score < 400) return "Advanced";
        return "Pro";
    }
}
