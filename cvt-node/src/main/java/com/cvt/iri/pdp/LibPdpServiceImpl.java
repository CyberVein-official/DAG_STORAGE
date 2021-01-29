package com.cvt.iri.pdp;

import org.apache.commons.lang3.StringUtils;

public class LibPdpServiceImpl implements PdpService {

	static final int KEY_PATH_NULL = -999;
	private String keypath = "";

	static {
		System.out.println(System.getProperty("java.library.path"));
		System.load("/root/git/libpdp/libpdp/com_cvt_io_pdp_PdpNative.so");

	}

	public LibPdpServiceImpl(String keypath) {
		this.keypath = keypath;
	}

	@Override
	public void storeFile(String filePath) throws PdpException {
		if(StringUtils.isBlank(this.keypath)) {
			throw new PdpException(KEY_PATH_NULL);
		}
		int ret;
		if ((ret = PDPstoreFile(filePath, keypath)) != 0) {
			throw new PdpException(ret);
		}
	}

	@Override
	public void challenge(String filePath) throws PdpException {
		if(StringUtils.isBlank(this.keypath)) {
			throw new PdpException(KEY_PATH_NULL);
		}
		int ret;
		if ((ret = PDPchallenge(filePath, keypath)) != 0) {
			throw new PdpException(ret);
		}
	}


	@Override
	public void prove(String filePath) throws PdpException {
		if(StringUtils.isBlank(this.keypath)) {
			throw new PdpException(KEY_PATH_NULL);
		}
		int ret;
		if ((ret = PDPproof(filePath, keypath)) != 0) {
			throw new PdpException(ret);
		}
	}

	private native int PDPstoreFile(String filename, String keypath);

	private native int PDPchallenge(String filename, String keypath);

	private native int PDPproof(String filename, String keypath);

}
