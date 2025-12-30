package com.pixelquest.Service;

import com.pixelquest.Entity.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pixelquest.Entity.Player;
import com.pixelquest.Repository.PlayerRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    public Player createPlayer(Player p) {
        return playerRepository.save(p);
    }

    public Player getPlayer(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Player not found"));
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
    public void updatePlayerPerformance(Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow();
        List<Game> finishedGames = player.getGames().stream()
                .filter(g -> g.getMovementTime() != null && g.getIndexDifficulty() != null)
                .collect(Collectors.toList());

        // we need at least 2 points to draw a line (slope and intercept)
        if (finishedGames.size() >= 2) {
            double n = finishedGames.size();
            double sumX = finishedGames.stream().mapToDouble(Game::getIndexDifficulty).sum();
            double sumY = finishedGames.stream().mapToDouble(Game::getMovementTime).sum();
            double sumXY = finishedGames.stream().mapToDouble(g -> g.getIndexDifficulty() * g.getMovementTime()).sum();
            double sumX2 = finishedGames.stream().mapToDouble(g -> Math.pow(g.getIndexDifficulty(), 2)).sum();

            // Formula for Slope (B) and Intercept (A)
            double slopeB = (n * sumXY - sumX * sumY) / (n * sumX2 - Math.pow(sumX, 2));
            double interceptA = (sumY - slopeB * sumX) / n;

            player.setFittsA(interceptA);
            player.setFittsB(slopeB);
            playerRepository.save(player);
        }
    }
}
