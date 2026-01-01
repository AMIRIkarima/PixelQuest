package com.pixelquest.Controller;

import com.pixelquest.Entity.PlayerLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pixelquest.Entity.Player;
import com.pixelquest.Service.PlayerService;

import java.util.List;

@RestController
@RequestMapping("/players")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @PostMapping
    public Player create(@RequestBody Player p) {
        return playerService.createPlayer(p);
    }

    @GetMapping("/{id}")
    public Player getProfile(@PathVariable Long id) {
        return playerService.getPlayer(id);
    }


    @GetMapping("/{id}/level")
    public PlayerLevel getLevel(@PathVariable Long id) {
        return playerService.getPlayer(id).getLevel();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}