package com.developmentontheedge.egisso.checker;

import java.io.File;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.developmentontheedge.egisso.checker.EgissoChecker.XMLSchemaErrorHandler;

import junit.framework.TestCase;

public class EgissoCheckerTest extends TestCase
{
    protected static final String relativePath = "./src/test/resources/";

    protected void checkXSD(String fileName) throws SAXException
    {
        File file = new File(EgissoChecker.class.getClassLoader().getResource(fileName).getFile());
        assertTrue("File is missing: " + file.getAbsolutePath(), file.exists());

        SchemaFactory factory = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI );

        XMLSchemaErrorHandler errorHandler = new XMLSchemaErrorHandler();
        factory.setErrorHandler(errorHandler);
        Schema schema = factory.newSchema( EgissoChecker.class.getClassLoader().getResource(fileName) );
        
        assertTrue("Ошибка при загрузке XSD схемы " + fileName, errorHandler.isOK);
    }
    
    public void testXSDLocalMSZ() throws SAXException
    {
    	checkXSD(EgissoChecker.XSD_LOCAL_MSZ);
    }

    public void testXSDAssignmentFact() throws SAXException
    {
    	checkXSD(EgissoChecker.XSD_ASSIGNMENT_FACT);
    }
    
    public void testRuMessageBundle()
    {
        Locale ru = new Locale("ru");
        ResourceBundle bundle = PropertyResourceBundle.getBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages", ru);

        assertEquals( "cvc-type.3.1.3: Значение ''{1}'' элемента ''{0}'' недопустимо.", bundle.getString("cvc-type.3.1.3") );
        assertEquals( "cvc-complex-type.2.4.d: Неверное содержимое, начиная с элемента ''{0}''. Не ожидалось никакого дочернего элемента в данной позиции.", bundle.getString("cvc-complex-type.2.4.d") );
    }

    public void testExample() throws Exception
    {
        String fileName = "10.05.I-1.0.0.sample.1.xml"; 

        EgissoChecker checker = new EgissoChecker();
        checker.checkLocalMSZ(relativePath + fileName);
    }

}
