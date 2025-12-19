package com.pixelquest;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.pixelquest.Entity.*;
import com.pixelquest.Repository.*;
import com.pixelquest.Service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class PartyServiceTest {

  @Mock private PartyRepository partyRepository;
  @Mock private PlayerRepository playerRepository;
  @Mock private PointSampleRepository pointSampleRepository;

  @InjectMocks private PartyService partyService;

  private Player player;
  private Party party;

  @BeforeEach
  void setUp() {
    player = new Player();
    player.setId(1L);
    player.setLevel(new PlayerLevel(0));

    party = new Party();
    party.setId(100L);
    party.setPlayer(player);
    party.setDifficulty(Difficulty.EASY);
    party.setStartPoint(new Point(0, 0));
    party.setTargetPoint(new Point(100, 0)); // Distance = 100
  }

  @Test
  void testFinishPartyCalculatesScore() {
    // Arrange: Mock two samples (0ms and 1000ms)
    PointSample s1 = new PointSample(); s1.setTimestamp(0L);
    PointSample s2 = new PointSample(); s2.setTimestamp(1000L); // 1 second movement

    when(partyRepository.findById(100L)).thenReturn(Optional.of(party));
    when(pointSampleRepository.findByPartyId(100L)).thenReturn(Arrays.asList(s1, s2));
    when(playerRepository.findById(1L)).thenReturn(Optional.of(player));
    when(playerRepository.save(any())).thenReturn(player);

    // Act
    Party finishedParty = partyService.finishParty(100L);

    // Assert
    assertNotNull(finishedParty.getScore());
    assertTrue(finishedParty.getScore() > 0, "Score should be positive");
    verify(partyRepository, times(1)).save(any(Party.class)); // Once in finish, once for player
  }
}
