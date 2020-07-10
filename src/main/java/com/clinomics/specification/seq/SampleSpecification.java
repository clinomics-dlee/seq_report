package com.clinomics.specification.seq;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.jpa.domain.Specification;

import com.clinomics.entity.seq.Sample;
import com.clinomics.enums.ChipTypeCode;
import com.clinomics.enums.StatusCode;
import com.google.common.collect.Lists;

public class SampleSpecification {

	public static Specification<Sample> createdDateOneMonth(Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			if (params.containsKey("yyyymm")) {
				List<Predicate> predicatesAnds = new ArrayList<>();
				String yyyymm = params.get("yyyymm") + "";

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime start = LocalDateTime.parse(yyyymm + "-01 00:00:00", formatter);
				LocalDateTime end = start.plusMonths(1).minusSeconds(1);

				predicatesAnds.add(criteriaBuilder.between(root.get("createdDate"), start, end));
				rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			}
			return rtn;

		};
	}

	public static Specification<Sample> modifiedDateOneMonth(Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			if (params.containsKey("yyyymm")) {
				List<Predicate> predicatesAnds = new ArrayList<>();
				String yyyymm = params.get("yyyymm") + "";

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime start = LocalDateTime.parse(yyyymm + "-01 00:00:00", formatter);
				LocalDateTime end = start.plusMonths(1).minusSeconds(1);

				predicatesAnds.add(criteriaBuilder.between(root.get("modifiedDate"), start, end));
				rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			}
			return rtn;

		};
	}

	public static Specification<Sample> customDateOneMonth(String field, Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			if (params.containsKey("yyyymm")) {
				List<Predicate> predicatesAnds = new ArrayList<>();
				String yyyymm = params.get("yyyymm") + "";

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime start = LocalDateTime.parse(yyyymm + "-01 00:00:00", formatter);
				LocalDateTime end = start.plusMonths(1).minusSeconds(1);

				predicatesAnds.add(criteriaBuilder.between(root.get(field), start, end));
				rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			}
			return rtn;

		};
	}

	public static Specification<Sample> customDateBetween(String field, Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			if (params.containsKey("sDate") && params.containsKey("fDate")) {
				List<Predicate> predicatesAnds = new ArrayList<>();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime start = LocalDateTime.parse(params.get("sDate") + " 00:00:00", formatter);
				LocalDateTime end = LocalDateTime.parse(params.get("fDate") + " 23:59:59", formatter);
				predicatesAnds.add(criteriaBuilder.between(root.get(field), start, end));
				rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			}
			return rtn;

		};
	}

	public static Specification<Sample> betweenDate(Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			if (params.containsKey("sDate") && params.containsKey("fDate")) {
				List<Predicate> predicatesAnds = new ArrayList<>();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime start = LocalDateTime.parse(params.get("sDate") + " 00:00:00", formatter);
				LocalDateTime end = LocalDateTime.parse(params.get("fDate") + " 23:59:59", formatter);
				predicatesAnds.add(criteriaBuilder.between(root.get("createdDate"), start, end));
				rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			}
			return rtn;

		};
	}

	public static Specification<Sample> betweenModifiedDate(Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			if (params.containsKey("sDate") && params.containsKey("fDate")) {
				List<Predicate> predicatesAnds = new ArrayList<>();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime start = LocalDateTime.parse(params.get("sDate") + " 00:00:00", formatter);
				LocalDateTime end = LocalDateTime.parse(params.get("fDate") + " 23:59:59", formatter);
				predicatesAnds.add(criteriaBuilder.between(root.get("modifiedDate"), start, end));
				rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			}
			return rtn;

		};
	}

	public static Specification<Sample> bundleId(Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			List<Predicate> predicatesAnds = new ArrayList<>();
			if (params.containsKey("bundleId") && !params.get("bundleId").toString().isEmpty()) {
				int bundleId = NumberUtils.toInt(params.get("bundleId") + "");
				predicatesAnds.add(criteriaBuilder.equal(root.get("bundle").get("id"), bundleId));
			}
			rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			return rtn;
		};
	}

	public static Specification<Sample> keywordLike(Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			List<Predicate> predicateLikes = new ArrayList<>();

			if (params.containsKey("keyword") && !params.get("keyword").toString().isEmpty()) {
				String text = "%" + params.get("keyword") + "%";
				predicateLikes.add(criteriaBuilder.like(criteriaBuilder.function("JSON_EXTRACT", String.class,
						root.get("items"), criteriaBuilder.literal("$.*")), text));

				predicateLikes.add(criteriaBuilder.like(root.get("laboratoryId"), text));
				predicateLikes.add(criteriaBuilder.like(root.get("bundle").get("name"), text));
				// predicates.add(criteriaBuilder.like(root.get("member"), "%" + text + "%"));

				rtn = criteriaBuilder.or(predicateLikes.toArray(new Predicate[predicateLikes.size()]));
			}

			return rtn;

		};
	}

	public static Specification<Sample> statusIn(List<StatusCode> statusCodes) {
		return (root, query, criteriaBuilder) -> {

			Predicate rtn = null;
			List<Predicate> predicatesAnds = new ArrayList<>();

			predicatesAnds.add(criteriaBuilder.and(root.get("statusCode").in(statusCodes)));

			rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			return rtn;
		};
	}

	public static Specification<Sample> statusNotIn(List<StatusCode> statusCodes) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			List<Predicate> predicatesAnds = new ArrayList<>();

			predicatesAnds.add(criteriaBuilder.not(root.get("statusCode").in(statusCodes)));

			rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			return rtn;
		};
	}

	public static Specification<Sample> statusEqual(StatusCode statusCode) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			List<Predicate> predicatesAnds = new ArrayList<>();

			predicatesAnds.add(criteriaBuilder.equal(root.get("statusCode"), statusCode));

			rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			return rtn;
		};
	}

	public static Specification<Sample> statusNotEqual(StatusCode statusCode) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			List<Predicate> predicatesAnds = new ArrayList<>();

			predicatesAnds.add(criteriaBuilder.notEqual(root.get("statusCode"), statusCode));

			rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			return rtn;
		};
	}

	public static Specification<Sample> bundleIsActive() {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			List<Predicate> predicatesAnds = new ArrayList<>();

			predicatesAnds.add(criteriaBuilder.isTrue(root.get("bundle").get("isActive")));
			rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));

			return rtn;
		};
	}

	public static Specification<Sample> laboratoryIdEqual(String laboratoryId) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = criteriaBuilder.equal(root.get("laboratoryId"), laboratoryId);
			return rtn;
		};
	}

	public static Specification<Sample> versionEqual(int version) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = criteriaBuilder.equal(root.get("version"), version);
			return rtn;
		};
	}

	public static Specification<Sample> isLastVersionTrue() {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = criteriaBuilder.isTrue(root.get("isLastVersion"));
			return rtn;
		};
	}

	public static Specification<Sample> mappingNoEqual(String mappingNo) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = criteriaBuilder.equal(root.get("mappingNo"), mappingNo);
			return rtn;
		};
	}

	public static Specification<Sample> mappingInfoGroupBy() {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = criteriaBuilder.isNotNull(root.get("mappingNo"));
			query.groupBy(root.get("mappingNo"), root.get("chipBarcode"), root.get("chipTypeCode"));
			return rtn;
		};
	}

	public static Specification<Sample> statusCodeGt(int number) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = criteriaBuilder.greaterThanOrEqualTo(
					criteriaBuilder.substring(root.get("statusCode"), 2, 3).as(Integer.class), number);
			return rtn;
		};
	}

	public static Specification<Sample> mappingInfoLike(Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			List<Predicate> predicateLikes = new ArrayList<>();

			if (params.containsKey("keyword") && !params.get("keyword").isEmpty()) {
				String text = "%" + params.get("keyword") + "%";

				predicateLikes.add(criteriaBuilder.like(root.get("mappingNo"), text));
				predicateLikes.add(criteriaBuilder.like(root.get("chipBarcode"), text));

				List<ChipTypeCode> codes = new ArrayList<ChipTypeCode>();
				for (ChipTypeCode code : ChipTypeCode.values()) {
					String value = code.getValue();
					if (value.toUpperCase().indexOf(params.get("keyword").trim().toUpperCase()) > -1) {
						codes.add(code);
					}
				}

				if (codes.size() > 0) {
					predicateLikes.add(criteriaBuilder.or(root.get("chipTypeCode").in(codes)));
				}

				rtn = criteriaBuilder.or(predicateLikes.toArray(new Predicate[predicateLikes.size()]));
			}

			return rtn;

		};
	}

	public static Specification<Sample> productNotLike(Map<String, String> params) {

		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			List<Predicate> predicateLikes = new ArrayList<>();

			if (params.containsKey("productType") && !params.get("productType").isEmpty()) {
				String text = "%_" + params.get("productType") + "_%";

				predicateLikes.add(criteriaBuilder.notLike(root.get("outputProductTypes"), text));
				predicateLikes.add(criteriaBuilder.isNull(root.get("outputProductTypes")));

				rtn = criteriaBuilder.or(predicateLikes.toArray(new Predicate[predicateLikes.size()]));
			}

			return rtn;

		};
	}

	public static Specification<Sample> checkCelFileEqual(String checkCelFile) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = criteriaBuilder.equal(root.get("checkCelFile"), checkCelFile);
			return rtn;
		};
	}

	public static Specification<Sample> chipBarcodeEqual(String chipBarcode) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = criteriaBuilder.equal(root.get("chipBarcode"), chipBarcode);
			return rtn;
		};
	}

	public static Specification<Sample> anlsCmplDatebetween(Map<String, String> params) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			if (params.containsKey("sDate") && params.containsKey("fDate")) {
				List<Predicate> predicatesAnds = new ArrayList<>();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				LocalDateTime start = LocalDateTime.parse(params.get("sDate") + " 00:00:00", formatter);
				LocalDateTime end = LocalDateTime.parse(params.get("fDate") + " 23:59:59", formatter);
				predicatesAnds.add(criteriaBuilder.between(root.get("anlsCmplDate"), start, end));
				query.orderBy(criteriaBuilder.asc(root.get("anlsCmplDate")));
				rtn = criteriaBuilder.and(predicatesAnds.toArray(new Predicate[predicatesAnds.size()]));
			}
			return rtn;

		};
	}

	public static Specification<Sample> orderBy(Map<String, String> params) {
		return (root, query, criteriaBuilder) -> {
			Predicate rtn = null;
			if (params.containsKey("order") && !params.get("order").isEmpty()) {
				List<Order> orderList = Lists.newArrayList();
				String order = params.get("order");

				List<String> sorting = Arrays.asList(order.substring(1).split(","));

				for (String sort : sorting) {

					String key = sort.replaceAll("^([\\S]+):([\\S]+)$", "$1");
					String type = sort.replaceAll("^([\\S]+):([\\S]+)$", "$2");

					Expression<?> expression = null;
					Order objQuery = null;

					if (key.contains("items")) {
						expression = criteriaBuilder.function(
							"JSON_EXTRACT"
							, String.class
							, root.get("items")
							, criteriaBuilder.literal("$." + key.replace("items.", ""))
						);
					} else {
						int dots = StringUtils.countMatches(key, ".");
						if (dots == 0) {
							expression = root.get(key);
						} else if (dots == 1) {
							String key1 = key.replaceAll("^([\\S]+)\\.([\\S]+)$", "$1");
							String key2 = key.replaceAll("^([\\S]+)\\.([\\S]+)$", "$2");
							expression = root.get(key1).get(key2);
						}
					}

					if ("asc".equals(type)) {
						objQuery = criteriaBuilder.asc(expression);					
					} else {
						objQuery = criteriaBuilder.desc(expression);
					}
					orderList.add(objQuery);
				}
				if (orderList.size() > 0) query.orderBy(orderList);
				
			} else {
				query.orderBy(criteriaBuilder.desc(root.get("laboratoryId")));
			}
			return rtn;
		
		};
	}
}
