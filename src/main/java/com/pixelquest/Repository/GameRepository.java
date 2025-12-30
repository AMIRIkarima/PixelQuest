package com.pixelquest.Repository;

import com.pixelquest.Entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

//    @Query("SELECT COALESCE(SUM(g.score),0) FROM Game g WHERE g.player.id = :playerId")
//    double sumXpByPlayer(@Param("playerId") Long playerId);


    List<Game> findByPlayer_Id(Long playerId);

}
