package com.developmentontheedge.egisso.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
    private static final String VERSION = "0.2.1";

    public enum Scheme
    {
        XSD_LOCAL_MSZ( "urn://egisso-ru/msg/10.05.I/1.0.3", "10.05.I-1.0.3.xsd" ),
        XSD_ASSIGNMENT_FACT_1_0_0( "urn://egisso-ru/msg/10.06.S/1.0.0", "10.06.S-1.0.0.xsd" ),
        XSD_ASSIGNMENT_FACT_1_0_1( "urn://egisso-ru/msg/10.06.S/1.0.1", "10.06.S-1.0.1.xsd" ),
        XSD_APP_RU_OSZ( "urn://egisso-ru/msg/10.11.I/1.0.1", "10.11.I-1.0.0.xsd" );

        final String urn;
        final String xsd;

        Scheme(String urn, String xsd)
        {
            this.urn = urn;
            this.xsd = xsd;
        }
    }

    public static void main(String[] args) throws Exception
    {
        // TODO check dir in arguments
        String[] fileNames;
        if( args.length > 0 )
        {
            fileNames = args;
        }
        else
        {
            fileNames = new java.io.File( "." ).list();
        }

        EgissoChecker checker = new EgissoChecker();
        checker.check( fileNames, true );
    }

    public Map<String, AtomicInteger> check(String[] fileNames, boolean createFiles) throws Exception
    {
        Locale.setDefault(new Locale("ru"));

        System.out.println("Чекер для ЕГИССО, версия: " + VERSION);

        Set<Map<String, AtomicInteger>> totalErrors = new HashSet<>();
        int counter = 0;
        for( String fileName : fileNames )
        {
            if( fileName.toLowerCase().equals( "egisso-checker" + VERSION + ".jar" ) )
            {
                continue;
            }

            if( !fileName.toLowerCase().endsWith( ".xml" ) )
            {
                System.out.println("\r\nПропускаем не xml файл " + fileName );
                continue;
            }

            counter++;
            try
            {
                Map<String, AtomicInteger> errorsMap = checkFile( fileName, createFiles );
                if( errorsMap != null && !errorsMap.isEmpty())
                    totalErrors.add(errorsMap);
            }
            catch(Throwable t)
            {
                System.out.println("Непредвиденная ошибка: " + t);
                t.printStackTrace();
            }
        }

        if( createFiles && counter > 1 ) // create summarized protocol
        {
            Map<String, AtomicInteger> errorsMap = sumByKey(totalErrors);
            CheckerErrorHandler errorHandler = new EgissoChecker().new CheckerErrorHandler( errorsMap );
            PrintWriter protocol = new PrintWriter( new File( "protocol.txt" ) );
            errorHandler.printProtocol(protocol);
            protocol.close();

            return errorsMap;
        }
        else if( totalErrors.size() == 1 )
        {
            return totalErrors.iterator().next();
        }

        return null;
    }

    protected String createInfo(File fileToCheck, File fileProtocol, File fileErrors, Scheme scheme)
    {
        StringBuilder info = new StringBuilder();

        info.append("")
            .append("\r\nПроверяемый файл:  " + fileToCheck.getName()) // AbsolutePath())
            .append("\r\nФайл протокола:    " + ( fileProtocol == null ? "не создается" : fileProtocol.getName() ) )
            .append("\r\nФайл с ошибками:   " + ( fileErrors == null ? " не создается" : fileErrors.getName() ) )
            .append("\r\nXSD схема для проверки: " + scheme.xsd);

        return info.toString();
    }

    protected Scheme defineScheme(File fileToCheck) throws Exception 
    {
        InputStream is = null;
        BufferedReader buf = null;
        try
        {
            is = new FileInputStream(fileToCheck);
            buf = new BufferedReader(new InputStreamReader(is)); 

            for(int i=0; i<20; i++)
            {
                String line = buf.readLine(); 
                if( line == null )
                    break;

                if( line.contains( Scheme.XSD_LOCAL_MSZ.urn ) )
                    return Scheme.XSD_LOCAL_MSZ;
                else if( line.contains( Scheme.XSD_ASSIGNMENT_FACT_1_0_0.urn ) )
                    return Scheme.XSD_ASSIGNMENT_FACT_1_0_0;
                else if( line.contains( Scheme.XSD_ASSIGNMENT_FACT_1_0_1.urn ) )
                    return Scheme.XSD_ASSIGNMENT_FACT_1_0_1;
                else if( line.contains( Scheme.XSD_APP_RU_OSZ.urn ) )
                    return Scheme.XSD_APP_RU_OSZ;
            }
        }
        finally
        {
            if( buf != null )
                buf.close();
            if( is != null )
                is.close();
        }

        System.out.println("\r\nНеправильный формат файла - не найдена подходящая схема для проверки.");
        System.out.println("Файл должен содержать подстроку: ");
        System.out.println(" - 'urn://egisso-ru/msg/10.05.I/1.0.3' - для файла с локальным классификатором услуг (ЛКМСЗ) или");
        System.out.println(" - 'urn://egisso-ru/msg/10.06.S/1.0.0' - для файла с фактами назначений или");
        System.out.println(" - 'urn://egisso-ru/msg/10.11.I/1.0.1' - для файла со списками участников.");

        return null;
    }

    public Map<String, AtomicInteger> checkFile(String fileName, boolean createFiles) throws Exception
    {
        File fileToCheck = new File(fileName);
        if( !fileToCheck.exists() )
        {
            String error = "\r\nНе найден файл для проверки: " + fileToCheck.getAbsolutePath();
            System.out.println(error);
            return Collections.singletonMap(error, new AtomicInteger(1));
        }

        Scheme scheme = defineScheme(fileToCheck);
        if( scheme == null )
        {
            String error = "\r\nНе найдена схема для проверки файла: " + fileToCheck.getAbsolutePath();
            System.out.println(error);
            return Collections.singletonMap(error, new AtomicInteger(1));
        }

        File fileProtocol = null;
        File fileErrors   = null;
        if( createFiles )
        {
            fileProtocol = new File(fileName + ".prt");
            fileErrors   = new File(fileName + ".err");
        }
        String info = createInfo(fileToCheck, fileProtocol, fileErrors, scheme);
        System.out.println(info);

        System.out.println("\r\nНачинаем проверку ...");

        Validator validator = createValidator(scheme.xsd, fileErrors);
        StreamSource xmlStream = new StreamSource(fileToCheck);

        validator.validate(xmlStream);

        CheckerErrorHandler errorHandler = (CheckerErrorHandler)validator.getErrorHandler();

        if( fileProtocol  != null )
        {
            PrintWriter protocol = new PrintWriter(fileProtocol);
            protocol.println(info);
            errorHandler.printProtocol(protocol);
            protocol.close();
        }

        if( errorHandler.errorsMap.isEmpty() )
        {
            System.out.println("\r\nОшибок не обнаружено.");
            return null;
        }
        else
        {
            System.out.println("\r\nОбнаружено ошибок: " + errorHandler.errorNum );

            StringWriter result = new StringWriter();
            errorHandler.printProtocol( new PrintWriter(result) );
            return errorHandler.getErrorsMap();
        }
    }

    protected Validator createValidator(String xsd, File fileErrors) throws SAXException, FileNotFoundException
    {
        SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        factory.setErrorHandler(new XMLSchemaErrorHandler());
        Schema schema = factory.newSchema( EgissoChecker.class.getClassLoader().getResource(xsd) );

        Validator validator = schema.newValidator();
        Locale ru = new Locale("ru");
        validator.setProperty("http://apache.org/xml/properties/locale", ru);
        validator.setErrorHandler(new CheckerErrorHandler(fileErrors));

        return validator;
    }

    public static Map<String, AtomicInteger> sortByValue(Map<String, AtomicInteger> unsortMap)
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

    public static Map<String, AtomicInteger> sumByKey(Set<Map<String, AtomicInteger>> maps)
    {
        Map<String, AtomicInteger> result = new LinkedHashMap<String, AtomicInteger>();
        for(Map<String, AtomicInteger> map : maps) 
        {
            for(Map.Entry<String, AtomicInteger> entry : map.entrySet() )
            {
                String key = entry.getKey();
                if( !result.containsKey( key ) )
                    result.put(key, entry.getValue());
                else
                    result.get(key).addAndGet( entry.getValue().get() );
            }
        }

        return sortByValue(result);
    }

    protected void logError(PrintWriter errors, SAXParseException e)
    {
        if( errors != null )
            errors.println(e.getLineNumber() + "\t" + e.getColumnNumber() + "\t" + getCode(e.getLocalizedMessage()) + "\t" + getMessage(e.getLocalizedMessage()) );
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

    public class CheckerErrorHandler implements ErrorHandler
    {
        private File fileErrors;
        private PrintWriter errors;
        private Map<String, AtomicInteger> errorsMap = new HashMap<>();
        private int errorNum;

        public CheckerErrorHandler(File fileErrors)
        {
            this.fileErrors = fileErrors;
        }

        public CheckerErrorHandler(Map<String, AtomicInteger> errorsMap)
        {
            this.errorsMap = errorsMap;
        }

        public Map<String, AtomicInteger> getErrorsMap()
        {
            return errorsMap;
        }

        public void error(SAXParseException e)
        {
            createErrorFile();
            logError(errors, e);

            errorNum++;

            String detailMessage = e.getMessage();
            errorsMap.putIfAbsent(detailMessage, new AtomicInteger(0));
            errorsMap.get(detailMessage).incrementAndGet();
        }

        public void fatalError(SAXParseException exception)
        {
            System.out.println("Фатальная ошибка: " + exception);
        }

        public void warning(SAXParseException e)
        {
            createErrorFile();
            logError(errors, e);
        }

        protected void printProtocol(PrintWriter protocol)
        {
            if( errors != null )
                errors.close();

            if( errorsMap.isEmpty() )
            {
                protocol.println("\r\nОшибок не обнаружено.");
                return;
            }

            protocol.println("\r\nСтатистика по ошибкам (число - описание ошибки):");

            // сортируем ошибки по числу, вначале - самые частые
            Map<String, AtomicInteger> map = sortByValue(errorsMap);

            for( Map.Entry<String, AtomicInteger> entry : map.entrySet() )
            {
                protocol.println(entry.getValue() + "\t- " + getMessage(entry.getKey()) );
            }
        }

        private void createErrorFile()
        {
            try
            {
                if( fileErrors != null && errors == null ) 
                    this.errors = new PrintWriter( fileErrors );
            }
            catch( FileNotFoundException ex )
            {
                throw new RuntimeException( ex );
            }
        }
        
    }

    public static class XMLSchemaErrorHandler implements ErrorHandler
    {
        boolean isOK = true;

        public void error(SAXParseException e)
        {
            isOK = false;
            System.out.println("Ошибка загрузки схемы: " + e);
        }

        public void fatalError(SAXParseException e)
        {
            isOK = false;
            System.out.println("Фатальная ошибка загрузки схемы: " + e);
        }

        public void warning(SAXParseException e)
        {
            System.out.println("Предупреждение загрузки схемы: " + e);
        }
    }

}
