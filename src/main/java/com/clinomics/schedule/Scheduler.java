package com.clinomics.schedule;

import com.clinomics.util.EmailSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Scheduler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	EmailSender emailSender;
	
	@Scheduled(cron = "10 * * * * *")
	public void run() {
	}
	
	
	/**
	 * Chip Analysis 완료 스케쥴러
	 * 스케쥴 1 분마다 (60초 = 1000 * 60)
	 */
	@Transactional
	@Scheduled(fixedDelay = 1000 * 60)
	public void completeChipAnalysis() {
		
	}
    
}