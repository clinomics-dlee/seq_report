package com.clinomics.repository.seq;

import java.util.List;

import com.clinomics.entity.seq.Report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportRepository extends JpaRepository<Report, Integer>, JpaSpecificationExecutor<Report> {
    List<Report> findByIdIn(List<Integer> id);
}
