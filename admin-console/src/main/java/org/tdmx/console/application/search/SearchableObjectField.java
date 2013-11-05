package org.tdmx.console.application.search;

import org.tdmx.console.application.domain.DomainObject;
import org.tdmx.console.application.search.match.MatchValueNormalizer;

/**
 * The SearchableObjectField contains a DomainObject's field value.
 * 
 * FieldType
 *             [SearchValue-Type] [OriginalValue-Type]
 *	Text       String[]           String
 *	String     String             String
 *	Token      String             String
 *	Number     Long               Number
 *	Date       Long               Calendar  [seconds since EPOCH]
 *	DateTime   [Long,Integer]     Calendar  [seconds since EPOCH, seconds since 00:00:00]  
*	Time       Integer            Calendar  [seconds since 00:00:00]
 * 
 * @see MatchValueNormalizer
 * 
 * @author Peter
 *
 */
public final class SearchableObjectField {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------

	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------

	public DomainObject object;
	public FieldDescriptor field; 
	public Object searchValue; // doesn't count to the identity
	public Object value; // the original field value 

	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SearchableObjectField( DomainObject object, FieldDescriptor field, Object value ) {
		this.object = object;
		this.field = field;
		this.value = value;
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
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
