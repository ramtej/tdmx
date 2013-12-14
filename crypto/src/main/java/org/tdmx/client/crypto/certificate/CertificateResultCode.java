package org.tdmx.client.crypto.certificate;

public enum CertificateResultCode {

	OK,
	
	ERROR_MISSING_CERTS,
	ERROR_TOO_MANY_CERTS,
	ERROR_FINGERPRINT_TAMPERING,
	
	ERROR_INVALID_KEY_SPEC,
	ERROR_MISSING_ALGORITHM,
	ERROR_KEYSTORE,
	
	ERROR_EXCEPTION,
	ERROR_ENCODING,
	ERROR_IO,
	
	
}
