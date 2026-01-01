package com.pixelquest.Controller;

import com.pixelquest.Entity.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.pixelquest.Entity.Difficulty;


import com.pixelquest.Service.GameService;

import org.springframework.web.bind.annotation.*;


import java.util.Map;

@RestController
@RequestMapping("/games")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private GameService gameService;

    // STEP 1: Mobile App calls this to start a game with a specific difficulty
    @PostMapping("/start")
    public Game startGame(@RequestParam Long playerId, @RequestParam Difficulty difficulty) {
        return gameService.startGame(playerId, difficulty);
    }

    // STEP 2: ESP32 calls this to fetch the mission coordinates (Target X, Y)
   @GetMapping("/active/{playerId}")
    public ResponseEntity<Game> getActiveGame(@PathVariable Long playerId) {
        Game game = gameService.getActiveGameForPlayer(playerId);

        if (game == null) {

            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(game);
    }
    // STEP 3: ESP32 calls this once to send final Time and Distance
    @PostMapping("/{gameId}/direct-finish")
    public Game finishDirect(@PathVariable Long gameId, @RequestBody Map<String, Double> data) {
        return gameService.finishGameWithDirectData(
                gameId,
                data.get("time_sec"),
                data.get("distance")
        );
    }
}