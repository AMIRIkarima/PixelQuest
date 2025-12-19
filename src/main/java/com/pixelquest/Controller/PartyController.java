package com.pixelquest.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.pixelquest.Entity.Party;
import com.pixelquest.Entity.Difficulty;


import com.pixelquest.Service.PartyService;

import com.pixelquest.DTO.PointSampleDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/parties")
public class PartyController {

    @Autowired
    private PartyService partyService;

    // STEP 1: Mobile App calls this to start a game
    @PostMapping("/start")
    public Party startParty(@RequestParam Long playerId, @RequestParam Difficulty difficulty) {
        // Logic inside service should generate targetX and targetY based on difficulty
        return partyService.startPartyWithRandomTarget(playerId,difficulty);
    }

    // STEP 2: ESP32 calls this to send the trajectory points
    @PostMapping("/{partyId}/samples")
    public void addSamples(@PathVariable Long partyId, @RequestBody List<PointSampleDTO> samples) {
        partyService.addSamples(partyId, samples);
    }

    // STEP 3: Triggered after samples are sent to calculate score
    @PostMapping("/{partyId}/finish")
    public Party finishParty(@PathVariable Long partyId) {
        return partyService.finishParty(partyId);
    }

    // STEP 4: ESP32 calls this to know what to draw on OLED
    @GetMapping("/active/{playerId}")
    public Party getActiveParty(@PathVariable Long playerId) {
        return partyService.findLatestActiveParty(playerId);
    }
}