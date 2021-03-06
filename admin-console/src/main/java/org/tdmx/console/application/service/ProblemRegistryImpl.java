package org.tdmx.console.application.service;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.tdmx.console.application.domain.ProblemDO;

public class ProblemRegistryImpl implements ProblemRegistry {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	private LinkedList<ProblemDO> problemList = new LinkedList<>();
	private int maxSize = 1000;
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	//TODO merge the last 2 problems together if the same - adding # to Problem
	@Override
	public void addProblem(ProblemDO problem) {
		problemList.addLast(problem);
		if ( problemList.size() > maxSize ) {
			problemList.removeFirst();
		}
	}

	@Override
	public void deleteProblem(String problemId) {
		if ( problemId == null ) {
			return;
		}
		Iterator<ProblemDO> it = problemList.iterator();
		while( it.hasNext() ) {
			ProblemDO p = it.next();
			if ( problemId.equals(p.getId()) ) {
				it.remove();
			}
		}
	}

	@Override
	public void deleteAllProblems() {
		problemList.clear();
	}

	@Override
	public List<ProblemDO> getProblems() {
		return Collections.unmodifiableList(problemList);
	}

	@Override
	public ProblemDO getLastProblem() {
		try {
			return problemList.getLast();
		} catch ( NoSuchElementException e ) {
			return null;
		}
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
}
