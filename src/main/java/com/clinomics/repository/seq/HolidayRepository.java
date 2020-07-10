package com.clinomics.repository.seq;

import java.util.List;

import com.clinomics.entity.seq.Holiday;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HolidayRepository extends JpaRepository<Holiday, Integer> {
    List<Holiday> findByDateBetween(String fromYyyymmdd, String toYyyymmdd);
    int countByDateBetween(String fromYyyymmdd, String toYyyymmdd);
}