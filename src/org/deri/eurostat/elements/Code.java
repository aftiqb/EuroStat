package org.deri.eurostat.elements;

public class Code {

	protected String value;
	protected String description;
	
	public Code()
	{
		this.value = null;
		this.description = null;
	}
	
	public Code(String value, String description)
	{
		this.value = value;
		this.description = description;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
