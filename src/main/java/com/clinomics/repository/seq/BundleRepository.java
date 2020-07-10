package com.clinomics.repository.seq;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.clinomics.entity.seq.Bundle;

public interface BundleRepository extends JpaRepository<Bundle, Integer> {
	List<Bundle> findByIsActiveTrue();
	List<Bundle> findByNameAndIsActiveTrue(String name);
	List<Bundle> findByIsSingleAndIsActiveTrue(boolean isSingle);
	List<Bundle> findByIsSingleAndIsActiveTrue(boolean isSingle, Pageable pageable);
	List<Bundle> findByProductIdAndIsSingleAndIsActiveTrue(int productId, boolean isSingle);
	Long countByIsSingleAndIsActiveTrue(boolean isSingle);
	Bundle findByType(String type);
}
