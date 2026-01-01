package com.pixelquest.Controller;

import com.pixelquest.Entity.Game;
import com.pixelquest.Service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

  @Autowired
  private GameService gameService;

  // Fetch data for the Scatter Plot (ID vs MT)
  @GetMapping("/fitts-data/{playerId}")
  public List<Map<String, Double>> getFittsRawData(@PathVariable Long playerId) {
        return gameService.getGamesByPlayer(playerId).stream()
          .filter(p -> p.getScore() != null && p.getMovementTime() != null)
          .map(p -> Map.of(
            "id", p.getIndexDifficulty(),
            "mt", p.getMovementTime()
          )).toList();
  }

  // Fetch the current Player's Fitts Law coefficients (a and b)
  @GetMapping("/constants/{playerId}")
  public Map<String, Double> getPlayerConstants(@PathVariable Long playerId) {
    return Map.of(
                "interceptA", gameService.getInterceptA(playerId),
                "slopeB", gameService.getSlopeB(playerId)
    );
  }

  // Fetch the history of games by Player Id
  @GetMapping("/history/{playerId}")
  public List<Game> getPlayerHistory(@PathVariable Long playerId) {
    return gameService.getGamesByPlayer(playerId);
  }
}