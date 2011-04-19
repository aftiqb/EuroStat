package org.deri.eurostat.elements;

public class Dimension {

	
	protected String conceptSchemeRef;
	protected String conceptRef;
	protected String codeList;
	
	public Dimension()
	{
		this.conceptRef = null;
		this.conceptSchemeRef = null;
		this.codeList = null;
	}
	
	public Dimension(String conceptRef, String conceptSchemeRef, String codeList)
	{
		this.conceptRef = conceptRef;
		this.conceptSchemeRef = conceptSchemeRef;
		this.codeList = codeList;
		
	}
	
	public String getConceptSchemeRef() {
		return conceptSchemeRef;
	}
	
	public void setConceptSchemeRef(String conceptSchemeRef) {
		this.conceptSchemeRef = conceptSchemeRef;
	}
	
	public String getConceptRef() {
		return conceptRef;
	}
	
	public void setConceptRef(String conceptRef) {
		this.conceptRef = conceptRef;
	}
	
	public String getCodeList() {
		return codeList;
	}
	
	public void setCodeList(String codeList) {
		this.codeList = codeList;
	}
	
	
}
