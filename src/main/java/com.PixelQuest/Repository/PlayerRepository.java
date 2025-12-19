package com.PixelQuest.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.PixelQuest.Entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {}