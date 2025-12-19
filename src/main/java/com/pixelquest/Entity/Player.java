package com.pixelquest.Entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Embedded
    private PlayerLevel playerlevel;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL)
    private List<Party> parties;



    private Double fittsA; // Intercept (a)
    private Double fittsB; // Slope (b)

    // Getters & Setters
    public void setId(Long id) {this.id = id;}
    public Double getFittsA() { return fittsA; }
    public void setFittsA(Double fittsA) { this.fittsA = fittsA; }

    public Double getFittsB() { return fittsB; }
    public void setFittsB(Double fittsB) { this.fittsB = fittsB; }


    public Long getId() {
        return id;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



    public List<Party> getParties() {
        return parties;
    }
    public void setParties(List<Party> parties) {
        this.parties = parties;
    }

    public PlayerLevel getLevel() {
        return playerlevel;
    }

    public void setLevel(PlayerLevel level) {
        this.playerlevel = level;
    }
}