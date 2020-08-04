package com.clinomics.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clinomics.entity.seq.Result;
import com.clinomics.service.ReportService;
import com.clinomics.service.ResultService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@RestController
public class HtmlController {
    
	@Autowired
	ReportService reportService;

	@Autowired
	private ResultService resultService;
	
	@Value("${seq.workspacePath}")
    private String workspacePath;
	
	@GetMapping("/result/download/html/{id}")
	public ResponseEntity<Resource> getTestPage(@PathVariable int id
		, HttpServletRequest request, HttpServletResponse response) {
		
		Result result = resultService.findResultById(id);
		String filePath = result.getFilePath();
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

		String html = templateEngine.process("report/view", ctx);

		html = reportService.imageToBase64String(html);
		
		InputStream targetStream = new ByteArrayInputStream(html.getBytes());
		InputStreamResource resource = new InputStreamResource(targetStream);

		HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=test.html");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
		header.add("Expires", "0");
		
		return ResponseEntity.ok()
				.headers(header)
				.contentLength(html.getBytes().length)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(resource);
		
	}
}