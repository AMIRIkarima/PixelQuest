package com.pixelquest.Repository;

import com.pixelquest.Entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
    @Query("SELECT COALESCE(SUM(p.score),0) FROM Party p WHERE p.player.id = :playerId")
    double sumXpByPlayer(@Param("playerId") Long playerId);

    List<Party> findByPlayerId(Long playerId);

}
