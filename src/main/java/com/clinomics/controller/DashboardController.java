package com.clinomics.controller;

import java.util.Map;

import com.clinomics.service.CalendarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/")
@Controller
public class DashboardController {

	@Autowired
	CalendarService calendarService;

	@GetMapping("/calendar/get/statistics")
	@ResponseBody
	public Map<String, Object> calendar(@RequestParam Map<String, String> params) {
		return calendarService.selectCountByMonthly(params);
	}

	@GetMapping("/popup/registered")
	@ResponseBody
	public Map<String, Object> registered(@RequestParam Map<String, String> params) {
		return calendarService.selectRegistered(params);
	}

	@GetMapping("/popup/running")
	@ResponseBody
	public Map<String, Object> running(@RequestParam Map<String, String> params) {
		return calendarService.selectRunning(params);
	}

	@GetMapping("/popup/completed")
	@ResponseBody
	public Map<String, Object> completed(@RequestParam Map<String, String> params) {
		return calendarService.selectCompleted(params);
	}

}