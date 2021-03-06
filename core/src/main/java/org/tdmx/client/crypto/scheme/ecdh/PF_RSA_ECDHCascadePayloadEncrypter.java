package org.tdmx.client.crypto.scheme.ecdh;

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
import org.tdmx.client.crypto.algorithm.DigestAlgorithm;
import org.tdmx.client.crypto.algorithm.KeyAgreementAlgorithm;
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
 * validate A-B must be a 384bit X.509 encoded EC sessionKey.
 * EC key generate (A-A,A-a), an EC keypair on secp384r1
 * ECDH key agreement (A-a,A-B) => shared secret S
 * 
 * PFS := SHA384(PF) - convert the PF into a shared secret.
 * ECS := SHA384(A-B||S||PFS) 
 * RS := PRNG(384bit)
 * 
 * CSKM := bytewise interleave ECS + RS in that order
 * 
 * SKk-aes || IVk-aes := first 384 bits of CSKM 
 * SKk-twofish || IVk-twofish := second 384s bit of CSKM 
 * 
 * E := AES256/CTR(SKe-aes,IVe-aes,
 *            Twofish256/CTR(SKe-twofish, IVe-twofish
 * 		ZLib-compress(M||Sign(K-a,M||long-byte-len(M)))
 * 
 * L := long-byte-len(M) 
 * RSA/ECB/OAEPWithSHA1AndMGF1Padding-encrypt( K-B, RS || A-A )
 *   where long-byte-len(M) is the length of M in bytes represented as 8-byte fixed length big-endian integer.
 *   A-A is a X.509 encoded EC public key - aka the sender’s messageKey
 * }
 * 
 * @author Peter
 *
 */
public class PF_RSA_ECDHCascadePayloadEncrypter implements Encrypter {

	private TemporaryBufferFactory bufferFactory;
	
	private KeyPair ownSigningKey;
	private PublicKey otherSigningKey;
	private PublicKey sessionKey;
	private KeyPair messageKey;
	
	private SecretKeySpec innerKey;
	private IvParameterSpec innerIv;
	private SecretKeySpec outerKey;
	private IvParameterSpec outerIv;
	private byte[] rs = null;
	
	private FileBackedOutputStream fbos = null;
	private SigningOutputStream sos = null;
	
	private StreamCipherAlgorithm innerPayloadCipher;
	private StreamCipherAlgorithm outerPayloadCipher;
	
	public PF_RSA_ECDHCascadePayloadEncrypter( KeyPair ownSigningKey, PublicKey otherSigningKey, byte[] passphrase, byte[] encodedSessionKey, TemporaryBufferFactory bufferFactory, StreamCipherAlgorithm innerPayloadCipher, StreamCipherAlgorithm outerPayloadCipher ) throws CryptoException {
		this.bufferFactory = bufferFactory;
		
		this.ownSigningKey = ownSigningKey;
		this.otherSigningKey = otherSigningKey;
	
		this.innerPayloadCipher = innerPayloadCipher;
		this.outerPayloadCipher = outerPayloadCipher;
		
		this.sessionKey = KeyAgreementAlgorithm.ECDH384.decodeX509EncodedKey(encodedSessionKey);
		this.messageKey = KeyAgreementAlgorithm.ECDH384.generateNewKeyPair();

		byte[] sharedSecret = KeyAgreementAlgorithm.ECDH384.agreeKey(messageKey, sessionKey);
		
		byte[] passphraseSecret = DigestAlgorithm.SHA_384.kdf(passphrase);
		this.rs = EntropySource.getRandomBytes(48);
		
		byte[] kdf = DigestAlgorithm.SHA_384.kdf(ByteArray.append(encodedSessionKey,sharedSecret,passphraseSecret));

		byte[] combinedKey = ByteArray.interleave(kdf, rs);
		
		byte[] iKey = ByteArray.subArray(combinedKey, 0, innerPayloadCipher.getKeyLength());
		byte[] iIv = ByteArray.subArray(combinedKey, innerPayloadCipher.getKeyLength(), innerPayloadCipher.getIvLength());
		innerKey = new SecretKeySpec(iKey, innerPayloadCipher.getAlgorithm());
		innerIv =  new IvParameterSpec(iIv);
		
		System.out.println("inner KEY: " + ByteArray.asHex(iKey));
		System.out.println("inner IV: " + ByteArray.asHex(iIv));

		byte[] oKey = ByteArray.subArray(combinedKey, innerPayloadCipher.getKeyLength()+innerPayloadCipher.getIvLength(), outerPayloadCipher.getKeyLength());
		byte[] oIv = ByteArray.subArray(combinedKey, innerPayloadCipher.getKeyLength()+innerPayloadCipher.getIvLength()+outerPayloadCipher.getKeyLength(), outerPayloadCipher.getIvLength());
		outerKey = new SecretKeySpec(oKey, outerPayloadCipher.getAlgorithm());
		outerIv =  new IvParameterSpec(oIv);
		
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
		Cipher ic = innerPayloadCipher.getEncrypter(innerKey, innerIv);
		CipherOutputStream icos = new CipherOutputStream(fbos, ic);
		
		Cipher oc = outerPayloadCipher.getEncrypter(outerKey, outerIv);
		CipherOutputStream ocos = new CipherOutputStream(icos, oc);

		DeflaterOutputStream zos = new DeflaterOutputStream(ocos, new Deflater(Deflater.DEFAULT_COMPRESSION, false), 512, false);
		
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
		byte[] msgKey = KeyAgreementAlgorithm.ECDH384.encodeX509PublicKey(messageKey.getPublic());
		byte[] plaintextLengthBytes = NumberToOctetString.longToBytes(sos.getSize());
		
		AsymmetricEncryptionAlgorithm rsa = AsymmetricEncryptionAlgorithm.getAlgorithmMatchingKey( otherSigningKey );
		byte[] encryptedContext = rsa.encrypt(otherSigningKey, ByteArray.append(rs,msgKey));
		byte[] encryptionContext = ByteArray.append(plaintextLengthBytes, encryptedContext);

		CryptoContext cc = new CryptoContext(fbos.getInputStream(), encryptionContext, sos.getSize(), fbos.getSize());
		return cc;
	}


}
