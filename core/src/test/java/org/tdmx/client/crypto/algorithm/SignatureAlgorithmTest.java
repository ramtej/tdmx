package org.tdmx.client.crypto.algorithm;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.security.Signature;
import java.security.SignatureException;

import org.junit.Test;

import org.tdmx.client.crypto.JCAProviderInitializer;
import org.tdmx.client.crypto.scheme.CryptoException;

public class SignatureAlgorithmTest {

	static {
		JCAProviderInitializer.init();	
	}
	
	@Test
	public void testOkRsa2048Signature() throws CryptoException, SignatureException {
		KeyPair kp = PublicKeyAlgorithm.RSA2048.generateNewKeyPair();
		testSignature(kp,SignatureAlgorithm.SHA_1_RSA);
		testSignature(kp,SignatureAlgorithm.SHA_256_RSA);
		testSignature(kp,SignatureAlgorithm.SHA_384_RSA);
		testSignature(kp,SignatureAlgorithm.SHA_512_RSA);
	}
	
	@Test
	public void testOkRsa4096Signature() throws CryptoException, SignatureException {
		KeyPair kp = PublicKeyAlgorithm.RSA4096.generateNewKeyPair();
		testSignature(kp,SignatureAlgorithm.SHA_1_RSA);
		testSignature(kp,SignatureAlgorithm.SHA_256_RSA);
		testSignature(kp,SignatureAlgorithm.SHA_384_RSA);
		testSignature(kp,SignatureAlgorithm.SHA_512_RSA);
	}
	
	@Test
	public void testOkEcdsa384Signature() throws CryptoException, SignatureException {
		KeyPair kp = PublicKeyAlgorithm.ECDSA384.generateNewKeyPair();
		testSignature(kp,SignatureAlgorithm.SHA_1_ECDSA);
		testSignature(kp,SignatureAlgorithm.SHA_256_ECDSA);
		testSignature(kp,SignatureAlgorithm.SHA_384_ECDSA);
		testSignature(kp,SignatureAlgorithm.SHA_512_ECDSA);
	}
	
	@Test
	public void testOkEcdsa256Signature() throws CryptoException, SignatureException {
		KeyPair kp = PublicKeyAlgorithm.ECDSA256.generateNewKeyPair();
		testSignature(kp,SignatureAlgorithm.SHA_1_ECDSA);
		testSignature(kp,SignatureAlgorithm.SHA_256_ECDSA);
		testSignature(kp,SignatureAlgorithm.SHA_384_ECDSA);
		testSignature(kp,SignatureAlgorithm.SHA_512_ECDSA);
	}
	
	private void testSignature(KeyPair kp, SignatureAlgorithm alg) throws CryptoException, SignatureException {
		Signature signature = alg.getSignature(kp.getPrivate());
		//Read the string into a buffer  
        String data = "{\n" +  
                      "  \"schemas\":[\"urn:scim:schemas:core:1.0\"],\n" +  
                      "  \"userName\":\"bjensen\",\n" +  
                      "  \"externalId\":\"bjensen\",\n" +  
                      "  \"name\":{\n" +  
                      "    \"formatted\":\"Ms. Barbara J Jensen III\",\n" +  
                      "    \"familyName\":\"Jensen\",\n" +  
                      "    \"givenName\":\"Barbara\"\n" +  
                      "  }\n" +  
                      "}";  

        byte[] dataInBytes = data.getBytes();  

        //update signature with data to be signed  
        signature.update(dataInBytes);  

        //sign the data  
        byte[] signedInfo = signature.sign(); 
        
        System.out.println("Alg: " + alg + " signature len "+ signedInfo.length);
        
        Signature verifier = alg.getVerifier(kp.getPublic());
        verifier.update(dataInBytes);
        assertTrue( verifier.verify(signedInfo) );
        assertTrue( signedInfo.length <= SignatureAlgorithm.getMaxSignatureSizeBytes(signature, kp.getPublic()) );
	}

	
}
