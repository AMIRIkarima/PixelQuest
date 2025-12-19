package com.pixelquest.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pixelquest.Entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {}