package org.deri.eurostat.elements;

public class Attribute {

	
	protected String conceptSchemeRef;
	protected String conceptRef;
	protected String codeList;
	protected String attachmentLevel;
	protected String assignmentStatus;
	
	
	public Attribute()
	{
		this.conceptRef = null;
		this.conceptSchemeRef = null;
		this.codeList = null;
		this.attachmentLevel = null;
		this.assignmentStatus = null;
	}
	
	public Attribute(String conceptRef, String conceptSchemeRef, String codeList, String attachmentLevel, String assignmentStatus)
	{
		this.conceptRef = conceptRef;
		this.conceptSchemeRef = conceptSchemeRef;
		this.codeList = codeList;
		this.attachmentLevel = attachmentLevel;
		this.assignmentStatus = assignmentStatus;
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
	
	public String getAttachmentLevel() {
		return attachmentLevel;
	}
	
	public void setAttachmentLevel(String attachmentLevel) {
		this.attachmentLevel = attachmentLevel;
	}
	
	public String getAssignmentStatus() {
		return assignmentStatus;
	}
	
	public void setAssignmentStatus(String assignmentStatus) {
		this.assignmentStatus = assignmentStatus;
	}
	
		
}
