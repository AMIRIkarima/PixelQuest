package com.PixelQuest.Entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    @Embedded
    private PlayerLevel level;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<Party> parties;

    public Player() {}

    private Double fittsA; // Intercept (a)
    private Double fittsB; // Slope (b)

    // ... getters and setters ...
    public Double getFittsA() { return fittsA; }
    public void setFittsA(Double fittsA) { this.fittsA = fittsA; }

    public Double getFittsB() { return fittsB; }
    public void setFittsB(Double fittsB) { this.fittsB = fittsB; }
    // Getters & Setters

    public Long getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public PlayerLevel getLevel() {
        return level;
    }
    public void setLevel(PlayerLevel level) {
        this.level = level;
    }


    public List<Party> getParties() {
        return parties;
    }
    public void setParties(List<Party> parties) {
        this.parties = parties;
    }

}