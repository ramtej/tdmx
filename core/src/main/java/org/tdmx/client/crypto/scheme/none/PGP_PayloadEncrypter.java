package org.tdmx.client.crypto.scheme.none;

import java.io.OutputStream;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.algorithm.StreamCipherAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryBufferFactory;
import org.tdmx.client.crypto.converters.ByteArray;
import org.tdmx.client.crypto.converters.NumberToOctetString;
import org.tdmx.client.crypto.entropy.EntropySource;
import org.tdmx.client.crypto.scheme.CryptoContext;
import org.tdmx.client.crypto.scheme.CryptoException;
import org.tdmx.client.crypto.scheme.Encrypter;
import org.tdmx.client.crypto.stream.FileBackedOutputStream;
import org.tdmx.client.crypto.stream.SigningOutputStream;

/**
 * encryption( M, PF, (K-A,K-a), K-B, A-B ) -> E, L
 * {
 * A-B is not used.
 * PF is not used.
 * 
 * SKe := PRNG(32-byte)
 * IVe:=  PRNG(16-byte)
 *   where SKe is a 256bit AES encryption key, 
 *   IVe is a 128bit initialization vector for the AES encryption
 * E := AES256/CTR(SKe,IVe,ZLib-compress(M||Sign(K-a,M||long-byte-len(M))))
 * L := long-byte-len(M) || 
 * RSA/ECB/OAEPWithSHA1AndMGF1Padding-encrypt( K-B, SKe || IVe )
 *   where long-byte-len(M) is the length of M in bytes represented as 8-byte fixed length big-endian integer.
 * }
 * 
 * @author Peter
 *
 */
public class PGP_PayloadEncrypter implements Encrypter {

	private TemporaryBufferFactory bufferFactory;
	
	private KeyPair ownSigningKey;
	private PublicKey otherSigningKey;
	
	private byte[] messageKey;
	private SecretKeySpec secretKey;
	private IvParameterSpec secretIv;
	
	private FileBackedOutputStream fbos = null;
	private SigningOutputStream sos = null;
	
	private StreamCipherAlgorithm payloadCipher;
	
	public PGP_PayloadEncrypter( KeyPair ownSigningKey, PublicKey otherSigningKey, byte[] passphrase, byte[] encodedSessionKey, TemporaryBufferFactory bufferFactory, StreamCipherAlgorithm payloadCipher ) throws CryptoException {
		this.bufferFactory = bufferFactory;
		
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
	
		this.payloadCipher = payloadCipher;
		
		byte[] aesKey = EntropySource.getRandomBytes(payloadCipher.getKeyLength());
		byte[] aesIv = EntropySource.getRandomBytes(payloadCipher.getIvLength());
		
		messageKey = ByteArray.append(aesKey, aesIv);
		
		secretKey = new SecretKeySpec(aesKey, payloadCipher.getAlgorithm());
		secretIv =  new IvParameterSpec(aesIv);
		
		System.out.println("PGP KEY: " + ByteArray.asHex(aesKey));
		System.out.println("PGP IV: " + ByteArray.asHex(aesIv));
	}
	
	
	/* (non-Javadoc)
	 * @see org.tdmx.client.crypto.scheme.Encrypter#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws CryptoException {
		if ( fbos != null ) {
			throw new IllegalStateException();
		}
		fbos = bufferFactory.getOutputStream();
		Cipher c = payloadCipher.getEncrypter(secretKey, secretIv);
		CipherOutputStream cos = new CipherOutputStream(fbos, c);
		
		DeflaterOutputStream zos = new DeflaterOutputStream(cos, new Deflater(Deflater.DEFAULT_COMPRESSION, false), 512, false);
		
		sos = new SigningOutputStream(SignatureAlgorithm.SHA_384_RSA, ownSigningKey.getPrivate(), true, true, zos);
		return sos;
	}


	@Override
	public CryptoContext getResult() throws CryptoException {
		if ( fbos == null ) {
			throw new IllegalStateException();
		}
		if ( !fbos.isClosed() ) {
			throw new IllegalStateException();
		}
		byte[] plaintextLengthBytes = NumberToOctetString.longToBytes(sos.getSize());
		
		AsymmetricEncryptionAlgorithm rsa = AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey( otherSigningKey );
		byte[] encryptedMsgKey = rsa.encrypt(otherSigningKey, messageKey);
		byte[] encryptionContext = ByteArray.append(plaintextLengthBytes, encryptedMsgKey);
		CryptoContext cc = new CryptoContext(fbos.getInputStream(), encryptionContext, sos.getSize(), fbos.getSize());
		return cc;
	}


}
