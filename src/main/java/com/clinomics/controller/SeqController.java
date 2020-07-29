package com.clinomics.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clinomics.service.SeqService;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.layout.font.FontProvider;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RequestMapping("/seq")
@RestController
public class SeqController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${seq.tempFilePath}")
	private String tempFilePath;

	@Autowired
	private SeqService seqService;

	@GetMapping("/report/get")
	public Map<String, Object> getReport(@RequestParam Map<String, String> params) {
		return seqService.findReportByParams(params);
	}

	@PostMapping("/report/save")
	public Map<String, String> save(@RequestParam("file") MultipartFile multipartFile, MultipartHttpServletRequest request)
			throws InvalidFormatException, IOException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String memberId = userDetails.getUsername();
		
		return seqService.save(multipartFile, memberId);
	}

	@PostMapping("/report/delete")
	public Map<String, String> delete(@RequestBody List<Integer> ids) {
		return seqService.delete(ids);
	}

	@GetMapping("/report/pdf")
	public void exportReportPdf(@RequestParam Map<String, String> params, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			File tempDir = new File(tempFilePath);
			if (!tempDir.exists()) tempDir.mkdirs();

			File file = File.createTempFile("temp_", ".pdf", tempDir);
			// #. pdf로 변활할 html가공
			String domain = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
			String pdfPageAddress = domain + "/pdf/template";
			InputStream is = new URL(pdfPageAddress).openStream();

			// #. 한글 문자열 문제로 인하여 폰트 지정
			ConverterProperties converterProp = new ConverterProperties();
			FontProvider fontProvider = new DefaultFontProvider(false, false, false);
			fontProvider.addFont(FontProgramFactory.createFont("/static/assets/font/NotoSansKR-Black.otf"));
			fontProvider.addFont(FontProgramFactory.createFont("/static/assets/font/NotoSansKR-Bold.otf"));
			fontProvider.addFont(FontProgramFactory.createFont("/static/assets/font/NotoSansKR-Light.otf"));
			fontProvider.addFont(FontProgramFactory.createFont("/static/assets/font/NotoSansKR-Medium.otf"));
			fontProvider.addFont(FontProgramFactory.createFont("/static/assets/font/NotoSansKR-Regular.otf"));
			fontProvider.addFont(FontProgramFactory.createFont("/static/assets/font/NotoSansKR-Thin.otf"));
			converterProp.setFontProvider(fontProvider);

			// #. HTML 을 PDF로 변경
			HtmlConverter.convertToPdf(is, new FileOutputStream(file), converterProp);

			requestFile(file, "report.pdf", request, response, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@GetMapping("/report/html")
	public void exportReportHtml(@RequestParam Map<String, String> params, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			File tempDir = new File(tempFilePath);
			if (!tempDir.exists()) tempDir.mkdirs();

			File file = File.createTempFile("temp_", ".html", tempDir);

			// #. pdf로 변활할 html가공
			String domain = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
			String pdfPageAddress = domain + "/pdf/template";
			InputStream is = new URL(pdfPageAddress).openStream();

			CopyOption[] options = new CopyOption[] {
				StandardCopyOption.REPLACE_EXISTING // 대상파일이 있어도 덮어쓴다
			};
			Files.copy(is, file.toPath(), options);

			requestFile(file, "report.html", request, response, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ############################ private
	private void requestFile(File file, String fileName, HttpServletRequest request, HttpServletResponse response, boolean deleteFile) {
		FileInputStream fis = null;
		OutputStream out = null;
		// #. 삭제되어야 하는 파일들
		List<File> garbageFiles = new ArrayList<File>();

		try {
			if (deleteFile) {
				garbageFiles.add(file);
			}

			String userAgent = request.getHeader("User-Agent");
			boolean ie = userAgent.indexOf("MSIE") > -1 || userAgent.indexOf("Trident") > -1 || userAgent.indexOf("Edge") > -1;
			if (ie) {
				fileName = URLEncoder.encode(file.getName(), "utf-8");
			} else {
				fileName = new String(file.getName().getBytes("utf-8"), "ISO-8859-1");
			}

			response.setContentType("application/octet-stream");
			response.setContentLengthLong(file.length());

			response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\";");
			response.setHeader("Content-Transfer-Encoding", "binary");
			// #. jquery.fileDownload 후 화면 처리를 위한 셋팅
			// response.setHeader("Set-Cookie", "fileDownload=true; path=/");

			out = response.getOutputStream();

			fis = new FileInputStream(file);
			FileCopyUtils.copy(fis, response.getOutputStream());
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (File garbageFile : garbageFiles) {
				if (garbageFile.exists()) {
					garbageFile.delete();
				}
			}
		}
	}
}
