/**
 * 
 */
package org.tdmx.client.crypto.scheme.none;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.algorithm.StreamCipherAlgorithm;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.converters.NumberToOctetString;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.CryptoResultCode;
import org.tdmx.client.crypto.scheme.Decrypter;
import org.tdmx.client.crypto.stream.SignatureVerifyingInputStream;

/**
 * decryption( PF, (K-B, K-b), (A-B, A-b), K-A, E, L ) -> M
 *{
 * E := AES256/CTR(SKe,IVe,ZLib-compress(M||Sign(K-a,M||long-byte-len(M))))
 * L := long-byte-len(M) || 
 * 	RSA/ECB/OAEPWithSHA1AndMGF1Padding-encrypt( K-B, SKe || IVe )
 *   where long-byte-len(M) is the length of M in bytes represented as 8-byte fixed length big-endian integer.
 * 
 * 
 * SKe || IVe:= RSA/ECB/OAEPWithSHA1AndMGF1Padding-decrypt( K-b, L )
 * 
 * M || Sign(K-a,M) := AES256/CTR-decrypt(SKe,IVe,ZLib-decompress(byte-len(M),E))
 *   where decompression fails if invalid stream or if decompressed length > long-byte-len(M) or stream ends before byte-len(M) bytes are decompressed.
 * verify(K-A, M, Sign(K-a,M)) and fail if signature incorrect.
 *}
 *
 * @author Peter
 *
 */
public class PGP_PayloadDecrypter implements Decrypter {

	private KeyPair ownSigningKey;
	private PublicKey otherSigningKey;
	private byte[] sessionKey;
	private byte[] passphrase;
	private StreamCipherAlgorithm payloadCipher;
	
	public PGP_PayloadDecrypter( KeyPair ownSigningKey, PublicKey otherSigningKey, byte[] passphrase, byte[] sessionKey, StreamCipherAlgorithm payloadCipher ) throws CryptoException {
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
		this.sessionKey = sessionKey;
		this.passphrase = passphrase;
		this.payloadCipher = payloadCipher;
	}

	/* (non-Javadoc)
	 * @see org.tdmx.client.crypto.scheme.Decrypter#getInputStream()
	 */
	@Override
	public InputStream getInputStream( InputStream encryptedData, byte[] encryptionContext ) throws CryptoException {
		if ( encryptionContext == null ) {
			throw new CryptoException(CryptoResultCode.ERROR_ENCRYPTION_CONTEXT_MISSING);
		}
		if ( encryptionContext.length < 8 ) {
			throw new CryptoException(CryptoResultCode.ERROR_ENCRYPTION_CONTEXT_INVALID);
			
		}
		byte[] lengthBytes = ByteArray.subArray(encryptionContext, 0, 8);
		long plaintextLength = NumberToOctetString.bytesToLong(lengthBytes);
		
		byte[] messageKeyBytes = ByteArray.subArray(encryptionContext, 8, encryptionContext.length - 8);
		AsymmetricEncryptionAlgorithm rsa = AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey( ownSigningKey.getPublic() );
		messageKeyBytes = rsa.decrypt(ownSigningKey.getPrivate(),messageKeyBytes);
		
		byte[] aesKey = ByteArray.subArray(messageKeyBytes, 0, payloadCipher.getKeyLength());
		byte[] aesIv = ByteArray.subArray(messageKeyBytes, payloadCipher.getKeyLength(), payloadCipher.getIvLength());
		
		SecretKeySpec secretKey = new SecretKeySpec(aesKey, payloadCipher.getAlgorithm());
		IvParameterSpec secretIv =  new IvParameterSpec(aesIv);
	
		System.out.println("PF KEY: " + ByteArray.asHex(aesKey));
		System.out.println("PF IV: " + ByteArray.asHex(aesIv));

		Cipher c = payloadCipher.getDecrypter(secretKey, secretIv);
		CipherInputStream cis = new CipherInputStream(encryptedData, c);
		
		InflaterInputStream zis = new InflaterInputStream(cis, new Inflater(false), 512);
		SignatureVerifyingInputStream sis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_384_RSA, otherSigningKey, plaintextLength, true, zis);
		
		return sis;
	}
	
}
