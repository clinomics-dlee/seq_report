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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.Context;
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

	@GetMapping("/report/test")
	public ResponseEntity<Resource> getTestPage(@RequestParam Map<String, String> params
		, HttpServletRequest request, HttpServletResponse response) {

		String filePath = params.get("filePath");
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setCacheable(false);
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode("HTML5");

		templateEngine.setTemplateResolver(templateResolver);
		
		final WebContext ctx = new WebContext(request, response, request.getServletContext());
		Map<String, Object> info = reportService.getReportInfo(filePath);

		ctx.setVariable("datas", info.get("datas"));
		ctx.setVariable("customer", info.get("customer"));
		ctx.setVariable("service", info.get("service"));
		ctx.setVariable("filePath", info.get("filePath"));
		ctx.setVariable("workspace", filePath);

		String result = templateEngine.process("report/view", ctx);

		result = reportService.imageToBase64String(result);
		
		InputStream targetStream = new ByteArrayInputStream(result.getBytes());
		InputStreamResource resource = new InputStreamResource(targetStream);

		HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.html");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
		header.add("Expires", "0");
		
		return ResponseEntity.ok()
				.headers(header)
				.contentLength(result.getBytes().length)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
		
	}

	@GetMapping("/report/{page}")
	public String getReportPage(@PathVariable String page, Model model) {

		return "report/" + page;
	}
}
