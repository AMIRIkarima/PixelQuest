package com.PixelQuest.Repository;
import com.PixelQuest.Entity.PointSample;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointSampleRepository
        extends JpaRepository<PointSample, Long> {

  List<PointSample> findByPartyId(Long partyId);
}
