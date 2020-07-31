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

import com.clinomics.entity.seq.Result;
import com.clinomics.service.ResultService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

@RequestMapping("/result")
@RestController
public class ResultController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${seq.tempFilePath}")
	private String tempFilePath;

	@Autowired
	private ResultService resultService;

	@GetMapping("/get")
	public Map<String, Object> getResult(@RequestParam Map<String, String> params) {
		return resultService.findResultByParams(params);
	}

	@PostMapping("/save")
	public Map<String, String> save(@RequestParam("file") MultipartFile multipartFile, MultipartHttpServletRequest request)
			throws InvalidFormatException, IOException {
		UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String memberId = userDetails.getUsername();
		String reportUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/report";
		
		return resultService.save(multipartFile, memberId, reportUrl);
	}

	@PostMapping("/delete")
	public Map<String, String> delete(@RequestBody List<Integer> ids) {
		return resultService.delete(ids);
	}

	@GetMapping("/download/pdf/{id}")
	public void downloadResultPdf(@PathVariable int id, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			Result result = resultService.findResultById(id);
			File file = new File(result.getFilePath() + "/Result.pdf");
			requestFile(file, "Result.pdf", request, response, false);
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
