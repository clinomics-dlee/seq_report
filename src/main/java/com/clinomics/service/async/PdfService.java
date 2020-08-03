package com.clinomics.service.async;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PdfService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${seq.workspacePath}")
    private String workspacePath;

    @Value("${seq.nodePath}")
    private String nodePath;

    @Value("${seq.htmlToPdfPath}")
    private String htmlToPdfPath;

    @Async
    public void createPdf(String reportUrl, String filePath) {
        try {
            logger.info("★★★★★★★★★★★ start create pdf");
            // #. pdf생성 호출
            List<String> commands = new ArrayList<String>();
            commands.add(nodePath);
            commands.add(htmlToPdfPath);
            commands.add(reportUrl);
            commands.add(filePath);
            logger.info("★★★★★★★★★★★ commands=" + commands.toString());
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            // #. 명령어 실행 표준 및 오류 처리
            BufferedReader standardErrorBr = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder standardErrorSb = new StringBuilder();
            String lineString = null;
            while ((lineString = standardErrorBr.readLine()) != null) {
                standardErrorSb.append(lineString);
                standardErrorSb.append("<br>");
            }
            
            logger.info("★★★★★★★★★★★ standardErrorSb=" + standardErrorSb.toString());
            logger.info("★★★★★★★★★★★ end create pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
