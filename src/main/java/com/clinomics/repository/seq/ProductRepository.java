package com.clinomics.repository.seq;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clinomics.entity.seq.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	
	List<Product> findByName(String name);

}
