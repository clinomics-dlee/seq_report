package com.clinomics.controller.menu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Map;

import com.clinomics.enums.ChipTypeCode;
import com.clinomics.enums.GenotypingMethodCode;
import com.clinomics.enums.StatusCode;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
 * @feature 기능별로 view 페이지 정렬
 * 
 * 
 */
@Controller
public class PageController {

	@Autowired
	ResourceLoader resourceLoader;

	@GetMapping()
	public String calendar(Model model) {
		Map<String, String> statusCodeMap = Maps.newLinkedHashMap();
		for (StatusCode statusCode : StatusCode.values()) {
			statusCodeMap.put(statusCode.getKey(), statusCode.getValue());
		}
		model.addAttribute("statusCodes", statusCodeMap);
		return "calendar";
	}

	@GetMapping("/chart")
	public String chart(Model model) {
		return "chart";
	}

	@GetMapping("/pdf/template")
	public String template(Model model) {
		try {
			String logoImageStr = this.getImageBase64String("classpath:static/assets/img/logo/logo_1.png");

			model.addAttribute("logoImageStr", logoImageStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "pdf/template";
	}

	@GetMapping("/p/{path1}/{path2}")
	public String intake(@PathVariable String path1, @PathVariable String path2, Model model) {

		Map<String, String> statusCodeMap = Maps.newHashMap();
		for (StatusCode statusCode : StatusCode.values()) {
			statusCodeMap.put(statusCode.getKey(), statusCode.getValue());
		}
		model.addAttribute("statusCodes", statusCodeMap);

		Map<String, String> genotypingMethodCodeMap = Maps.newHashMap();
		for (GenotypingMethodCode code : GenotypingMethodCode.values()) {
			genotypingMethodCodeMap.put(code.getKey(), code.getValue());
		}

		Map<String, String> chipTypeCodeMap = Maps.newHashMap();
		for (ChipTypeCode code : ChipTypeCode.values()) {
			chipTypeCodeMap.put(code.getKey(), code.getValue());
		}
		model.addAttribute("statusCodes", statusCodeMap);
		model.addAttribute("genotypingMethodCodes", genotypingMethodCodeMap);
		model.addAttribute("chipTypeCodes", chipTypeCodeMap);
		return path1 + "/" + path2;
	}

	private String getImageBase64String(String resourcePath) {
		String rtn = "";
		InputStream is = null;

		try {
			is = resourceLoader.getResource(resourcePath).getInputStream();
			String mimeType = URLConnection.guessContentTypeFromStream(is);
			byte[] imageBytes = ByteStreams.toByteArray(is);
			rtn = "data:" + mimeType + ";base64," + Base64.encodeBase64String(imageBytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return rtn;
	}
}