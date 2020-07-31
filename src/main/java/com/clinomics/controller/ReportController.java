package com.clinomics.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.clinomics.service.setting.MemberService;
import com.google.common.collect.Maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReportController {

	@Autowired
	MemberService userService;

	@GetMapping("/report")
	public String getReport(@RequestParam Map<String, String> params, Model model) {
		String filePath = params.get("filePath");
		System.out.println("★★★★★★★★★★ filePath=" + filePath);

		String workspace = "/workspace" + filePath.substring(filePath.lastIndexOf("/"));
		String customer = "";
		String service = "";

		String outString = this.getOutString(new File(filePath + "/statistics.out"));

		String[] tableDatas = outString.split("\n\n");

		Map<String, Object> datas = Maps.newHashMap();
		for (int i = 0; i < tableDatas.length; i++) {
			String tableData = tableDatas[i];
			String[] rows = tableData.split("\n");

			String headerRow = rows[1];
			int headerCount = headerRow.split("\t").length;
			List<List<String>> tableRows = new ArrayList<List<String>>();
			for (int j = 2; j < rows.length; j++) {
				String row = rows[j];
				String[] valueArray = row.split("\t");

				List<String> values = new ArrayList<String>(Arrays.asList(valueArray));

				// #. header 보다 값의 수가 적은 경우 모자란 수 만큼 공백값을 추가
				if (headerCount > values.size()) {
					int diff = headerCount - values.size();
					for (int k = 0 ; k < diff; k++) {
						values.add("");
					}
				}

				// #. 첫번째 테이블에 첫번째 값과 마지막 값은 고정 셋팅
				if (i == 0 && j == 2) {
					customer = values.get(0);
					service = values.get(5);
				}

				tableRows.add(values);
			}
			datas.put("tableData" + i, tableRows);
		}

		model.addAttribute("datas", datas);
		model.addAttribute("customer", customer);
		model.addAttribute("service", service);
		model.addAttribute("filePath", filePath);
		model.addAttribute("workspace", workspace);

		return "report/print";
	}

	@GetMapping("/report/{page}")
	public String getReportPage(@PathVariable String page, Model model) {

		return "report/" + page;
	}

	// ############################ private
	private String getOutString(File outFile) {
		String outString = "";
        BufferedReader br = null;
		try {
            // #. read csv 데이터 파일
            br = new BufferedReader(new FileReader(outFile));
            String sLine = null;
            while((sLine = br.readLine()) != null) {
                outString += sLine + "\n";
            }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return outString;
	}
}
