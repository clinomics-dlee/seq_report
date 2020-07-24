package com.clinomics.enums;

public enum StatusCode {
	S100_PDF_CREATING("PDF 생성중"),
	S110_PDF_CMPL("PDF 생성완료"),
	S120_PDF_FAIL("PDF 생성실패")
	;

	private final String value;
	
	StatusCode(String value) {
		this.value = value;
	}
	public String getKey() {
		return name();
	}
	public String getValue() {
		return value;
	}
}
