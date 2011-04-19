package org.deri.eurostat.dsdParser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.deri.eurostat.elements.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class dsdParser {

    private Document xmlDocument;
    private XPath xPath;	
    private static String xmlFilePath = "E:/EU Projects/EuroStat/tsieb010.sdmx/tsieb010.dsd.xml";
    private static String outputFilePath = "E:/EU Projects/EuroStat/datacube mapping/RDF/";
    ArrayList<Code> lstCode = new ArrayList<Code>();
    ArrayList<CodeList> lstCodeLists = new ArrayList<CodeList>();
    ArrayList<Dimension> lstDimensions = new ArrayList<Dimension>();
    ArrayList<Dimension> lstTimeDimensions = new ArrayList<Dimension>();
    ArrayList<Attribute> lstAttributes = new ArrayList<Attribute>();
    ArrayList<Measure> lstMeasures = new ArrayList<Measure>();
    static BufferedWriter write = null;
	static FileWriter fstream = null;
	String fileName = "";
	String codeListURL = "http://example.org/EuroStat/";
	
    private void initObjects(){        
        try {
            xmlDocument = DocumentBuilderFactory.
			newInstance().newDocumentBuilder().
			parse(xmlFilePath);            
            xPath =  XPathFactory.newInstance().
			newXPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }       
    }

    
	public void parseFile()
	{
		Element element = xmlDocument.getDocumentElement();
		NodeList nl;
		getFileName(element);
		
		// parse CodeLists from DSD
		nl = element.getElementsByTagName("CodeLists");
		
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++)
			{
				Element ele = (Element)nl.item(i);
				
				getAllCodeLists(ele);
				
			}
		}
		
		// parse KeyFamilies from DSD
		nl = element.getElementsByTagName("KeyFamilies");
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++)
			{
				Element ele = (Element)nl.item(i);
				
				getKeyFamilies(ele);
				
			}
		}
		
		writeDatatoFile();
	}

	public void getFileName(Element element)
	{
		NodeList nl = element.getElementsByTagName("Header");
		Element ele = (Element)nl.item(0);
		NodeList name = ele.getElementsByTagName("ID");
		
		fileName = name.item(0).getTextContent();

	}
	
	public void getKeyFamilies(Element ele)
	{
		NodeList nl = ele.getElementsByTagName("structure:KeyFamily");
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++)
			{
				Element key = (Element)nl.item(i);
				getKeyFamilyInfo(key);
			}
		}
	}
	
	public void getKeyFamilyInfo(Element key)
	{
		NodeList name = key.getElementsByTagName("structure:Name");
		//KeyFamily obj = new KeyFamily(key.getAttribute("id"),key.getAttribute("agencyID"),key.getAttribute("isFinal"),name.item(0).getTextContent(),key.getAttribute("isExternalReference"));
		
		getComponents(key);
	}
	
	public void getComponents(Element key)
	{
		NodeList name = key.getElementsByTagName("structure:Components");
		
		Element comp = (Element)name.item(0);

		// Dimension
		NodeList dimension = comp.getElementsByTagName("structure:Dimension");
		if(dimension != null && dimension.getLength() > 0)
		{
			for(int i = 0 ; i < dimension.getLength();i++)
			{
				Element dim = (Element)dimension.item(i);
				Dimension obj = new Dimension(dim.getAttribute("conceptRef"),dim.getAttribute("conceptSchemeRef"),dim.getAttribute("codelist"));
				lstDimensions.add(obj);
			}
		}

		// TimeDimension
		NodeList tDimension = comp.getElementsByTagName("structure:TimeDimension");
		if(tDimension != null && tDimension.getLength() > 0)
		{
			for(int i = 0 ; i < tDimension.getLength();i++)
			{
				Element measure = (Element)tDimension.item(i);
				Dimension obj = new Dimension(measure.getAttribute("conceptRef"),measure.getAttribute("conceptSchemeRef"),"");
				lstTimeDimensions.add(obj);
			}
		}

		// PrimaryMeasure
		NodeList pMeasure = comp.getElementsByTagName("structure:PrimaryMeasure");
		if(pMeasure != null && pMeasure.getLength() > 0)
		{
			for(int i = 0 ; i < pMeasure.getLength();i++)
			{
				Element measure = (Element)pMeasure.item(i);
				Measure obj = new Measure(measure.getAttribute("conceptRef"),measure.getAttribute("conceptSchemeRef"),"");
				lstMeasures.add(obj);
			}
		}

		// Attribute
		NodeList attribute = comp.getElementsByTagName("structure:Attribute");
		if(attribute != null && attribute.getLength() > 0)
		{
			for(int i = 0 ; i < attribute.getLength();i++)
			{
				Element att = (Element)attribute.item(i);
				Attribute obj = new Attribute(att.getAttribute("conceptRef"),att.getAttribute("conceptSchemeRef"),att.getAttribute("codelist"),"","");
				lstAttributes.add(obj);
			}
		}
		
	}
	
	public void getAllCodeLists(Element ele)
	{
		NodeList nl = ele.getElementsByTagName("structure:CodeList");
		
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++)
			{
				Element code = (Element)nl.item(i);
				getCodeListInfo(code);
			}
		}

		
	}
	
	public void getCodeListInfo(Element code)
	{
		
		NodeList name = code.getElementsByTagName("structure:Name");
		
		getCodes(code);
		
		CodeList obj = new CodeList(code.getAttribute("id"),code.getAttribute("agencyID"),code.getAttribute("isFinal"),name.item(0).getTextContent(),lstCode);
		lstCodeLists.add(obj);
		
	}
	
	public void getCodes(Element codes)
	{
		lstCode = new ArrayList<Code>();
		
		NodeList name = codes.getElementsByTagName("structure:Code");
		
		if(name != null && name.getLength() > 0)
		{
			for(int i = 0 ; i < name.getLength();i++)
			{
				Element ele = (Element)name.item(i);
				
				NodeList code = ele.getElementsByTagName("structure:Description");
				
				Code obj = new Code();
				obj.setDescription(code.item(0).getTextContent());
				obj.setValue(ele.getAttribute("value"));
				lstCode.add(obj);
			}
		}
		
	}
	
	
	public void writeDatatoFile()
	{
		String codeListID = "";
		int counter = 1;
		
		createRDFFile(fileName);
		
		writeLinetoFile("@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .");
		writeLinetoFile("@prefix skos: <http://www.w3.org/2004/02/skos#> .");
		writeLinetoFile("@prefix qb: <http://purl.org/linked-data/cube#> .");
		writeLinetoFile("@prefix sdmx-concept: <http://purl.org/linked-data/sdmx/2009/concept#> .");
		writeLinetoFile("@prefix concepts: <http://example.org/EuroStat/concepts#> .");
		writeLinetoFile("@prefix property: <http://example.org/EuroStat/property#> .");
		writeLinetoFile("@prefix cl: <http://example.org/EuroStat/CodeList/> .");
		
		// define DataStructureDefinition based on the components identified in the 'KeyFamilies' tag of DSD
		writeLinetoFile("<" + codeListURL + "dsd/" + fileName.substring(0,fileName.indexOf("_")) + ">	a qb:DataStrucutreDefinition;");
		writeLinetoFile("		skos:notation \"" + fileName + "\";");
		
		for(Dimension dim:lstDimensions)
			writeLinetoFile("		qb:component [qb:dimension	property:" + dim.getConceptRef() + "	qb:order " + counter++ + "];");
		
		for(Dimension dim:lstTimeDimensions)
			writeLinetoFile("		qb:component [qb:dimension	property:" + dim.getConceptRef() + "	qb:order " + counter++ + "];");
		
		for(Measure measure:lstMeasures)
			writeLinetoFile("		qb:component [qb:measure	property:" + measure.getConceptRef() + "];");
		
		for(Attribute att:lstAttributes)
			writeLinetoFile("		qb:component [qb:attribute	property:" + att.getConceptRef() + "];");
		
		// there is an extra ; before '.'. Fix this issue
		writeLinetoFile("		.");
		
		for(Dimension dim:lstDimensions)
		{
			writeLinetoFile("property:" + dim.getConceptRef() + " a qb:DimensionProperty, qb:CodedProperty;");
			writeLinetoFile("		rdfs:domain		qb:Observation;");
			writeLinetoFile("		qb:concept		concept:" + dim.getConceptRef() + ";");
			
			for(CodeList obj:lstCodeLists)
			{
				if(obj.getId().toString().equals(dim.getCodeList().toString()))
				{
					if(obj.getAgencyID().equals("SDMX"))
					{
						// re-use the URI from sdmx-code.ttl file
					}
					else
					{
						writeLinetoFile("		qb:codeList		cl:" + obj.getId().substring(obj.getId().indexOf("_")+1) + ";");
						writeLinetoFile("		rdfs:range		cl:" + obj.getId().substring(obj.getId().indexOf("_")+1));
					}
				}
			}
			writeLinetoFile("		.");
		}
		
		for(Dimension dim:lstTimeDimensions)
		{
			writeLinetoFile("property:" + dim.getConceptRef() + " a qb:DimensionProperty;");
			writeLinetoFile("		rdfs:domain		qb:Observation;");
			writeLinetoFile("		qb:concept		concept:" + dim.getConceptRef() + ";");
			// TODO : define rdfs:range of type xsd:date
			
			writeLinetoFile("		.");
		}
		
		for(Measure measure:lstMeasures)
		{
			writeLinetoFile("property:" + measure.getConceptRef() + " a qb:MeasureProperty, qb:CodedProperty;");
			writeLinetoFile("		rdfs:domain		qb:Observation;");
			writeLinetoFile("		qb:concept		concept:" + measure.getConceptRef() + ";");
			// TODO : define rdfs:range of the datatype used
			
			writeLinetoFile("		.");
		}
		
		for(Attribute att:lstAttributes)
		{
			writeLinetoFile("property:" + att.getConceptRef() + " a qb:AttributeProperty, qb:CodedProperty;");
			writeLinetoFile("		rdfs:domain		qb:Observation;");
			writeLinetoFile("		qb:concept		concept:" + att.getConceptRef() + ";");
			for(CodeList obj:lstCodeLists)
			{
				if(obj.getId().toString().equals(att.getCodeList().toString()))
				{
					if(obj.getAgencyID().equals("SDMX"))
					{
						// re-use the URI from sdmx-code.ttl file
					}
					else
					{
						writeLinetoFile("		qb:codeList		cl:" + obj.getId().substring(obj.getId().indexOf("_")+1) + ";");
						writeLinetoFile("		rdfs:range		cl:" + obj.getId().substring(obj.getId().indexOf("_")+1));
					}
				}
			}
			writeLinetoFile("		.");
		}
		
		// translate all codelists from DSD which are defined by agencies other than SDMX.
		for(CodeList obj:lstCodeLists)
		{
			
			if(!obj.getAgencyID().equals("SDMX"))
			{
				codeListID = obj.getId().substring(obj.getId().indexOf("_")+1);
				writeLinetoFile("<" + codeListURL + "CodeList/" + codeListID + ">	a skos:ConceptScheme;");
				writeLinetoFile("		rdfs:label \"" + obj.getName() + "\"@en;");
				writeLinetoFile("		skos:notation \"" + obj.getId() + "\";");
				
				ArrayList<Code> arrCode = obj.getCode();
				for(Code code:arrCode)
				{
					writeLinetoFile("		skos:hasTopConcept <" + codeListURL + "CodeList/" + codeListID + "#" + code.getValue() + ">;");
				}
				// there is an extra ; before '.'. Fix this issue
				writeLinetoFile("		.");
				
				for(Code code:arrCode)
				{
					writeLinetoFile("<" + codeListURL + "CodeList/" + codeListID + "#" + code.getValue() + ">	a skos:Concept;");
					writeLinetoFile("		skos:prefLabel \"" + code.getDescription() + "\"@en;");
					writeLinetoFile("		skos:inScheme <" + codeListURL + "CodeList/" + codeListID + ">;");
					writeLinetoFile("		skos:notation \"" + code.getValue() + "\"");
					writeLinetoFile("		.");
				}

			}
		}

		
	}
	
	public static void writeLinetoFile(String line)
	{
		
	   	try{
	       	
	   		write.newLine();
	       	write.write(line);
	       	write.flush();
	       }
	       catch (Exception e){//Catch exception if any
	       	      System.err.println("Error: " + e.getMessage());
	       	}

	}
	public void createRDFFile(String fileName)
	{
		try
	   	{
			fstream = new FileWriter(outputFilePath + fileName + ".rdf",false);
			write = new BufferedWriter(fstream);
	   	}catch(Exception e)
	   	{
	   		System.out.println("Error while creating file ...");
	   	}
	}
	
	public static void main(String[] args)
	{
		dsdParser obj = new dsdParser();
		
		xmlFilePath = args[0];
		outputFilePath = args[1];
		
		obj.initObjects();
		obj.parseFile();
	}
}
