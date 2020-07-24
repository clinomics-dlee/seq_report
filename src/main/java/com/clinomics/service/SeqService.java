package com.clinomics.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.transaction.Transactional;

import com.clinomics.entity.seq.Report;
import com.clinomics.enums.ResultCode;
import com.clinomics.enums.StatusCode;
import com.clinomics.repository.seq.ReportRepository;
import com.clinomics.specification.seq.ReportSpecification;
import com.google.common.collect.Maps;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SeqService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${seq.tempFilePath}")
    private String tempFilePath;

    @Value("${seq.workspacePath}")
    private String workspacePath;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    DataTableService dataTableService;

    public Map<String, Object> findReportByParams(Map<String, String> params) {
        int draw = 1;
        // #. paging param
        int pageNumber = NumberUtils.toInt(params.get("pgNmb") + "", 0);
        int pageRowCount = NumberUtils.toInt(params.get("pgrwc") + "", 10);

        // #. paging 관련 객체
        Pageable pageable = Pageable.unpaged();
        if (pageRowCount > 1) {
            pageable = PageRequest.of(pageNumber, pageRowCount);
        }
        long total;

        Specification<Report> where = Specification.where(ReportSpecification.betweenDate(params))
                .and(ReportSpecification.keywordLike(params)).and(ReportSpecification.orderBy(params));

        total = reportRepository.count(where);
        Page<Report> page = reportRepository.findAll(where, pageable);
        List<Report> list = page.getContent();
        long filtered = total;

        return dataTableService.getDataTableMap(draw, pageNumber, total, filtered, list);
    }

    @Transactional
    public Map<String, String> save(MultipartFile multipartFile, String memberId) {
        Map<String, String> rtn = Maps.newHashMap();

        try {
            String now = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            String tempPath = tempFilePath + "/" + now;
            File srcDir = new File(tempPath);
            if (!srcDir.exists()) srcDir.mkdirs();
            // #. file 압축을 풀어서 statistics.out에서 정보 읽기
            File zipFile = new File(tempPath + "/" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(zipFile);
            
            this.upzipFile(zipFile);
            
            String outString = this.getOutString(new File(tempPath + "/statistics.out"));
            String[] lines = outString.split("\n");
            String[] values = lines[2].split("\t");
            String rawDataId = values[2];

            // #. workspace를 생성
            String filePath = workspacePath + "/" + rawDataId;
            File destDir = new File(filePath);
            if (!destDir.exists()) destDir.mkdirs();

            // #. 복사
            FileUtils.copyDirectory(srcDir, destDir);

            // #. 임시 폴더 삭제
            srcDir.delete();

            // #. save
            Report report = new Report();
            report.setSampleId(rawDataId);
            report.setFilePath(filePath);
            report.setStatusCode(StatusCode.S100_PDF_CREATING);
            reportRepository.save(report);

            rtn.put("result", ResultCode.SUCCESS.get());
            rtn.put("message", ResultCode.SUCCESS.getMsg());
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    private void upzipFile(File zipFile) {
        FileInputStream fis = null;
        ZipInputStream zis = null;
        ZipEntry entry = null;
        try {
            String zipPath = FilenameUtils.getFullPath(zipFile.getAbsolutePath());
            // 파일 스트림
            fis = new FileInputStream(zipFile);
            // Zip 파일 스트림
            zis = new ZipInputStream(fis);
            // entry가 없을때까지 뽑기
            while ((entry = zis.getNextEntry()) != null) {
                String filename = entry.getName();
                File file = new File(zipPath + "/" + filename);
                // entiry가 폴더면 폴더 생성
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // 파일이면 파일 만들기
                    this.createFile(file, zis);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (zis != null) zis.close();
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 파일 만들기 메소드
     * 
     * @param file 파일
     * @param zis  Zip스트림
     */
    private void createFile(File file, ZipInputStream zis) {
        // 디렉토리 확인
        File parentDir = new File(file.getParent());
        // 디렉토리가 없으면 생성하자
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        // 파일 스트림 선언
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[256];
            int size = 0;
            // Zip스트림으로부터 byte뽑아내기
            while ((size = zis.read(buffer)) > 0) {
                // byte로 파일 만들기
                fos.write(buffer, 0, size);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                logger.info("★★★★★★★★★★ outString=" + outString);
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
