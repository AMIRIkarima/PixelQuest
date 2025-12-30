package com.pixelquest.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @JsonManagedReference
    private List<Game> games;



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



    public List<Game> getGames() {
        return games;
    }
    public void setGames(List<Game> games) {
        this.games = games;
    }

    public PlayerLevel getLevel() {
        return playerlevel;
    }

    public void setLevel(PlayerLevel level) {
        this.playerlevel = level;
    }
}