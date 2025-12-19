package com.pixelquest.Controller;

import com.pixelquest.Service.PartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

  @Autowired
  private PartyService partyService;

  @GetMapping("/fitts-data/{playerId}")
  public List<Map<String, Double>> getFittsRawData(@PathVariable Long playerId) {
    return partyService.getPartiesByPlayer(playerId).stream()
            .filter(p -> p.getScore() != null) // Only finished games
            .map(p -> Map.of(
                    "id", partyService.calculateID(p),
                    "mt", partyService.calculateMT(p)
            )).toList();
  }

  @GetMapping("/constants/{playerId}")
  public Map<String, Double> getPlayerConstants(@PathVariable Long playerId) {
    return Map.of(
            "interceptA", partyService.getInterceptA(playerId),
            "slopeB", partyService.getSlopeB(playerId)
    );
  }
}