package com.PixelQuest.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.PixelQuest.Entity.*;
import com.PixelQuest.Repository.PartyRepository;
import com.PixelQuest.Repository.PlayerRepository;
import com.PixelQuest.Repository.PointSampleRepository;
import com.PixelQuest.DTO.PointSampleDTO;

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

    /* ===============================
       1️⃣ Start a pointing experiment
       =============================== */
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

    /* ===============================
       2️⃣ Store trajectory samples
       =============================== */
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

    /* ===============================
       3️⃣ Finish experiment & compute XP
       =============================== */
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

    /* ===============================
       4️⃣ Update player level
       =============================== */
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
    /* ===============================
       5️⃣ Analytics: Compute Fitts Parameters
       =============================== */
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
            // *Recommendation*: Add 'private Double movementTime' to Party entity to avoid fetching samples loop here.

            // Assuming you added movementTime to Party, or calculating it from samples (expensive):
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
}
