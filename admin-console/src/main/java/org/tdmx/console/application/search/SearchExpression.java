package org.tdmx.console.application.search;

import java.util.HashMap;
import java.util.Map;

import org.tdmx.console.application.search.FieldDescriptor.DomainObjectType;
import org.tdmx.console.application.search.FieldDescriptor.FieldType;
import org.tdmx.console.application.search.match.MatchFunction;

/**
 * SearchExpression := (":"<Type>("."<fieldName>)?" ")?<value>
 *  
 * @author Peter
 *
 */
public final class SearchExpression {

	//-------------------------------------------------------------------------
	//PUBLIC CONSTANTS
	//-------------------------------------------------------------------------
	public static enum ValueType {
		TimeRange, DateTimeRange, DateRange, NumberRange, Time, DateTime, Date, Number, QuotedText, Text
	}
	
	//-------------------------------------------------------------------------
	//PROTECTED AND PRIVATE VARIABLES AND CONSTANTS
	//-------------------------------------------------------------------------
	public DomainObjectType objectType;
	public String fieldName;
	public ValueType valueType;
	public Map<FieldType,MatchFunction> matchFunctionMap = new HashMap<FieldDescriptor.FieldType, MatchFunction>();
	
	//-------------------------------------------------------------------------
	//CONSTRUCTORS
	//-------------------------------------------------------------------------

	public SearchExpression() {
	}
	
	//-------------------------------------------------------------------------
	//PUBLIC METHODS
	//-------------------------------------------------------------------------
	
	public void add( FieldType fieldType, MatchFunction fn ) {
		matchFunctionMap.put(fieldType, fn);
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
