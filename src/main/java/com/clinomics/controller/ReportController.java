package com.clinomics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.clinomics.entity.seq.Member;
import com.clinomics.service.setting.MemberService;

@Controller
public class ReportController {

	@Autowired
	MemberService userService;
	
	@GetMapping("/report")
	public String getUsers(Model model) {
		
		return "report/print";
	}
	
	@GetMapping("/report/{page}")
	public String getUsers(@PathVariable String page, Model model) {
		
		return "report/" + page;
	}
}
