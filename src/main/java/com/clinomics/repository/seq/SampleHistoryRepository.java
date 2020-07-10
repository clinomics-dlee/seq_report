package com.clinomics.repository.seq;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.clinomics.entity.seq.SampleHistory;

public interface SampleHistoryRepository extends JpaRepository<SampleHistory, Integer> {
	boolean existsBySample_Id(String sampleId);
	Long countBySample_Id(String sampleId);
	List<SampleHistory> findBySample_Id(String sampleId, Pageable pageable);
}
