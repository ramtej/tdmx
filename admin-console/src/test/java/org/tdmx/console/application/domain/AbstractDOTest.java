package org.tdmx.console.application.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.tdmx.console.application.domain.ProblemDO.ProblemCode;

public class AbstractDOTest {

	@Test
	public void testSuperImplicitConstructor() {
		ProblemDO p1 = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		assertNotNull(p1.getId());
		
		ServiceProviderDO sp = new ServiceProviderDO();
		assertNotNull(sp.getId());
	}
	
	@Test
	public void testGetNextObjectId() {
		ProblemDO p1 = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		ProblemDO p2 = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		
		assertFalse(p1.equals(p2));
		assertFalse(p1.getId().equals(p2.getId()));
		
	}

	@Test
	public void testClassNotEquals() {
		ProblemDO p = new ProblemDO(ProblemCode.CONFIGURATION_FILE_MARSHAL, "msg");
		ServiceProviderDO s = new ServiceProviderDO();
		
		String id = "1";
		p.setId(id);
		s.setId(id);
		
		assertFalse(p.equals(s));
		
	}

}
