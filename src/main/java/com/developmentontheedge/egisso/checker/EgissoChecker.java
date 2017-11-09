package com.developmentontheedge.egisso.checker;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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
    public static final String XSD_LOCAL_MSZ = "10.05.I-1.0.3.xsd";

    protected static Validator createValidator(String schemaFileName) throws SAXException
    {
        File file = new File(EgissoChecker.class.getClassLoader().getResource(schemaFileName).getFile());

        SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );
        factory.setErrorHandler(new CheckerErrorHandler());
        Schema schema = factory.newSchema(file);

        Validator validator = schema.newValidator();
        validator.setErrorHandler(new CheckerErrorHandler());

        return validator;
    }

    public void checkLocalMSZ(File xml) throws SAXException, IOException
    {
        Validator validator = createValidator(XSD_LOCAL_MSZ);
        StreamSource xmlStream = new StreamSource(xml);

        try
        {
            validator.validate(xmlStream);
        }
        catch(SAXParseException saxException)
        {
            System.out.println(saxException);
        }
        ((CheckerErrorHandler)validator.getErrorHandler()).printErrorsSummary();
    }

    public static class CheckerErrorHandler implements ErrorHandler
    {
        boolean isOK = true;
        int error;
        Map<String, AtomicInteger> errors = new HashMap<>();

        public void error(SAXParseException e)
        {
            isOK = false;
            error++;
            //String path = extractPath(xml, exception.lineNumber, exception.columnNumber);
            System.out.println("error: " + e);

            String detailMessage = e.getMessage();
            errors.putIfAbsent(detailMessage, new AtomicInteger(0));
            errors.get(detailMessage).incrementAndGet();
        }

        public void fatalError(SAXParseException exception)
        {
            isOK = false;
            error++;
            //String path = extractPath(xml, exception.lineNumber, exception.columnNumber);
            System.out.println("fatal error: " + exception);
        }

        public void warning(SAXParseException exception)
        {
            System.out.println("warning: " + exception);
        }

        public void printErrorsSummary()
        {
            System.out.println("Errors summary");
            for( Map.Entry<String, AtomicInteger> entry : errors.entrySet() )
            {
                System.out.println( "count: " + entry.getValue() + " message: " + entry.getKey() );
            }
        }

    }

}
