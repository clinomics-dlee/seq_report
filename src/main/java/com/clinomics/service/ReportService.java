package com.clinomics.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    
    public Map<String, Object> getReportInfo(String filePath) {
        Map<String, Object> rtn = Maps.newHashMap();
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
        rtn.put("datas", datas);
        rtn.put("customer", customer);
        rtn.put("service", service);
        rtn.put("filePath", filePath);
        rtn.put("workspace", workspace);

        return rtn;
    }

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
    
    public String imageToBase64String(String content) {

		Pattern p = Pattern.compile("src=\"(.*?)\"");
		Matcher m = p.matcher(content);
		StringBuffer sb = new StringBuffer();

		while (m.find()) {

			String filePathName = m.group(1);
			if (filePathName.startsWith("data")) continue;
			String fileExtName = filePathName.substring(filePathName.lastIndexOf(".") + 1);

			FileInputStream inputStream = null;
			ByteArrayOutputStream byteOutStream = null;

            String replacement = "";
			try {
				File file = new File(filePathName);

				if (file.exists()) {
					inputStream = new FileInputStream(file);
					byteOutStream = new ByteArrayOutputStream();

					int len = 0;
					byte[] buf = new byte[1024];
					while ((len = inputStream.read(buf)) != -1) {
						byteOutStream.write(buf, 0, len);
					}

					byte[] fileArray = byteOutStream.toByteArray();
					String imageString = new String(Base64.encodeBase64(fileArray));

					replacement = "src=\"data:image/" + fileExtName + ";base64, " + imageString + "\"";
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (byteOutStream != null) {
                        byteOutStream.close();
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            m.appendReplacement(sb, replacement);
		}
		m.appendTail(sb);

		return sb.toString();
	}
}