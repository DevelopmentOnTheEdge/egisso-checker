package com.developmentontheedge.egisso.checker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class EgissoChecker
{
    public static final String VERSION       = "0.1.0";
    public static final String XSD_LOCAL_MSZ = "10.05.I-1.0.3.xsd";
    public static final String USAGE 		 = "java -jar egisso-checker" + VERSION + ".jar файл_для_проверки"; 

   
    public static void main(String[] args)
    {
        Locale.setDefault(new Locale("ru"));

    	if( args.length != 1 )
    	{
    		System.out.println("\r\nНеправильное число аргументов, правильно: \r\n" + USAGE);
    		System.exit(-1);
    	}

        try
        {
       	
        	EgissoChecker checker = new EgissoChecker();
        	checker.checkLocalMSZ(args[0]);
        }
        catch(Throwable t)
        {
    		System.out.println("Непредвиденная ошибка: " + t);
    		t.printStackTrace();
        }
    }
    
    // ////////////////////////////////////////////////////////////////////////

    private static long startTime;

    protected String createInfo(File fileToCheck, File fileProtocol, File fileErrors)
    {
    	StringBuilder info = new StringBuilder();
    	
    	info.append("Чекер для ЕГИССО, версия: " + VERSION)
            .append("\r\nПроверяемый файл:  " + fileToCheck.getName()) // AbsolutePath())
    	    .append("\r\nФайл протокола:    " + fileProtocol.getName())
    	    .append("\r\nФайл с ошибками:   " + fileErrors.getName())
    	    .append("\r\nXSD схема для проверки локального классификатора услуг (ЛКМСЗ): " + XSD_LOCAL_MSZ);
    	
    	return info.toString();
    }
    
    public void checkLocalMSZ(String fileName) throws SAXException, IOException
    {
    	File fileToCheck = new File(fileName);
    	if( !fileToCheck.exists() )
        {
    		System.out.println("\r\nНе найден файл для проверки: " + fileToCheck.getAbsolutePath());
    		System.exit(-1);
        }

    	File fileProtocol = new File(fileName + ".prt");
    	File fileErrors   = new File(fileName + ".err");

    	String info = createInfo(fileToCheck, fileProtocol, fileErrors);
    	System.out.println(info);
    	
    	PrintWriter protocol = new PrintWriter(fileProtocol);
    	protocol.println(info);

    	PrintWriter errors = new PrintWriter(fileErrors);
    	errors.println("Строка\tПозиция\tКод ошибки\tОписание");
    	
    	startTime = System.currentTimeMillis();
		System.out.println("\r\nНачинаем проверку ...");
    	
    	Validator validator = createValidator(XSD_LOCAL_MSZ, errors);
    	StreamSource xmlStream = new StreamSource(fileToCheck);

        try
        {
            validator.validate(xmlStream);
        }
        catch(SAXParseException e)
        {
        	logError(errors, e);
        }
       
        errors.close();

    	CheckerErrorHandler errorHandler = (CheckerErrorHandler)validator.getErrorHandler();    	
        printProtocol(protocol, errorHandler);
        protocol.close();

		System.out.println("\r\nПроверка завершена (" + (System.currentTimeMillis() - startTime) + " ms)");
        
		if( errorHandler.isOK )
        	System.out.println("\r\nОшибок не обнаружено. :)");
		else
        	System.out.println("\r\nОбнаружено ошибок: " + errorHandler.errorNum + "   :(");
    }

    protected Validator createValidator(String schemaFileName, PrintWriter errors) throws SAXException
    {
        SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        factory.setErrorHandler(new CheckerErrorHandler(errors));	// TODO
        Schema schema = factory.newSchema( EgissoChecker.class.getClassLoader().getResource(schemaFileName) );

        Validator validator = schema.newValidator();
        Locale ru = new Locale("ru");
        validator.setProperty("http://apache.org/xml/properties/locale", ru);
        validator.setErrorHandler(new CheckerErrorHandler(errors));
        
        return validator;
    }

    protected void printProtocol(PrintWriter protocol, CheckerErrorHandler errorHandler)
    {
    	protocol.println("\r\nСтатистика по ошибкам (число - описание ошибки):");
    
    	// сортируем ошибки по числу, вначале - самые частые
    	Map<String, AtomicInteger> map = sortByValue(errorHandler.errorsMap);
        
        for( Map.Entry<String, AtomicInteger> entry : map.entrySet() )
        {
        	protocol.println(entry.getValue() + "\t- " + getMessage(entry.getKey()) );
        }
    }
    
    public Map<String, AtomicInteger> sortByValue(Map<String, AtomicInteger> unsortMap) 
    {

        List<Map.Entry<String, AtomicInteger>> list = new LinkedList<Map.Entry<String, AtomicInteger>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<String, AtomicInteger>>() 
        {
           	public int compare(Map.Entry<String, AtomicInteger> o1, Map.Entry<String, AtomicInteger> o2) 
           	{
           		return o2.getValue().get() - o1.getValue().get();
           	}
        });
        
        Map<String, AtomicInteger> result = new LinkedHashMap<String, AtomicInteger>();
        for (Map.Entry<String, AtomicInteger> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }        

        return result;
    }

    protected void logError(PrintWriter errors, SAXParseException e)
    {
    	//errors.println(e.getLineNumber() + "\t" + e.getColumnNumber() + "\t" + getCode(e.getLocalizedMessage()) + "\t" + getMessage(e.getLocalizedMessage()) );
    	errors.println(e.getLineNumber() + "\t" + e.getColumnNumber() + "\t" + getMessage(e.getLocalizedMessage()) );
    }
    
    protected String getCode(String message)
    {
    	int i = message.indexOf(':');
    	if( i > 0 )
    		return message.substring(0, i).trim();
    	
    	return "";
    }
    
    protected String getMessage(String message)
    {
    	int i = message.indexOf(':');
    	if( i > 0 )
    		return message.substring(i+1).trim();
    	
    	return message;
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // CheckerErrorHandler
    // 
    
    public class CheckerErrorHandler implements ErrorHandler
    {
    	PrintWriter errors;
        Map<String, AtomicInteger> errorsMap = new HashMap<>();
        boolean isOK = true;
        int errorNum;

        public CheckerErrorHandler(PrintWriter errors)
        {
        	this.errors = errors;
        }
        
        public void error(SAXParseException e)
        {
        	logError(errors, e);

        	isOK = false;
            errorNum++;

            String detailMessage = e.getMessage();
            errorsMap.putIfAbsent(detailMessage, new AtomicInteger(0));
            errorsMap.get(detailMessage).incrementAndGet();
        }

        public void fatalError(SAXParseException exception)
        {
            isOK = false;
            System.out.println("Фатальная ошибка: " + exception);
        }

        public void warning(SAXParseException e)
        {
        	logError(errors, e);
        }
    }

}
