package com.clinomics.repository.seq;

import java.util.List;

import com.clinomics.entity.seq.Result;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResultRepository extends JpaRepository<Result, Integer>, JpaSpecificationExecutor<Result> {
    List<Result> findByIdIn(List<Integer> id);
}
