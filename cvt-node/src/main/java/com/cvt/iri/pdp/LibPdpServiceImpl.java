package com.cvt.iri.pdp;

import org.apache.commons.lang3.StringUtils;

public class LibPdpServiceImpl implements PdpService {

	static final int KEY_PATH_NULL = -999;
	private String keypath = "";

	static {
		System.out.println(System.getProperty("java.library.path"));
		System.load("/root/git/libpdp/libpdp/com_cvt_io_pdp_PdpNative.so");

	}



}
