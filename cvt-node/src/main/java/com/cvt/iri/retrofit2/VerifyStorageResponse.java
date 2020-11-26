package com.cvt.iri.retrofit2;

import com.cvt.iri.service.dto.AbstractResponse;

public class VerifyStorageResponse extends AbstractResponse {
	
	private Boolean verifySuccess;

	public static AbstractResponse create(Boolean verifySuccess) {
		VerifyStorageResponse res = new VerifyStorageResponse();
		res.verifySuccess = verifySuccess;
		return res;
	}

	public Boolean isVerifySuccess() {
		return verifySuccess;
	}
}
