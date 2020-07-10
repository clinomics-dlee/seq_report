package com.clinomics.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.clinomics.entity.seq.Sample;
import com.clinomics.service.OutputService;
import com.clinomics.util.EmailSender;
import com.google.common.collect.Maps;

@RequestMapping("/rest")
@RestController
public class ApiController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	OutputService outputService;
	
	@Autowired
	EmailSender emailSender;
	
	@RequestMapping(value = "/result/get")
	public Map<String, Object> getResultWithWaitStatus(@RequestParam Map<String, String> params) {
		logger.info("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆ getResultWithWaitStatus ☆☆☆ IN interface : /result/get ");
		HttpServletRequest req = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		String ip = req.getHeader("X-FORWARDED-FOR");
		if (ip == null)	ip = req.getRemoteAddr();
		
		logger.info("☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆ getResultWithWaitStatus ☆☆☆ request IP : [" + ip + "]");
		return outputService.getResultsForRest(params);
	}
	
	@RequestMapping(value = "/mail/test")
	public String sendMailTest() {
		Sample sample = new Sample();
		sample.setChipBarcode("test1");
		sample.setLaboratoryId("TEST-0000-0001");
		sample.setStatusMessage("Test is good.");
		sample.setVersion(1);
		List<Sample> samples = new ArrayList<Sample>();
		samples.add(sample);
		emailSender.sendMailToFail(samples);
		return "asdf";
	}
}
