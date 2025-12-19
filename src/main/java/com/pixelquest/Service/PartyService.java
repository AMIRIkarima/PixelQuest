package com.pixelquest.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.pixelquest.Entity.*;
import com.pixelquest.Repository.PartyRepository;
import com.pixelquest.Repository.PlayerRepository;
import com.pixelquest.Repository.PointSampleRepository;
import com.pixelquest.DTO.PointSampleDTO;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartyService {
        
    @Autowired
    private PartyRepository partyRepository; 

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PointSampleRepository pointSampleRepository;


    // Initial global Fitts parameters (temporary)
    private final double A = 0.2;
    private final double B = 0.1;

    /*
       Start a pointing experiment
    */
    public Party startParty(
            Long playerId,
            Difficulty difficulty,
            int startX,
            int startY,
            int targetX,
            int targetY
    ) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        Party party = new Party();
        party.setPlayer(player);
        party.setDifficulty(difficulty);
        party.setCreationDate(LocalDateTime.now());

        party.setStartPoint(new Point(startX, startY));
        party.setTargetPoint(new Point(targetX, targetY));

        return partyRepository.save(party);
    }


    /*  Store trajectory samples
        */
    public void addSamples(Long partyId, List<PointSampleDTO> samples) {

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found"));

        for (PointSampleDTO dto : samples) {

            PointSample sample = new PointSample();
            sample.setPoint(new Point(dto.getX(), dto.getY()));
            sample.setTimestamp(dto.getTimestamp());
            sample.setParty(party);

            pointSampleRepository.save(sample);
        }
    }

    /*  Finish experiment & compute XP
       */
    public Party finishParty(Long partyId) {

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found"));

        List<PointSample> samples =
                pointSampleRepository.findByPartyId(partyId);

        if (samples.size() < 2) {
            throw new RuntimeException("Not enough samples to compute metrics");
        }

        // Movement time (seconds)
        long t0 = samples.get(0).getTimestamp();
        long tEnd = samples.get(samples.size() - 1).getTimestamp();
        double movementTime = (tEnd - t0) / 1000.0;

        // Initial distance (pixels)
        Point start = party.getStartPoint();
        Point target = party.getTargetPoint();

        double distance = Math.hypot(
                target.getX() - start.getX(),
                target.getY() - start.getY()
        );

        // Fitts expected time
        double expectedTime =
                A + B * (Math.log(distance + 1) / Math.log(2));

        // XP computation
        double xp = party.getDifficulty().getMultiplier()
                * 50
                * (expectedTime / movementTime);

        xp = Math.max(0, Math.min(500, xp));

        party.setScore(xp);
        partyRepository.save(party);

        updatePlayerLevel(party.getPlayer().getId(), (int) xp);
        updatePlayerFittsParameters(party.getPlayer());

        return party;
    }

    /*  Update player level
        */
    public Player updatePlayerLevel(Long playerId, int gainedScore) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        PlayerLevel level = player.getLevel();
        if (level == null) {
            level = new PlayerLevel(0);
        }

        level.addScore(gainedScore);
        player.setLevel(level);

        return playerRepository.save(player);
    }

    /*  Analytics: Compute Fitts Parameters
        */
    // 1. Fetch all parties for the dashboard
    public List<Party> getPartiesByPlayer(Long playerId) {
        return partyRepository.findByPlayerId(playerId);
    }

    // 2. Calculate Index of Difficulty (ID) on the fly
    public double calculateID(Party party) {
        double distance = Math.hypot(
                party.getTargetPoint().getX() - party.getStartPoint().getX(),
                party.getTargetPoint().getY() - party.getStartPoint().getY()
        );
        // ID = log2(D + 1)
        return Math.log(distance + 1) / Math.log(2);
    }

    // 3. Calculate Movement Time (MT) on the fly
    public double calculateMT(Party party) {
        List<PointSample> samples = pointSampleRepository.findByPartyId(party.getId());
        if (samples.isEmpty() || samples.size() < 2) return 0.0;

        long t0 = samples.get(0).getTimestamp();
        long tEnd = samples.get(samples.size() - 1).getTimestamp();
        return (tEnd - t0) / 1000.0; // Seconds
    }

    // 4. Get the stored A from the Player
    public Double getInterceptA(Long playerId) {
        return playerRepository.findById(playerId)
                .map(Player::getFittsA)
                .orElse(0.0);
    }

    // 5. Get the stored B from the Player
    public Double getSlopeB(Long playerId) {
        return playerRepository.findById(playerId)
                .map(Player::getFittsB)
                .orElse(0.0);
    }
    private void updatePlayerFittsParameters(Player player) {
        List<Party> history = partyRepository.findByPlayerId(player.getId());

        // We need at least 2 data points for a line
        if (history.size() < 2) return;

        double sumID = 0;
        double sumMT = 0;
        double sumIDMT = 0;
        double sumIDSq = 0;
        int n = 0;

        for (Party p : history) {
            // Skip incomplete parties
            if (p.getScore() == null || p.getStartPoint() == null || p.getTargetPoint() == null) continue;

            // 1. Calculate Distance (D)
            double distance = Math.hypot(
                    p.getTargetPoint().getX() - p.getStartPoint().getX(),
                    p.getTargetPoint().getY() - p.getStartPoint().getY()
            );

            // 2. Calculate Index of Difficulty (ID)
            // ID = log2(D/W + 1). Assuming W (target width) = 1 pixel
            double ID = Math.log(distance + 1) / Math.log(2);

            // 3. Get Movement Time (MT)
            // We need to retrieve MT. Since it's not stored directly in Party,
            // we re-calculate it or you should store 'movementTime' in Party entity.
            // For now, let's estimate it from samples if available, or assume you add a field to Party.


            List<PointSample> samples = pointSampleRepository.findByPartyId(p.getId());
            if(samples.isEmpty()) continue;

            long t0 = samples.get(0).getTimestamp();
            long tEnd = samples.get(samples.size() - 1).getTimestamp();
            double MT = (tEnd - t0) / 1000.0; // Seconds

            // Linear Regression Sums
            sumID += ID;
            sumMT += MT;
            sumIDMT += (ID * MT);
            sumIDSq += (ID * ID);
            n++;
        }

        if (n < 2) return;

        // Least Squares formulas
        // Slope (b) = (n*sumXY - sumX*sumY) / (n*sumX^2 - (sumX)^2)
        double slopeB = (n * sumIDMT - sumID * sumMT) / (n * sumIDSq - sumID * sumID);

        // Intercept (a) = (sumY - b*sumX) / n
        double interceptA = (sumMT - slopeB * sumID) / n;

        // Save to Player
        player.setFittsA(interceptA);
        player.setFittsB(slopeB);
        playerRepository.save(player);
}


    public Party startPartyWithRandomTarget(Long playerId, Difficulty difficulty) {
        // 1. Generate random coordinates based on difficulty
        // Higher difficulty could mean further distance
        int range = (int) (difficulty.getMultiplier() * 100);

        int startX = 0;
        int startY = 0;
        int targetX = (int) (Math.random() * range) + 20; // Ensure at least 20px distance
        int targetY = (int) (Math.random() * range) + 20;

        // 2. Call your existing startParty method
        return startParty(playerId, difficulty, startX, startY, targetX, targetY);
    }

    public Party findLatestActiveParty(Long playerId) {
        // Finds the most recent party for this player that hasn't been finished (score is null)
        return partyRepository.findByPlayerId(playerId).stream()
                .filter(p -> p.getScore() == null)
                .sorted((p1, p2) -> p2.getCreationDate().compareTo(p1.getCreationDate()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No active party found for player"));
    }
}
