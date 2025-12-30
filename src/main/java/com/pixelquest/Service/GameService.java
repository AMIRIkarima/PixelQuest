package com.pixelquest.Service;

import com.pixelquest.Entity.*;
import com.pixelquest.Repository.GameRepository;
import com.pixelquest.Repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PlayerRepository playerRepository;

    // Defaults match the ESP32 OLED in your attachment (128x64)
    @Value("${pixelquest.screen.width:128}")
    private int screenWidth;

    @Value("${pixelquest.screen.height:64}")
    private int screenHeight;

    // Default Fitts parameters used if a player has no history
    private final double DEFAULT_A = 0.2;
    private final double DEFAULT_B = 0.1;

    /**
     * MOBILE APP: Starts a new game experiment.
     * Generates a random target based on the selected difficulty.
     */
    public Game startGame(Long playerId, Difficulty difficulty) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        Game game = new Game();
        game.setPlayer(player);
        game.setDifficulty(difficulty);
        game.setCreationDate(LocalDateTime.now());

        // Setup points: Start fixed to center of the device screen
        int range = (int) (difficulty.getMultiplier() * 100);
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;
        game.setStartPoint(new Point(centerX, centerY));
        // Target randomized around center to match ESP32 behavior (offsets from center)
        int offsetX = (int) ((Math.random() - 0.5) * range);
        int offsetY = (int) ((Math.random() - 0.5) * range);
        int tx = Math.max(0, Math.min(screenWidth - 1, centerX + offsetX));
        int ty = Math.max(0, Math.min(screenHeight - 1, centerY + offsetY));
        game.setTargetPoint(new Point(tx, ty));

        return gameRepository.save(game);
    }

    /**
     * ESP32: Polls for the active mission started by the mobile app.
     */
    public Game getActiveGameForPlayer(Long playerId) {
        return gameRepository.findByPlayer_Id(playerId).stream()
                .filter(p -> p.getScore() == null)
                .sorted((p1, p2) -> p2.getCreationDate().compareTo(p1.getCreationDate()))
                .findFirst()
            .orElseThrow(() -> new RuntimeException("No active game found for this player"));
    }

    /**
     * BACKEND: Fitts's Law Calculation and XP reward.
     * Called by ESP32 providing the direct movement time and distance achieved.
     */
    public Game finishGameWithDirectData(Long gameId, double timeSec, double distance) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // 1. Store direct metrics from hardware
        game.setMovementTime(timeSec);

        // 2. Calculate Index of Difficulty (ID) -> ID = log2(D + 1)
        double id = Math.log(distance + 1) / Math.log(2);
        game.setIndexDifficulty(id);

        // 3. Calculate Expected Time using Player's current parameters
        // We use the existing values in the Player entity
        double a = (game.getPlayer().getFittsA() != null) ? game.getPlayer().getFittsA() : 0.5; // Default 0.5
        double b = (game.getPlayer().getFittsB() != null) ? game.getPlayer().getFittsB() : 0.2; // Default 0.2
        double expectedTime = a + b * id;

        // 4. XP computation
        double xp = game.getDifficulty().getMultiplier() * 50 * (expectedTime / timeSec);
        xp = Math.max(0, Math.min(500, xp));

        game.setScore(xp);

        // Save the game first so it is included in the regression math
        gameRepository.save(game);

        // 5. Update player's global level progress
        updatePlayerLevel(game.getPlayer().getId(), (int) xp);

        // 6. Recalculate and update Player's Fitts A and B (The Regression)
        updatePlayerFittsParameters(game.getPlayer());

        return game;
    }

    /**
     * ANALYTICS: Updates the player's personal Fitts parameters (a and b)
     * using Linear Regression on their game history.
     */
    private void updatePlayerFittsParameters(Player player) {
        // Fetch all games for this player that have valid data
        List<Game> validGames = gameRepository.findByPlayer_Id(player.getId()).stream()
                .filter(g -> g.getMovementTime() != null && g.getIndexDifficulty() != null)
                .toList();

        // We need at least 2 points to calculate a trend (slope and intercept)
        if (validGames.size() >= 2) {
            double n = validGames.size();
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

            for (Game g : validGames) {
                double x = g.getIndexDifficulty();
                double y = g.getMovementTime();
                sumX += x;
                sumY += y;
                sumXY += (x * y);
                sumX2 += (x * x);
            }

            // Linear Regression Formulas
            double denominator = (n * sumX2 - Math.pow(sumX, 2));

            // Avoid division by zero if all IDs are the same
            if (denominator != 0) {
                double b = (n * sumXY - sumX * sumY) / denominator;
                double a = (sumY - b * sumX) / n;

                player.setFittsA(a);
                player.setFittsB(b);
                playerRepository.save(player); // Persistent update to the DB
            }
        }
    }

    /**
     * Updates player score and rank.
     */
    public void updatePlayerLevel(Long playerId, int gainedScore) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        PlayerLevel level = player.getLevel();
        if (level == null) {
            level = new PlayerLevel(0);
        }

        level.addScore(gainedScore);
        player.setLevel(level);
        playerRepository.save(player);
    }


    // --- Analytics Helper Methods for Controllers ---

    public List<Game> getGamesByPlayer(Long playerId) {
        return gameRepository.findByPlayer_Id(playerId);
    }

    public Double getInterceptA(Long playerId) {
        return playerRepository.findById(playerId)
                .map(Player::getFittsA)
                .orElse(DEFAULT_A);
    }

    public Double getSlopeB(Long playerId) {
        return playerRepository.findById(playerId)
                .map(Player::getFittsB)
                .orElse(DEFAULT_B);
    }
}