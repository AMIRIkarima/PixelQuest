package com.pixelquest.Repository;
import com.pixelquest.Entity.PointSample;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PointSampleRepository
        extends JpaRepository<PointSample, Long> {

  List<PointSample> findByPartyId(Long partyId);
}
