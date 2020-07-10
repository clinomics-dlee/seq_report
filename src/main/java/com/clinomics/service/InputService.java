package com.clinomics.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.clinomics.entity.seq.Bundle;
import com.clinomics.entity.seq.Member;
import com.clinomics.entity.seq.Role;
import com.clinomics.entity.seq.Sample;
import com.clinomics.entity.seq.SampleHistory;
import com.clinomics.enums.ResultCode;
import com.clinomics.enums.RoleCode;
import com.clinomics.enums.StatusCode;
import com.clinomics.repository.seq.BundleRepository;
import com.clinomics.repository.seq.MemberRepository;
import com.clinomics.repository.seq.ProductRepository;
import com.clinomics.repository.seq.SampleHistoryRepository;
import com.clinomics.repository.seq.SampleRepository;
import com.clinomics.specification.seq.SampleSpecification;
import com.clinomics.util.CustomIndexPublisher;
import com.google.common.collect.Maps;

@Service
public class InputService {

	@Autowired
	SampleRepository sampleRepository;

	@Autowired
	SampleHistoryRepository sampleHistoryRepository;

	@Autowired
	BundleRepository bundleRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	MemberRepository memberRepository;
	
	@Autowired
	DataTableService dataTableService;

	@Autowired
	VariousFieldsService variousDayService;
	
	@Autowired
	SampleItemService sampleItemService;

	@Autowired
	CustomIndexPublisher customIndexPublisher;
	
	public Map<String, Object> findAll() {
		int draw = 1;
		long total = sampleRepository.count();
		List<Sample> list = sampleRepository.findAll();
		
		return dataTableService.getDataTableMap(draw, draw, total, total, list);
	}
	
	public Map<String, Object> find(Map<String, String> params, List<StatusCode> statusCodes) {
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
		
		Specification<Sample> where = Specification
					.where(SampleSpecification.betweenDate(params))
					.and(SampleSpecification.bundleId(params))
					.and(SampleSpecification.keywordLike(params))
					.and(SampleSpecification.bundleIsActive())
					.and(SampleSpecification.statusIn(statusCodes))
					.and(SampleSpecification.orderBy(params));
					
		
		total = sampleRepository.count(where);
		Page<Sample> page = sampleRepository.findAll(where, pageable);
		
		List<Sample> list = page.getContent();
		List<Map<String, Object>> header = sampleItemService.filterItemsAndOrdering(list);
		long filtered = total;
		
		return dataTableService.getDataTableMap(draw, pageNumber, total, filtered, list, header);
	}
	
	@Transactional
	public Map<String, String> save(Map<String, String> inputItems, boolean history) {
		Map<String, Object> items = Maps.newHashMap();
		items.putAll(inputItems);
		Map<String, String> rtn = Maps.newHashMap();
		
		String id = items.getOrDefault("id", "0") + "";
		
		Sample sample = searchExistsSample(NumberUtils.toInt(id));
		
		boolean existsSample = sample.getId() > 0;
		
		LocalDateTime now = LocalDateTime.now();
		Bundle bundle;
		if (existsSample) {
			if (!sampleRepository.existsById(NumberUtils.toInt(id)) && history) {
				saveSampleHistory(sample);
			}
			bundle = sample.getBundle();
			
		} else {
			if (!items.containsKey("bundleId")) {
				rtn.put("result", ResultCode.FAIL_NOT_EXISTS.get());
				return rtn;
			}
			int bundleId = NumberUtils.toInt(items.get("bundleId") + "");
			Optional<Bundle> oBundle = bundleRepository.findById(bundleId);
			bundle = oBundle.orElseThrow(NullPointerException::new);
			
			Optional<Member> oMember = memberRepository.findById(items.getOrDefault("memberId", "") + "");
			Member member = oMember.orElseThrow(NullPointerException::new);
			sample.setCreatedDate(now);
			sample.setModifiedDate(now);
			sample.setCreatedMember(member);
			sample.setStatusCode(StatusCode.S000_INPUT_REG);

			
		}
		
		items.remove("memberId");
		items.remove("bundleId");
		sample.setBundle(bundle);

		variousDayService.setFields(existsSample, sample, items);

		items.remove("id");
		
		Map<String, Object> newItems = Maps.newHashMap();
		newItems.putAll(items);
		sample.setItems(newItems);
		
		sampleRepository.save(sample);
		
		rtn.put("result", ResultCode.SUCCESS.get());
		rtn.put("message", ResultCode.SUCCESS.getMsg());
		return rtn;
	}
	
	public Map<String, String> saveFromList(List<Map<String, String>> list, String memberId) {
		Map<String, String> rtn = Maps.newHashMap();
		for (Map<String, String> l : list) {
			l.put("memberId", memberId);
			Map<String, String> tmp = this.save(l, false);
			if (!ResultCode.SUCCESS.get().equals(tmp.getOrDefault("result", "AA"))) {
				return tmp;
			}
			rtn.putAll(tmp);
		}
		return rtn;
	}

	@Transactional
	public Map<String, String> delete(List<Integer> ids) {
		Map<String, String> rtn = Maps.newHashMap();
		List<Sample> samples = sampleRepository.findByIdInAndStatusCodeIn(ids
			, Arrays.asList(new StatusCode[] { StatusCode.S020_INPUT_RCV, StatusCode.S000_INPUT_REG })
		);
		
		rtn.put("result", ResultCode.SUCCESS_DELETE.get());
		rtn.put("message", ResultCode.SUCCESS_DELETE.getMsg());

		
		sampleRepository.deleteAll(samples);
		
		return rtn;
	}

	@Transactional
	public Map<String, String> approve(List<Integer> ids, String memberId) {
		Map<String, String> rtn = Maps.newHashMap();
		
		// sample.set
		Optional<Member> oMember = memberRepository.findById(memberId);
		Member member = oMember.get();
		LocalDateTime now = LocalDateTime.now();
		String roles = "";
		for (Role r : member.getRole()) {
			roles += "," + r.getCode();
		}
		roles = roles.substring(1);

		List<StatusCode> status = new ArrayList<>();
		status.add(StatusCode.S020_INPUT_RCV);
		if (roles.contains(RoleCode.ROLE_INPUT_20.toString())) {
			status.add(StatusCode.S000_INPUT_REG);
		} 
		List<Sample> samples = sampleRepository.findByIdInAndStatusCodeIn(ids, status);

		rtn.put("result", ResultCode.SUCCESS_APPROVED.get());
		rtn.put("message", ResultCode.SUCCESS_APPROVED.getMsg());

		if (roles.contains(RoleCode.ROLE_EXP_80.toString())) {
			
			samples.stream().forEach(s -> {
				s.setInputDrctApproveDate(now);
				s.setInputDrctMember(member);
				s.setModifiedDate(now);
				if (s.getInputApproveDate() != null && s.getInputMngApproveDate() != null && s.getInputDrctApproveDate() != null) {
					s.setStatusCode(StatusCode.S200_EXP_READY);
				}
			});

		} else if (roles.contains(RoleCode.ROLE_INPUT_40.toString())) {
			
			samples.stream().forEach(s -> {
				s.setInputMngApproveDate(now);
				s.setInputMngApproveMember(member);
				s.setModifiedDate(now);
				if (s.getInputApproveDate() != null && s.getInputMngApproveDate() != null && s.getInputDrctApproveDate() != null) {
					s.setStatusCode(StatusCode.S200_EXP_READY);
				}
			});

		} else if (roles.contains(RoleCode.ROLE_INPUT_20.toString())) {
			
			samples.stream().forEach(s -> {
				s.setInputApproveDate(now);
				s.setInputApproveMember(member);
				s.setModifiedDate(now);
				s.setStatusCode(StatusCode.S020_INPUT_RCV);
			});
		} else {
			rtn.put("result", ResultCode.NO_PERMISSION.get());
			rtn.put("message", ResultCode.NO_PERMISSION.getMsg());
			return rtn;
		}
		sampleRepository.saveAll(samples);
		
		return rtn;
	}

	public Sample searchExistsSample(int id) {
		
		Optional<Sample> oSample = sampleRepository.findById(id);
		
		Sample news = new Sample();
		LocalDateTime now = LocalDateTime.now();
		news.setCreatedDate(now);
		Sample sample = oSample.orElse(news);
		sample.setModifiedDate(now);

		return sample;
	}
	
	private void saveSampleHistory(Sample smpl) {
		SampleHistory sh = new SampleHistory();
		sh.setSample(smpl);
		sh.setItems(smpl.getItems());
		sh.setMember(smpl.getCreatedMember());
		
		sampleHistoryRepository.save(sh);
	}
}
