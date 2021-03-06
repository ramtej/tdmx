package org.tdmx.client.crypto.certificate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TrustStoreCertificateIOUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetAllTrustedCAs() throws Exception {
		
		List<TrustStoreEntry> rootCAs = TrustStoreCertificateIOUtils.getAllSystemTrustedCAs();
		assertNotNull(rootCAs);
		
		for( TrustStoreEntry rootCA : rootCAs ) {
			TrustStoreEntry e = new TrustStoreEntry(rootCA.getCertificate());
			e.setFriendlyName("friendlyname="+e.getCertificate().getFingerprint());
			e.addComment("this is a comment1 "+e.getCertificate().getFingerprint());
			e.addComment("this is a comment2 "+e.getCertificate().getFingerprint());
			System.out.println(TrustStoreCertificateIOUtils.trustStoreEntryToPem(e));
			System.out.println();
		}
	}

	@Test
	public void testTrustEntryToPemConversion() throws Exception {
		
		List<TrustStoreEntry> rootCAs = TrustStoreCertificateIOUtils.getAllSystemTrustedCAs();
		assertNotNull(rootCAs);

		for( TrustStoreEntry rootCA : rootCAs ) {
			TrustStoreEntry e = new TrustStoreEntry(rootCA.getCertificate());
			e.setFriendlyName("friendlyname="+e.getCertificate().getFingerprint());
			e.addComment("this is a comment1 "+e.getCertificate().getFingerprint());
			e.addComment("this is a comment2 "+e.getCertificate().getFingerprint());
			String s = TrustStoreCertificateIOUtils.trustStoreEntryToPem(e);
			assertNotNull(s);
		}
	}

	@Test
	public void testPemToX509ListConversion() throws Exception {
		
		List<TrustStoreEntry> rootCAs = TrustStoreCertificateIOUtils.getAllSystemTrustedCAs();
		assertNotNull(rootCAs);

		List<TrustStoreEntry> trustEntries = new ArrayList<>();
		
		StringBuffer sb = new StringBuffer();
		for( TrustStoreEntry rootCA : rootCAs ) {
			TrustStoreEntry e = new TrustStoreEntry(rootCA.getCertificate());
			e.setFriendlyName("friendlyname="+e.getCertificate().getFingerprint());
			e.addComment("this is a comment1 "+e.getCertificate().getFingerprint());
			e.addComment("this is a comment2 "+e.getCertificate().getFingerprint());
			trustEntries.add(e);

			String s = TrustStoreCertificateIOUtils.trustStoreEntryToPem(e);
			assertNotNull(s);
			sb.append(s);
		}
		
		String pemList = sb.toString();
		List<TrustStoreEntry> readTrustEntries= TrustStoreCertificateIOUtils.pemToTrustStoreEntries(pemList);
		assertNotNull(readTrustEntries);
		assertEquals(readTrustEntries.size(), trustEntries.size());
		for( TrustStoreEntry e : readTrustEntries) {
			assertEquals("friendlyname="+e.getCertificate().getFingerprint(), e.getFriendlyName());
			assertEquals("this is a comment1 "+e.getCertificate().getFingerprint()+TrustStoreEntry.NL+"this is a comment2 "+e.getCertificate().getFingerprint(), e.getComment());
		}
	}
}
