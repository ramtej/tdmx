package org.tdmx.client.crypto.scheme;

public enum CryptoResultCode {

	OK,
	
	UNKNOWN_CRYPTOSCHEME_NAME,
	ERROR_ENCRYPTION_CONTEXT_MISSING,
	ERROR_ENCRYPTION_CONTEXT_INVALID,
	
	ERROR_KA_ALGORITHM_MISSING,
	ERROR_KA_ALGORITHM_PARAMETER_INVALID,
	ERROR_KA_PUBLIC_KEY_SPEC_INVALID,
	ERROR_KA_PRIVATE_KEY_SPEC_INVALID,
	ERROR_KA_SHARED_SECRET_KEY_INVALID,
	
	ERROR_PK_ALGORITHM_MISSING,
	ERROR_PK_ALGORITHM_MISMATCH,
	ERROR_PK_PRIVATE_KEY_SPEC_INVALID,
	ERROR_PK_PUBLIC_KEY_SPEC_INVALID,
	ERROR_PK_PADDING_MISSING,
	ERROR_PK_KEY_INVALID,
	ERROR_PK_BLOCKSIZE_INVALID,
	ERROR_PK_PADDING_INVALID,
	
	ERROR_SK_ALGORITHM_MISSING,
	ERROR_SK_PADDING_MISSING,
	ERROR_SK_KEY_INVALID,
	ERROR_SK_IV_INVALID,
	ERROR_SK_ALGORITHM_PARAMETER_INVALID,
	ERROR_SK_BLOCKSIZE_INVALID,
	ERROR_SK_PADDING_INVALID,
	
	ERROR_PBE_ALGORITHM_MISSING,
	ERROR_PBE_KEY_INVALID,

	ERROR_SIGNATURE_ALGORITHM_MISSING,
	ERROR_SIGNATURE_KEY_INVALID,
	ERROR_SIGNATURE_VALIDATION_KEY_INVALID,
	
	ERROR_DIGEST_ALGORITHM_MISSING,
	
	ERROR_ENCODING_MISSING,
	ERROR_ENCODED_KEY_FORMAT_INVALID,
	
	ERROR;
	
	
}