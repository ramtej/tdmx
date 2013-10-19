package org.tdmx.console.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tdmx.console.application.service.ObjectRegistry;
import org.tdmx.console.application.service.ProblemRegistry;
import org.tdmx.console.domain.Domain;
import org.tdmx.console.domain.Problem;
import org.tdmx.console.domain.User;

public class UIServiceImpl implements UIService {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private ObjectRegistry objectRegistry;
	private ProblemRegistry problemRegistry;
	private int busyId;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	@Override
	public List<Domain> listDomains( ) {
		return objectRegistry.getDomains();
	}

	@Override
	public List<Problem> getProblems() {
		List<Problem> list = new ArrayList<>();
		for( org.tdmx.console.application.domain.Problem p : getProblemRegistry().getProblems()) {
			list.add(new Problem(p));
		}
		return list;
	}

	@Override
	public boolean hasProblems() {
		return getProblemRegistry().getProblems().size() > 0;
	}

	@Override
	public void deleteAllProblems() {
		getProblemRegistry().deleteAllProblems();
	}

	@Override
	public void deleteProblem(int id) {
		getProblemRegistry().deleteProblem(id);
	}

	@Override
	public int getNumberOfProblems() {
		return problemRegistry.getProblems().size();
	}

	@Override
	public Problem getMostRecentProblem() {
		org.tdmx.console.application.domain.Problem p = problemRegistry.getLastProblem();
		return p != null ? new Problem(p) : null;
	}

	@Override
	public User authenticate(String login, String password) {
		if ("secret".equals(password) ) {
			User user = new User(login, "George", "Bush", "noreply@mycompany.com", new Date());
			return user;
		}
		return null;
	}

    //-------------------------------------------------------------------------
	//PROTECTED METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PRIVATE METHODS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC ACCESSORS (GETTERS / SETTERS)
	//-------------------------------------------------------------------------

	public ObjectRegistry getObjectRegistry() {
		return objectRegistry;
	}

	public void setObjectRegistry(ObjectRegistry objectRegistry) {
		this.objectRegistry = objectRegistry;
	}
	
	public ProblemRegistry getProblemRegistry() {
		return problemRegistry;
	}

	public void setProblemRegistry(ProblemRegistry problemRegistry) {
		this.problemRegistry = problemRegistry;
	}

	public int getBusyId() {
		return busyId;
	}

	public void setBusyId(int busyId) {
		this.busyId = busyId;
	}

}