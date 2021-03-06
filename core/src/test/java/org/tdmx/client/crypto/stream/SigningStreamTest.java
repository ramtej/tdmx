package org.tdmx.client.crypto.stream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.Signature;
import java.security.SignatureException;

import org.junit.Before;
import org.junit.Test;

import org.tdmx.client.crypto.algorithm.AsymmetricEncryptionAlgorithm;
import org.tdmx.client.crypto.algorithm.SignatureAlgorithm;
import org.tdmx.client.crypto.buffer.TemporaryBufferFactory;
import org.tdmx.client.crypto.buffer.TemporaryFileManagerImpl;
import org.tdmx.client.crypto.converters.NumberToOctetString;
import org.tdmx.client.crypto.scheme.CryptoException;

public class SigningStreamTest {

	TemporaryBufferFactory factory = new TemporaryFileManagerImpl(); 
	KeyPair kp = null;
	
	@Before
	public void setUp() throws Exception {
		kp = AsymmetricEncryptionAlgorithm.RSA2048.generateNewKeyPair();
	}

	@Test
	public void testPlainSign_ArrayWritesInclLength_OutputSignature() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		Signature check = SignatureAlgorithm.SHA_1_RSA.getSignature(kp.getPrivate());
		
		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), true, true, fbos);
			
			byte[] content = StreamUtils.createRndArray(chunklen);
			StreamUtils.writeArrayAsArray(sos, content, reps); // test
			StreamUtils.signArray(check, content, reps); // check
			
			sos.close();
			assertTrue(fbos.getSize() > reps*chunklen); // +signature length which for RSA is dep. on the RSA key length in bytes.

			byte[] sizeBytes = NumberToOctetString.longToBytes(sos.getSize());
			check.update(sizeBytes);
			assertArrayEquals(sos.getSignatureValue(), check.sign());
			
		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainSign_ArrayWritesInclLength() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		Signature check = SignatureAlgorithm.SHA_1_RSA.getSignature(kp.getPrivate());
		
		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), false, true, fbos);
			
			byte[] content = StreamUtils.createRndArray(chunklen);
			StreamUtils.writeArrayAsArray(sos, content, reps); // test
			StreamUtils.signArray(check, content, reps); // check
			
			sos.close();
			assertEquals(reps*chunklen, fbos.getSize());

			byte[] sizeBytes = NumberToOctetString.longToBytes(sos.getSize());
			check.update(sizeBytes);
			assertArrayEquals(sos.getSignatureValue(), check.sign());
			
		} finally {
			fbos.discard();
		}
	}

	
	@Test
	public void testPlainSign_ArrayWrites() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		Signature check = SignatureAlgorithm.SHA_1_RSA.getSignature(kp.getPrivate());
		
		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), false, false, fbos);
			
			byte[] content = StreamUtils.createRndArray(chunklen);
			StreamUtils.writeArrayAsArray(sos, content, reps); // test
			StreamUtils.signArray(check, content, reps); // check
			
			sos.close();
			assertEquals(reps*chunklen, fbos.getSize());
			assertArrayEquals(sos.getSignatureValue(), check.sign());
			
		} finally {
			fbos.discard();
		}
	}


	@Test
	public void testPlainVerify_SingleArray() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), false, false, fbos);
			
			byte[] content = StreamUtils.createRndArray(chunklen);
			sos.write(content);
			
			sos.close();
			assertEquals(chunklen, fbos.getSize());
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();
			
			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPublic(), contentLen, false, signatureValue, fbis);
			byte[] buffer = new byte[content.length];
			StreamUtils.readArrayAsArray(svis, buffer, content, 1);
			assertEquals( -1, svis.read() );
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals( signatureValue, svis.getSignatureValue() );
			
		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainVerify_SingleArray_AppendedExclLength() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), true, false, fbos);
			
			byte[] content = StreamUtils.createRndArray(chunklen);
			sos.write(content);
			
			sos.close();
			assertTrue(fbos.getSize() > chunklen);
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();
			
			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPublic(), contentLen, false, fbis);
			byte[] buffer = new byte[content.length];
			StreamUtils.readArrayAsArray(svis, buffer, content, 1);
			assertEquals( -1, svis.read() );
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals( signatureValue, svis.getSignatureValue() );
			
		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainVerify_SingleArray_AppendedInclLength() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), true, true, fbos);
			
			byte[] content = StreamUtils.createRndArray(chunklen);
			sos.write(content);
			
			sos.close();
			assertTrue(fbos.getSize() > chunklen);
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();
			
			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPublic(), contentLen, true, fbis);
			byte[] buffer = new byte[content.length];
			StreamUtils.readArrayAsArray(svis, buffer, content, 1);
			assertEquals( -1, svis.read() );
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals( signatureValue, svis.getSignatureValue() );
			
		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainVerify_ArrayWrites() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), false, false, fbos);
			
			byte[] content = StreamUtils.createRndArray(chunklen);
			StreamUtils.writeArrayAsArray(sos, content, reps); // test
			
			sos.close();
			assertEquals(reps*chunklen, fbos.getSize());
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();
			
			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPublic(), contentLen, false, signatureValue, fbis);
			byte[] buffer = new byte[content.length];
			StreamUtils.readArrayAsArray(svis, buffer, content, reps);
			assertEquals( -1, svis.read() );
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals( signatureValue, svis.getSignatureValue() );
			
		} finally {
			fbos.discard();
		}
	}

	@Test
	public void testPlainVerify_ArrayWrites_AppendedInclLength() throws CryptoException, IOException, SignatureException {
		FileBackedOutputStream fbos = factory.getOutputStream();
		try {
			int reps = 1024;
			int chunklen = 2048;
			SigningOutputStream sos = new SigningOutputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPrivate(), true, true, fbos);
			
			byte[] content = StreamUtils.createRndArray(chunklen);
			StreamUtils.writeArrayAsArray(sos, content, reps); // test
			
			sos.close();
			assertTrue(fbos.getSize() > reps*chunklen);
			byte[] signatureValue = sos.getSignatureValue();
			long contentLen = sos.getSize();
			
			InputStream fbis = fbos.getInputStream();
			SignatureVerifyingInputStream svis = new SignatureVerifyingInputStream(SignatureAlgorithm.SHA_1_RSA, kp.getPublic(), contentLen, true, fbis);
			byte[] buffer = new byte[content.length];
			StreamUtils.readArrayAsArray(svis, buffer, content, reps);
			assertEquals( -1, svis.read() );
			svis.close();
			assertTrue(svis.isSignatureValid());
			assertArrayEquals( signatureValue, svis.getSignatureValue() );
			
		} finally {
			fbos.discard();
		}
	}
}
