package com.clinomics.repository.seq;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinomics.entity.seq.SampleItem;

public interface SampleItemRepository extends JpaRepository<SampleItem, Integer> {
	
	List<SampleItem> findByName(String name);

}
