package com.cvt.iri.pdp;

/**
 * PdpException
 *
 * @author admin
 * Time:
 */
public class PdpException extends RuntimeException {
	
	int ret=0;
	
	public PdpException(int ret) {
		super("pdp ret:"+ret+"!!!!");
	}
}
