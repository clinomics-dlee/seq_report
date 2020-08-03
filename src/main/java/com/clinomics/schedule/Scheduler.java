package com.clinomics.schedule;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.clinomics.entity.seq.Result;
import com.clinomics.enums.StatusCode;
import com.clinomics.repository.seq.ResultRepository;
import com.clinomics.specification.seq.ResultSpecification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Scheduler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	ResultRepository resultRepository;

	@Scheduled(cron = "10 * * * * *")
	public void run() {
	}
	
	
	/**
	 * Chip Analysis 완료 스케쥴러
	 * 스케쥴 1 분마다 (60초 = 1000 * 60)
	 */
	@Transactional
	@Scheduled(fixedDelay = 1000 * 60)
	public void completePdf() {
		// #. 상태가 S100_PDF_CREATING(PDF 생성중) 인 목록 조회
		Specification<Result> where = Specification
				.where(ResultSpecification.statusEqual(StatusCode.S100_PDF_CREATING));

		List<Result> list = resultRepository.findAll(where);

		List<Result> savedResults = new ArrayList<Result>();
		for (Result result : list) {
			String filePath = result.getFilePath();
			String pdfFilePath = filePath + "/Result.pdf";
			File pdf = new File(pdfFilePath);
			
			if (pdf.exists()) {
				result.setStatusCode(StatusCode.S110_PDF_CMPL);
				savedResults.add(result);
			}
		}

		resultRepository.saveAll(savedResults);
	}
    
}