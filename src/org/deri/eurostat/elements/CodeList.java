package org.deri.eurostat.elements;

import java.util.ArrayList;

public class CodeList {

	protected String id;
	protected String agencyID;
	protected String isFinal;
	protected String name;
	protected ArrayList<Code> lstCodes;
	
	public CodeList()
	{
		this.id = null;
		this.agencyID = null;
		this.isFinal = null;
		this.name = null;
		this.lstCodes = new ArrayList<Code>();
	}
	
	public CodeList(String id, String agencyID, String isFinal, String name, ArrayList<Code> lstCode)
	{
		this.id = id;
		this.agencyID = agencyID;
		this.isFinal = isFinal;
		this.name = name;
		this.lstCodes = lstCode;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getAgencyID() {
		return agencyID;
	}
	
	public void setAgencyID(String agencyID) {
		this.agencyID = agencyID;
	}
	
	public String getIsFinal() {
		return isFinal;
	}
	
	public void setIsFinal(String isFinal) {
		this.isFinal = isFinal;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ArrayList<Code> getCode() {
		return lstCodes;
	}
	
	public void setComments(ArrayList<Code> code) {
		this.lstCodes = code;
	}

}
