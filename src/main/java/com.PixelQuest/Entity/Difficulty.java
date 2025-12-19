package com.PixelQuest.Entity;

public enum Difficulty {
    EASY(1.0),
    MEDIUM(1.5),
    HARD(2.2);

    private final double multiplier;

    Difficulty(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
