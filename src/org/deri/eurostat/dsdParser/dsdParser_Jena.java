package org.deri.eurostat.dsdParser;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.deri.eurostat.DataModel.DataStoreModel;
import org.deri.eurostat.elements.Attribute;
import org.deri.eurostat.elements.Code;
import org.deri.eurostat.elements.CodeList;
import org.deri.eurostat.elements.Concept;
import org.deri.eurostat.elements.Dimension;
import org.deri.eurostat.elements.Measure;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;


/*
 * Slovenia in french language is being written in different characters than represented in the DSD XML file. Check
 * Multi-Language issue in jena
 *  
 */
public class dsdParser_Jena {

    private Document xmlDocument;
    private XPath xPath;	
    private static String xmlFilePath = "E:/EU Projects/EuroStat/tsieb010.sdmx/tsieb010.dsd.xml";
    private static String outputFilePath = "E:/EU Projects/EuroStat/datacube mapping/RDF/";
    private static String serialization = "RDF/XML";
    ArrayList<Code> lstCode = new ArrayList<Code>();
    ArrayList<Concept> lstConcepts = new ArrayList<Concept>();
    ArrayList<CodeList> lstCodeLists = new ArrayList<CodeList>();
    ArrayList<Dimension> lstDimensions = new ArrayList<Dimension>();
    ArrayList<Dimension> lstTimeDimensions = new ArrayList<Dimension>();
    ArrayList<Attribute> lstAttributes = new ArrayList<Attribute>();
    ArrayList<Measure> lstMeasures = new ArrayList<Measure>();
    //static BufferedWriter write = null;
	//static FileWriter fstream = null;
	String fileName = "";
	String codeListURL = "http://example.org/EuroStat/";
	static DataStoreModel dsModel;
	public final String baseURI = "http://purl.org/linked-data/sdmx#";
	
	public String getCodeList(String codeList)
	{
		dsModel = new DataStoreModel();
		dsModel.addRDFtoDataModel("sdmx-code/sdmx-code.ttl", baseURI, "TURTLE");
		return dsModel.returnCodeListURI(codeList);
	}
	
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
		
		
		// parse Concepts from DSD
		nl = element.getElementsByTagName("Concepts");
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++)
			{
				Element ele = (Element)nl.item(i);
				
				getConcepts(ele);
				
			}
		}
		
		produceRDF();
	}

	public void getConcepts(Element element)
	{
		NodeList nl = element.getElementsByTagName("structure:ConceptScheme");
		if(nl != null && nl.getLength() > 0)
		{
			for(int i = 0 ; i < nl.getLength();i++)
			{
				Element key = (Element)nl.item(i);
				getConceptInfo(key);
			}
		}
	}
	
	public void getConceptInfo(Element ele)
	{
		HashMap<String, String> hshName = new HashMap<String, String>();
		
		NodeList concept = ele.getElementsByTagName("structure:Concept");
		
		if(concept != null && concept.getLength() > 0)
		{
			for(int i = 0 ; i < concept.getLength();i++)
			{
				hshName = new HashMap<String, String>();
				Element con = (Element)concept.item(i);
				//System.out.println("ID : " + con.getAttribute("id"));
				NodeList lst = con.getElementsByTagName("structure:Name");
				
				for(int j = 0 ; j < lst.getLength() ; j++)
				{
					Element desc = (Element)lst.item(j);
					//System.out.println(desc.getAttribute("xml:lang") + " -- " +  desc.getTextContent());
					hshName.put(desc.getAttribute("xml:lang"), desc.getTextContent());
				}
				
				Concept obj = new Concept(con.getAttribute("id"),hshName);
				lstConcepts.add(obj);
			}
		}
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
				Dimension obj = new Dimension(dim.getAttribute("conceptRef"),dim.getAttribute("conceptSchemeRef"),dim.getAttribute("codelist"), getType(dim));
				lstDimensions.add(obj);
				
				/*
				NodeList lstType = dim.getElementsByTagName("structure:TextFormat");
				if(lstType != null && lstType.getLength() > 0)
				{
					Element type = (Element) lstType.item(0);
					System.out.println("type : " + type.getAttribute("textType"));
				}
				*/
			}
		}

		// TimeDimension
		NodeList tDimension = comp.getElementsByTagName("structure:TimeDimension");
		if(tDimension != null && tDimension.getLength() > 0)
		{
			for(int i = 0 ; i < tDimension.getLength();i++)
			{
				Element measure = (Element)tDimension.item(i);
				Dimension obj = new Dimension(measure.getAttribute("conceptRef"),measure.getAttribute("conceptSchemeRef"),measure.getAttribute("codelist"), getType(measure));
				lstTimeDimensions.add(obj);
				/*
				NodeList lstType = measure.getElementsByTagName("structure:TextFormat");
				if(lstType != null && lstType.getLength() > 0)
				{
					Element type = (Element) lstType.item(0);
					System.out.println("type : " + type.getAttribute("textType"));
				}
				*/
			}
		}

		// PrimaryMeasure
		NodeList pMeasure = comp.getElementsByTagName("structure:PrimaryMeasure");
		if(pMeasure != null && pMeasure.getLength() > 0)
		{
			for(int i = 0 ; i < pMeasure.getLength();i++)
			{
				Element measure = (Element)pMeasure.item(i);
				Measure obj = new Measure(measure.getAttribute("conceptRef"),measure.getAttribute("conceptSchemeRef"),measure.getAttribute("codelist"),getType(measure));
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
				Attribute obj = new Attribute(att.getAttribute("conceptRef"),att.getAttribute("conceptSchemeRef"),att.getAttribute("codelist"),"","", getType(att));
				lstAttributes.add(obj);
			}
		}
		
	}
	
	public String getType(Element ele)
	{
		NodeList lstType = ele.getElementsByTagName("structure:TextFormat");
		if(lstType != null && lstType.getLength() > 0)
		{
			Element type = (Element) lstType.item(0);
			//System.out.println("type : " + type.getAttribute("textType"));
			return type.getAttribute("textType");
		}
		else
			return "";
	
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
		HashMap<String, String> hshName = new HashMap<String, String>();
		
		NodeList name = code.getElementsByTagName("structure:Name");
		for(int j = 0 ; j < name.getLength() ; j++)
		{
			Element desc = (Element)name.item(j);
			hshName.put(desc.getAttribute("xml:lang"), desc.getTextContent());
		}
		
		getCodes(code);
		
		CodeList obj = new CodeList(code.getAttribute("id"),code.getAttribute("agencyID"),code.getAttribute("isFinal"),hshName,lstCode);
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
				HashMap<String, String> hshDescription = new HashMap<String, String>();
				
				Element ele = (Element)name.item(i);
				
				NodeList code = ele.getElementsByTagName("structure:Description");
				
				for(int j = 0 ; j < code.getLength() ; j++)
				{
					Element desc = (Element)code.item(j);
					hshDescription.put(desc.getAttribute("xml:lang"), desc.getTextContent());
				}
				
				Code obj = new Code(ele.getAttribute("value"),hshDescription);
				//obj.setDescription(code.item(0).getTextContent());
				//obj.setValue(ele.getAttribute("value"));
				lstCode.add(obj);
			}
		}
		
	}
	
	public void produceRDF()
	{
		//Model model = ModelFactory.createDefaultModel();
		
		Model model = ParserUtil.getModelProperties();
		Model codelist_Model = ModelFactory.createDefaultModel();
		
		Resource root = model.createResource( codeListURL + "dsd/" + fileName.substring(0,fileName.indexOf("_")) );
		
		
		model.add(root,ParserUtil.type,ParserUtil.dsd).add(root,ParserUtil.notation,fileName);
		
		//
		for(Dimension dim:lstDimensions)
		{
			Resource component_1 = model.createResource();
			model.add(root,ParserUtil.component,component_1);
			Property prop = model.createProperty(ParserUtil.property + dim.getConceptRef());
			model.add(component_1,ParserUtil.dimension,prop);
			model.add(prop,ParserUtil.type,ParserUtil.dimensionProperty);
			model.add(prop,ParserUtil.type,ParserUtil.codedProperty);
			model.add(prop,ParserUtil.rdfsDomain,ParserUtil.observation);
			Property cncpt = model.createProperty(ParserUtil.concepts + dim.getConceptRef());
			model.add(prop,ParserUtil.concept,cncpt);
			
			if(!dim.getCodeList().equals(""))
			{
				for(CodeList obj:lstCodeLists)
				{
					if(obj.getId().toString().equals(dim.getCodeList().toString()))
					{
						if(obj.getAgencyID().equals("SDMX"))
						{
							// re-use the URI from sdmx-code.ttl file
							String codeList = getCodeList(obj.getId());
							Property cList = model.createProperty(ParserUtil.sdmx_code + codeList);
							model.add(prop,ParserUtil.codeList,cList);
							model.add(prop,ParserUtil.rdfsRange,cList);
						}
						else
						{
							Property cList = model.createProperty(ParserUtil.cl + obj.getId().substring(obj.getId().indexOf("_")+1));
							model.add(prop,ParserUtil.codeList,cList);
							model.add(prop,ParserUtil.rdfsRange,cList);
						}
					}
				}
			}
			else
			{
				Property type = model.createProperty(ParserUtil.xsd + dim.getDataType().toLowerCase());
				model.add(prop,ParserUtil.rdfsRange,type);
			}
		}
		
		//
		for(Dimension dim:lstTimeDimensions)
		{
			Resource component_1 = model.createResource();
			model.add(root,ParserUtil.component,component_1);
			Property prop = model.createProperty(ParserUtil.property + dim.getConceptRef());
			model.add(component_1,ParserUtil.dimension,prop);
			model.add(prop,ParserUtil.type,ParserUtil.dimensionProperty);
			model.add(prop,ParserUtil.rdfsDomain,ParserUtil.observation);
			Property cncpt = model.createProperty(ParserUtil.concepts + dim.getConceptRef());
			model.add(prop,ParserUtil.concept,cncpt);
			
			if(!dim.getCodeList().equals(""))
			{
				for(CodeList obj:lstCodeLists)
				{
					if(obj.getId().toString().equals(dim.getCodeList().toString()))
					{
						if(obj.getAgencyID().equals("SDMX"))
						{
							String codeList = getCodeList(obj.getId());
							Property cList = model.createProperty(ParserUtil.sdmx_code + codeList);
							model.add(prop,ParserUtil.codeList,cList);
							model.add(prop,ParserUtil.rdfsRange,cList);
						}
						else
						{
							Property cList = model.createProperty(ParserUtil.cl + obj.getId().substring(obj.getId().indexOf("_")+1));
							model.add(prop,ParserUtil.codeList,cList);
							model.add(prop,ParserUtil.rdfsRange,cList);
						}
					}
				}
			}
			else
			{
				Property type = model.createProperty(ParserUtil.xsd + dim.getDataType().toLowerCase());
				model.add(prop,ParserUtil.rdfsRange,type);
			}
		}
		
		//
		for(Measure measure:lstMeasures)
		{
			Resource component_1 = model.createResource();
			model.add(root,ParserUtil.component,component_1);
			Property prop = model.createProperty(ParserUtil.property + measure.getConceptRef());
			model.add(component_1,ParserUtil.measure,prop);
			model.add(prop,ParserUtil.type,ParserUtil.measureProperty);
			model.add(prop,ParserUtil.type,ParserUtil.codedProperty);
			model.add(prop,ParserUtil.rdfsDomain,ParserUtil.observation);
			Property cncpt = model.createProperty(ParserUtil.concepts + measure.getConceptRef());
			model.add(prop,ParserUtil.concept,cncpt);
			
			if(!measure.getCodeList().equals(""))
			{
				for(CodeList obj:lstCodeLists)
				{
					if(obj.getId().toString().equals(measure.getCodeList().toString()))
					{
						if(obj.getAgencyID().equals("SDMX"))
						{
							String codeList = getCodeList(obj.getId());
							Property cList = model.createProperty(ParserUtil.sdmx_code + codeList);
							model.add(prop,ParserUtil.codeList,cList);
							model.add(prop,ParserUtil.rdfsRange,cList);
						}
						else
						{
							Property cList = model.createProperty(ParserUtil.cl + obj.getId().substring(obj.getId().indexOf("_")+1));
							model.add(prop,ParserUtil.codeList,cList);
							model.add(prop,ParserUtil.rdfsRange,cList);
						}
					}
				}
			}
			else
			{
				Property type = model.createProperty(ParserUtil.xsd + measure.getDataType().toLowerCase());
				model.add(prop,ParserUtil.rdfsRange,type);
			}
		}
		
		// 
		for(Attribute att:lstAttributes)
		{
			Resource component_1 = model.createResource();
			model.add(root,ParserUtil.component,component_1);
			Property prop = model.createProperty(ParserUtil.property + att.getConceptRef());
			model.add(component_1,ParserUtil.attribute,prop);
			model.add(prop,ParserUtil.type,ParserUtil.attributeProperty);
			model.add(prop,ParserUtil.type,ParserUtil.codedProperty);
			model.add(prop,ParserUtil.rdfsDomain,ParserUtil.observation);
			Property cncpt = model.createProperty(ParserUtil.concepts + att.getConceptRef());
			model.add(prop,ParserUtil.concept,cncpt);
			
			for(CodeList obj:lstCodeLists)
			{
				if(obj.getId().toString().equals(att.getCodeList().toString()))
				{
					if(obj.getAgencyID().equals("SDMX"))
					{
						// re-use the URI from sdmx-code.ttl file
						String codeList = getCodeList(obj.getId());
						Property cList = model.createProperty(ParserUtil.sdmx_code + codeList);
						model.add(prop,ParserUtil.codeList,cList);
						model.add(prop,ParserUtil.rdfsRange,cList);
					}
					else
					{
						Property cList = model.createProperty(ParserUtil.cl + obj.getId().substring(obj.getId().indexOf("_")+1));
						model.add(prop,ParserUtil.codeList,cList);
						model.add(prop,ParserUtil.rdfsRange,cList);
					}
				}
			}
		}

		// translates code lists
		String codeListID = "";
		String name;
		HashMap<String, String> hshName = new HashMap<String, String>();
		
		for(CodeList obj:lstCodeLists)
		{
			if(!obj.getAgencyID().equals("SDMX"))
			{
				codelist_Model = ParserUtil.getModelProperties();
				
				codeListID = obj.getId().substring(obj.getId().indexOf("_")+1);
				Resource codeLists = model.createResource(codeListURL + "CodeList/" + codeListID);
				Resource codelist_Lists = codelist_Model.createResource(codeListURL + "CodeList/" + codeListID);
				
				model.add(codeLists,ParserUtil.type,ParserUtil.conceptScheme);
				codelist_Model.add(codelist_Lists,ParserUtil.type,ParserUtil.conceptScheme);
				
				model.add(codeLists,ParserUtil.notation,obj.getId());
				codelist_Model.add(codelist_Lists,ParserUtil.notation,obj.getId());
				
				// print multilingual labels
				hshName = obj.gethshName();
				Iterator entrySetIterator = hshName.entrySet().iterator();
				while (entrySetIterator.hasNext())
				{
					Map.Entry entry = (Map.Entry) entrySetIterator.next();
		            String key = (String) entry.getKey();
		            name = hshName.get(key);
		            model.add(codeLists,ParserUtil.rdfsLabel,model.createLiteral(name,key));
		            codelist_Model.add(codelist_Lists,ParserUtil.rdfsLabel,model.createLiteral(name,key));
				}
				
				ArrayList<Code> arrCode = obj.getCode();
				for(Code code:arrCode)
				{
					//writeLinetoFile("		skos:hasTopConcept <" + codeListURL + "CodeList/" + codeListID + "#" + code.getValue() + ">;");
					String str = codeListURL + "CodeList/" + codeListID + "#" + code.getValue();
					Resource res = model.createResource(str);
					Resource codelist_res = codelist_Model.createResource(str);
					
					model.add(codeLists,ParserUtil.topConcept,res);
					codelist_Model.add(codelist_Lists,ParserUtil.topConcept,codelist_res);
					
					model.add(res,ParserUtil.type,ParserUtil.skosConcept);
					codelist_Model.add(codelist_res,ParserUtil.type,ParserUtil.skosConcept);
					
					// print multilingual labels
					hshName = code.gethshDescription();
					Iterator entryIterator = hshName.entrySet().iterator();
					while (entryIterator.hasNext())
					{
						Map.Entry entry = (Map.Entry) entryIterator.next();
			            String key = (String) entry.getKey();
			            name = hshName.get(key);
			            
			            model.add(res,ParserUtil.skosLabel, model.createLiteral(name,key));
			            codelist_Model.add(codelist_res,ParserUtil.skosLabel, model.createLiteral(name,key));
					}
					
					str = str.substring(0,str.indexOf("#"));
					Resource resource = model.createResource(str);
					Resource codelist_resource = codelist_Model.createResource(str);
					
					model.add(res,ParserUtil.skosScheme,resource);
					codelist_Model.add(codelist_res,ParserUtil.skosScheme,codelist_resource);
					
					model.add(res,ParserUtil.notation,code.getValue());
					codelist_Model.add(codelist_res,ParserUtil.notation,code.getValue());
				}
				
				codelist_Model.write(System.out,serialization);
			}
		}

		for(Concept concept:lstConcepts)
		{
			Resource con = model.createResource(ParserUtil.concepts + concept.getId());
			model.add(con,ParserUtil.type,ParserUtil.sdmx);
			model.add(con,ParserUtil.notation,concept.getId());
			
			//print multilingual labels
			hshName = concept.gethshName();
			Iterator entrySetIterator = hshName.entrySet().iterator();
			while (entrySetIterator.hasNext())
			{
				Map.Entry entry = (Map.Entry) entrySetIterator.next();
	            String key = (String) entry.getKey();
	            name = hshName.get(key);
	            //writeLinetoFile("		skos:prefLabel \"" + name + "\"@" + key + ";");
	            model.add(con,ParserUtil.skosLabel,model.createLiteral(name,key));
			}
			
			Resource res = model.createResource(ParserUtil.concepts);
			model.add(con,ParserUtil.skosScheme,res);
			
		}
		
		
	
		writeRDFToFile(fileName,model);
	}
	
	public void writeRDFToFile(String fileName, Model model)
	{
		try
	   	{
			OutputStream output = new FileOutputStream(outputFilePath + fileName + ".rdf",false);
			model.write(output,serialization);
	   	}catch(Exception e)
	   	{
	   		System.out.println("Error while creating file ..." + e.getMessage());
	   	}
	}	
	public static void main(String[] args)
	{
		dsdParser_Jena obj = new dsdParser_Jena();
		
		xmlFilePath = args[0];
		outputFilePath = args[1];
		if(args.length > 2)
			serialization = args[2];
		
		obj.initObjects();
		obj.parseFile();
	}
}
