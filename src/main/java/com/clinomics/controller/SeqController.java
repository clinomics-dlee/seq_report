package com.clinomics.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.layout.font.FontProvider;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/seq")
@RestController
public class SeqController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@GetMapping("/report/pdf")
	public void exportReportPdf(@RequestParam Map<String, String> params, HttpServletResponse response) {
		try {
			File file = new File("/BiO/Serve/seq_report/test.pdf");
			// #. pdf로 변활할 html가공
			String html = "";

			InputStream input = new URL("http://127.0.0.1:8080/p/pdf/template").openStream();

			// #. 한글 문자열 문제로 인하여 폰트 지정
			ConverterProperties converterProp = new ConverterProperties();
			FontProvider fontProvider = new DefaultFontProvider(false, false, false);
			fontProvider.addFont(FontProgramFactory.createFont("/static/assets/font/NanumGothic.ttf"));
			converterProp.setFontProvider(fontProvider);

			// #. HTML 을 PDF로 변경
			// HtmlConverter.convertToPdf(html, new FileOutputStream(file), converterProp);
			HtmlConverter.convertToPdf(input, new FileOutputStream(file), converterProp);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	// ############################ private
	private void requestExcel(XSSFWorkbook xlsx, String fileName, HttpServletResponse response) {
		if (fileName == null || fileName.trim().length() < 1) {
			fileName = "sample";
		}
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
		// 엑셀파일명 한글깨짐 조치
		response.setHeader("Content-Transfer-Encoding", "binary;");
		response.setHeader("Pragma", "no-cache;");
		response.setHeader("Expires", "-1;");
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		try {
			ServletOutputStream out = response.getOutputStream();

			xlsx.write(out);
			xlsx.close();
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
