package com.clinomics.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.clinomics.entity.seq.Result;
import com.clinomics.enums.StatusCode;
import com.clinomics.repository.seq.ResultRepository;
import com.clinomics.specification.seq.ResultSpecification;
import com.google.common.collect.Maps;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CalendarService {

	@Autowired
	ResultRepository resultRepository;

	@PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    DataTableService dataTableService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public Map<String, Object> selectCountByMonthly(Map<String, String> params) {
		List<Result> sample1 = resultRepository.findAll(getRegisteredWhere(params));
		List<Result> sample2 = resultRepository.findAll(getRunningWhere(params));
		List<Result> sample3 = resultRepository.findAll(getCompletedWhere(params));
		
		List<Map<String, String>> mapResult = sample1.stream()
			.map(s -> {
				Map<String, String> t = Maps.newHashMap();
				LocalDateTime cd = s.getCreatedDate();
				t.put("day", (cd == null ? "" : cd.getDayOfMonth()) + "");
				return t;
			}).collect(Collectors.toList());
		
		List<Map<String, String>> mapRunning = sample2.stream()
			.filter(s -> StatusCode.S100_PDF_CREATING == s.getStatusCode())
			.map(s -> {
				Map<String, String> t = Maps.newHashMap();
				LocalDateTime md = s.getCreatedDate();
				t.put("day", (md != null ? md.getDayOfMonth() : "") + "");
				return t;
			}).collect(Collectors.toList());
		
		List<Map<String, String>> mapComplete = sample3.stream()
			.filter(s -> StatusCode.S110_PDF_CMPL == s.getStatusCode())
			.map(s -> {
				Map<String, String> t = Maps.newHashMap();
				LocalDateTime md = s.getCreatedDate();
				t.put("day", (md != null ? md.getDayOfMonth() : "") + "");
				return t;
			}).collect(Collectors.toList());
		
		Map<String, Long> groupbyResult = getGroupingMap(mapResult, "day");
		Map<String, Long> groupbyAnalysis = getGroupingMap(mapRunning, "day");
		Map<String, Long> groupbyComplete = getGroupingMap(mapComplete, "day");
		
        Map<String, Object> rtn = Maps.newHashMap();
        rtn.put("register", groupbyResult);
        rtn.put("running", groupbyAnalysis);
        rtn.put("complete", groupbyComplete);
        
		return rtn;
		
    }
    
    public Map<String, Object> selectRegistered(Map<String, String> params) {
		int draw = 1;
		// #. paging param
		int pageNumber = NumberUtils.toInt(params.get("pgNmb") + "", 0);
		int pageRowCount = NumberUtils.toInt(params.get("pgrwc") + "", 10);

		List<Order> orders = Arrays.asList(new Order[] { Order.asc("id") });
		// #. paging 관련 객체
		Pageable pageable = Pageable.unpaged();
		if (pageRowCount > 1) {
			pageable = PageRequest.of(pageNumber, pageRowCount, Sort.by(orders));
		}
		
		Specification<Result> where = getRegisteredWhere(params);
		Page<Result> result = resultRepository.findAll(where, pageable);
		long total = result.getTotalElements();
		List<Result> list = result.getContent();
		long filtered = total;
		
		return dataTableService.getDataTableMap(draw, pageNumber, total, filtered, list);
    }
    
    public Map<String, Object> selectRunning(Map<String, String> params) {
		int draw = 1;
		// #. paging param
		int pageNumber = NumberUtils.toInt(params.get("pgNmb") + "", 0);
		int pageRowCount = NumberUtils.toInt(params.get("pgrwc") + "", 10);

		List<Order> orders = Arrays.asList(new Order[] { Order.asc("id") });
		// #. paging 관련 객체
		Pageable pageable = Pageable.unpaged();
		if (pageRowCount > 1) {
			pageable = PageRequest.of(pageNumber, pageRowCount, Sort.by(orders));
		}
		
		Specification<Result> where = getRunningWhere(params);
		Page<Result> result = resultRepository.findAll(where, pageable);
		long total = result.getTotalElements();
		List<Result> list = result.getContent();
		long filtered = total;
		
		return dataTableService.getDataTableMap(draw, pageNumber, total, filtered, list);
    }
    
    public Map<String, Object> selectCompleted(Map<String, String> params) {
		int draw = 1;
		// #. paging param
		int pageNumber = NumberUtils.toInt(params.get("pgNmb") + "", 0);
		int pageRowCount = NumberUtils.toInt(params.get("pgrwc") + "", 10);

		List<Order> orders = Arrays.asList(new Order[] { Order.asc("id") });
		// #. paging 관련 객체
		Pageable pageable = Pageable.unpaged();
		if (pageRowCount > 1) {
			pageable = PageRequest.of(pageNumber, pageRowCount, Sort.by(orders));
		}
		
		Specification<Result> where = getCompletedWhere(params);
		Page<Result> result = resultRepository.findAll(where, pageable);
		long total = result.getTotalElements();
		List<Result> list = result.getContent();
		long filtered = total;
		
		return dataTableService.getDataTableMap(draw, pageNumber, total, filtered, list);
	}
	
    // ############################# private 

	private Specification<Result> getCreateDateWhere(Map<String, String> params) {
		if (params.containsKey("yyyymm")) {
			return ResultSpecification.createdDateOneMonth(params);
		} else {
			return ResultSpecification.betweenDate(params);
		}
    }
    
    private Specification<Result> getRegisteredWhere(Map<String, String> params) {
		return Specification.where(getCreateDateWhere(params));
	}

	private Specification<Result> getRunningWhere(Map<String, String> params) {
		return Specification
			.where(getCreateDateWhere(params))
			.and(ResultSpecification.statusEqual(StatusCode.S100_PDF_CREATING));
	}
	
	private Specification<Result> getCompletedWhere(Map<String, String> params) {
		return Specification
			.where(getCreateDateWhere(params))
			.and(ResultSpecification.statusEqual(StatusCode.S110_PDF_CMPL));
	}
	
	private Map<String, Long> getGroupingMap(List<Map<String, String>> map, String key) {
		return map.stream().collect(
			Collectors.groupingBy(m -> m.get(key), Collectors.counting())
		);
	}
	
}
