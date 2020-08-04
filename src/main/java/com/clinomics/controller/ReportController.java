package com.clinomics.controller;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clinomics.service.ReportService;
import com.clinomics.service.setting.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Controller
public class ReportController {

	@Autowired
	MemberService userService;

	@Autowired
	ReportService reportService;
	
	@Value("${seq.workspacePath}")
    private String workspacePath;

	@GetMapping("/report")
	public String getReport(@RequestParam Map<String, String> params, Model model) {
		String filePath = params.get("filePath");
		System.out.println("★★★★★★★★★★ filePath=" + filePath);

		Map<String, Object> info = reportService.getReportInfo(filePath);
		
		model.addAttribute("datas", info.get("datas"));
		model.addAttribute("customer", info.get("customer"));
		model.addAttribute("service", info.get("service"));
		model.addAttribute("filePath", info.get("filePath"));
		model.addAttribute("workspace", info.get("workspace"));

		return "report/print";
	}
}
