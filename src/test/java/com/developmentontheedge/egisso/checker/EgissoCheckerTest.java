package com.developmentontheedge.egisso.checker;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

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

        assertNotNull( schema );
        assertTrue("Ошибка при загрузке XSD схемы " + fileName, errorHandler.isOK);
    }

    public void testXSDLocalMSZ() throws SAXException
    {
        checkXSD(EgissoChecker.Scheme.XSD_LOCAL_MSZ.xsd);
    }

    public void testXSDAssignmentFact() throws SAXException
    {
        checkXSD(EgissoChecker.Scheme.XSD_ASSIGNMENT_FACT_1_0_0.xsd);
        checkXSD(EgissoChecker.Scheme.XSD_ASSIGNMENT_FACT_1_0_1.xsd);
    }

    public void testRuMessageBundle()
    {
        Locale ru = new Locale("ru");
        ResourceBundle bundle = PropertyResourceBundle.getBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages", ru);

        assertEquals( "cvc-type.3.1.3: Значение ''{1}'' элемента ''{0}'' недопустимо.", bundle.getString("cvc-type.3.1.3") );
        assertEquals( "cvc-complex-type.2.4.d: Неверное содержимое, начиная с элемента ''{0}''. Не ожидалось никакого дочернего элемента в данной позиции.", bundle.getString("cvc-complex-type.2.4.d") );
    }

    public void testLKMSZ() throws Exception
    {
        String fileName = "10.05.I-1.0.0.sample.1.xml";

        EgissoChecker checker = new EgissoChecker();
        assertNull( checker.checkFile(relativePath + fileName, false) );
    }

    public void testAssignmentFact() throws Exception
    {
        String fileName = "10.06.S-1.0.0.test.xml";

        EgissoChecker checker = new EgissoChecker();
        assertNull( checker.checkFile(relativePath + fileName, false) );
    }

    public void testAppRuOsz() throws Exception
    {
        String fileName = "10.11.I-1.0.0.test.xml";

        EgissoChecker checker = new EgissoChecker();
        assertNull( checker.check(new String[] { relativePath + fileName }, false) );
    }

    public void testErrors() throws Exception
    {
        String fileName = "10.06.S-1.0.0.testErrors.xml";

        EgissoChecker checker = new EgissoChecker();
        String error = "cvc-complex-type.2.4.a: Неверное содержимое, начиная с элемента 'amount'. Содержимое должно соответствовать '{\"urn://egisso-ru/types/assignment-fact/1.0.1\":amount}'.";
        Map<String, AtomicInteger> errors = checker.checkFile(relativePath + fileName, false);
        assertEquals( 1, errors.size() );
        assertEquals( error, errors.keySet().iterator().next() );
        assertEquals( 1, errors.values().iterator().next().get() );

        errors = checker.check(new String[] { relativePath + fileName }, false);
        assertEquals( 1, errors.size() );
        assertEquals( error, errors.keySet().iterator().next() );
        assertEquals( 1, errors.values().iterator().next().get() );
    }

    public void testSomeFiles() throws Exception
    {
        String[] fileNames = { relativePath + "10.06.S-1.0.0.testErrors.xml", relativePath + "10.06.S-1.0.0.test.xml" };

        EgissoChecker checker = new EgissoChecker();
        String error = "cvc-complex-type.2.4.a: Неверное содержимое, начиная с элемента 'amount'. Содержимое должно соответствовать '{\"urn://egisso-ru/types/assignment-fact/1.0.1\":amount}'.";
        Map<String, AtomicInteger> errors = checker.check(fileNames, false);
        assertEquals( 1, errors.size() );
        assertEquals( error, errors.keySet().iterator().next() );
        assertEquals( 1, errors.values().iterator().next().get() );
    }

}
