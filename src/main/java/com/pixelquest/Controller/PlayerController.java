package com.pixelquest.Controller;

import com.pixelquest.Entity.PlayerLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.pixelquest.Entity.Player;
import com.pixelquest.Service.PlayerService;

@RestController
@RequestMapping("/players")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @PostMapping
    public Player create(@RequestBody Player p) {
        return playerService.createPlayer(p);
    }

    @GetMapping("/{id}")
    public Player getProfile(@PathVariable Long id) {
        return playerService.getPlayer(id);
    }

    // New: Mobile app can check current level and XP
    @GetMapping("/{id}/level")
    public PlayerLevel getLevel(@PathVariable Long id) {
        return playerService.getPlayer(id).getLevel();
    }
}