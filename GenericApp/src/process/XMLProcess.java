package process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import parametros.ComboBoxItem;
import parametros.Parametro;
import parametros.ParametroCheckBox;
import parametros.ParametroComboBox;
import parametros.ParametroDate;
import parametros.ParametroRadioButton;
import parametros.RadioButtonItem;
import parametros.Validation;
import parametros.Parametro.inputs;

public class XMLProcess 
{
	private static final Logger logger = Logger.getLogger(XMLProcess.class.getName());
	
	public String filename;
	Document document;
	String fullCommand;
	
	public XMLProcess(String FileName)
	{
		filename = FileName;
		fullCommand = "";
		
		logger.fine("Archivo = " + FileName);
		logger.fine("XML Process creado");
		
		parseXmlFile();
	}
	
	private void parseXmlFile()
	{
		//get the factory
		DocumentBuilderFactory documentBuilderFactory;
		DocumentBuilder documentBuilder;
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		
		try 
		{
			//Using factory get an instance of document builder
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			
			//parse using builder to get DOM representation of the XML file
			document = documentBuilder.parse(filename);

		}
		catch(ParserConfigurationException pce) 
		{
			pce.printStackTrace();
		}
		catch(SAXException se) 
		{
			se.printStackTrace();
		}
		catch(IOException ioe) 
		{
			ioe.printStackTrace();
		}
	}
	
	public ArrayList<Aplicacion> parseDocument()
	{
		Element element;
		NodeList nodeList_Head;
		NodeList nodeList_Parameters;
		NodeList nodeList_customValidation;
		NodeList nodeList_Aplication = document.getElementsByTagName("application");
		
		if(nodeList_Aplication == null)
		{
			logger.severe("No puede no tener tag application");
			return null;
		}
		
		ArrayList<Aplicacion> aplicaciones = new ArrayList<Aplicacion>();
		
		for(int i = 0 ; i < nodeList_Aplication.getLength();i++) 
		{	
			element = (Element)nodeList_Aplication.item(i);
			String appName = element.getAttribute("name");
			String exePath = element.getAttribute("exePath");
			String command = element.getAttribute("command");
			
			Aplicacion app = new Aplicacion(appName);
			app.SetCommand(command);
			app.SetExePath(exePath);
			
			nodeList_Head = element.getElementsByTagName("head");
			if(nodeList_Head == null || nodeList_Head.getLength() > 1)
			{
				logger.severe("No puede no tener tag head o tener m�s de uno");
				return null;
			}
			
			if(nodeList_Head.getLength() == 1)
			{
				Element el = (Element)nodeList_Head.item(0);
				NodeList nodeList_label = el.getElementsByTagName("label");
				el = (Element)nodeList_label.item(0);
				String Description = el.getFirstChild().getNodeValue();
				app.SetDescription(Description);
			}
			else
			{
				app.SetDescription("No description shown on XML File");
			}

			nodeList_Parameters = element.getElementsByTagName("parameter"); 
			
			if(nodeList_Parameters == null)
			{
				logger.severe("No puede no tener tag parameter");
				return null;
			}

			parsearParametros(nodeList_Parameters,app);
			
			nodeList_customValidation = element.getElementsByTagName("customValidation"); 
			
			parsearCustomValidation(nodeList_customValidation, app);
			
			aplicaciones.add(app);
		}
		
		return aplicaciones;
		
	}
	
	private void parsearCustomValidation(NodeList nodeList_customValidation, Aplicacion app) 
	{
		Element element;
		
		try
		{	
			element = (Element)nodeList_customValidation.item(0);
			String className = element.getAttribute("class");
			String classPackage = element.getAttribute("package");
			String classPath = element.getAttribute("path");
			CustomValidationClass aCustomValidationClass = new CustomValidationClass(classPath, classPackage, className);
			app.SetCustomValidationClass(aCustomValidationClass);
		}
		catch(NullPointerException e)
		{
			logger.fine("Application has not CustomValidation");
		}
	}

	private void parsearParametros(NodeList nodeList_Parameters, Aplicacion app)
	{
		Element element;
		
		logger.fine("getParamentro(" + app.name + ")");
		for(int j = 0 ; j < nodeList_Parameters.getLength();j++) 
		{	
			element = (Element)nodeList_Parameters.item(j);
			
			Parametro parametro = getParametro(element);
			app.agregarParametro(parametro);
			
		}
	}
	
	public Parametro getParametro(Element element)
	{
		NodeList nodeList;
		
		String label = getTextValue(element, "label");
		String flag = getTextValue(element, "flag");
		
		Validation validation = getValidation(element);
		
		nodeList = element.getElementsByTagName("folderInput");
		if(nodeList != null && nodeList.getLength() > 0) 
		{
			Parametro parametro = new Parametro(label, flag, inputs.folderInput, validation);
			return parametro;
		}
		
		nodeList = element.getElementsByTagName("fileInput");
		if(nodeList != null && nodeList.getLength() > 0) 
		{
			Parametro parametro = new Parametro(label, flag, inputs.fileInput, validation);
			return parametro;
		}
		
		nodeList = element.getElementsByTagName("comboBox");
		if(nodeList != null && nodeList.getLength() > 0) 
		{
			ParametroComboBox parametro = new ParametroComboBox(label, flag, inputs.comboBox, validation);
			Element elemAux = (Element) nodeList.item(0);	//obtengo el elemento <comboBox>
			NodeList items = elemAux.getElementsByTagName("comboBoxItem");	//lista de <comboBoxItem>
			for(int k=0 ; k < items.getLength() ; k++)
			{
				logger.fine("Entro en el for de comboBoxItem");
				
				logger.fine("tems.getLength() = " + items.getLength());
				
				elemAux = (Element) items.item(k);			//cada <comboBoxItem> de la lista
				String tag = elemAux.getAttribute("tag");
				String flag2 = elemAux.getAttribute("flag");
				
				logger.fine("tag = " + tag);
				
				logger.fine("flag = " + flag2);
				
				ComboBoxItem cBItem = new ComboBoxItem(tag, flag2);
				cBItem.addSubParametros(listaSubparametrosIfSelected(elemAux));	//agrega todos los subparametros en <ifSelected>
				
				parametro.addComboBoxItem(cBItem);
			}
			return parametro;
		}
		
		nodeList = element.getElementsByTagName("checkBox");
		
		if(nodeList != null && nodeList.getLength() > 0) 
		{
			Element elemAux = (Element) nodeList.item(0);
			String checkBoxFlag = elemAux.getAttribute("flag");
			ParametroCheckBox parametro = new ParametroCheckBox(label, checkBoxFlag, inputs.checkBox, validation);
			parametro.addSubparametros(listaSubparametrosIfSelected(elemAux));
			return parametro;
		}
		
		nodeList = element.getElementsByTagName("radioButton");
		
		if(nodeList != null && nodeList.getLength() > 0) 
		{
			ParametroRadioButton parametro = new ParametroRadioButton(label, flag, inputs.radioButton, validation);
			Element elemAux = (Element) nodeList.item(0); //obtengo el elemento <radioButton>
			NodeList items = elemAux.getElementsByTagName("radioButtonItem");  //lista de <radioButtonItem>
			for(int k=0 ; k < items.getLength() ; k++)
			{
				logger.fine("Entro en el for de radioButtonItem");
				logger.fine("items.getLength() = " + items.getLength());
        
				elemAux = (Element) items.item(k);      //cada <radioButtonItem> de la lista
				String tag = elemAux.getAttribute("tag");
				String flag2 = elemAux.getAttribute("flag");
        
				logger.fine("tag = " + tag);
				logger.fine("flag = " + flag2);
        
				RadioButtonItem radioButtonItem = new RadioButtonItem(tag, flag2);
				radioButtonItem.addSubParametros(listaSubparametrosIfSelected(elemAux)); //agrega todos los subparametros en <ifSelected>        
				parametro.addRadioButtonItem(radioButtonItem);
			}
			return parametro;
		}
		
		nodeList = element.getElementsByTagName("dateTimePicker");
		
		if(nodeList != null && nodeList.getLength() > 0) 
		{
			Element elemAux = (Element) nodeList.item(0);
			String format = elemAux.getAttribute("format");
			Parametro parametro = new ParametroDate(label, flag, inputs.dateTimePicker, validation, format);
			return parametro;
		}
		
		nodeList = element.getElementsByTagName("textBox");
		
		if(nodeList != null && nodeList.getLength() > 0) 
		{
			Parametro parametro = new Parametro(label, flag, inputs.textBox, validation);
			return parametro;
		}
		
		//si no tiene un tag de input
		Parametro parametro = new Parametro(label, flag, null, validation);
		
		return parametro;
	}
	
	public ArrayList<Parametro> listaSubparametrosIfSelected(Element elemAux)
	{
		ArrayList<Parametro> listaSubparametros = null;
		Element element = (Element) elemAux.getElementsByTagName("ifSelected").item(0);
		if(element != null)	//si adentro del <comboBoxItem> hay un <ifSelected>
		{
			listaSubparametros = new ArrayList<Parametro>();
			NodeList nodeList = element.getElementsByTagName("item");	//lista de <item>
			for(int l=0 ; l < nodeList.getLength() ; l++ )
			{
				Parametro subParametro = getParametro((Element)nodeList.item(l));
				listaSubparametros.add(subParametro);
			}
		}
		return listaSubparametros;
	}
	
	private Validation getValidation(Element element)
	{
		Element elemAux = (Element) element.getElementsByTagName("validation").item(0);
		Validation validation = new Validation();
		if(elemAux == null)
		{
			//Log.writeLogMessage(Log.INFO, "No existe tag Validation");
			logger.info("No existe tag Validation");
			return null;
		}
		
		String minSize = elemAux.getAttribute("minSize");
		if(minSize.length()>0)
		{
			validation.setMinSize(Integer.parseInt(minSize));
		}
		String maxSize = elemAux.getAttribute("maxSize");
		if(maxSize.length()>0)
		{
			validation.setMaxSize(Integer.parseInt(maxSize));
		}
		String nullable = elemAux.getAttribute("nullable");
		if(nullable.length()>0)
		{
			validation.setNullable(Boolean.parseBoolean(nullable));
		}
		String numeric = elemAux.getAttribute("numeric");
		if(numeric.length()>0)
		{
			validation.setNumeric(Boolean.parseBoolean(numeric));
		}
		String exists = elemAux.getAttribute("exists");
		if(exists.length()>0)
		{
			validation.setExists(Boolean.parseBoolean(exists));
		}
		return validation;
	}		
	
	public String getTextValue(Element element, String tagName) 
	{
		String textVal = null;
		
		logger.fine("Buscando el Tag " + tagName);
		
		NodeList nodeList = element.getElementsByTagName(tagName);
		if(nodeList != null && nodeList.getLength() > 0) 
		{
			logger.fine("nodeList != null");
			
			Element el = (Element)nodeList.item(0);
			try
			{
				textVal = el.getFirstChild().getNodeValue();
			}
			catch(NullPointerException e)
			{
				textVal = "";
			}
		}
		return textVal;
	}
	
	public String getFullCommand()
	{
		return fullCommand;
	}
	
}
