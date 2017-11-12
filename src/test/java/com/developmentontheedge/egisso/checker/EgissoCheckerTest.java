package com.developmentontheedge.egisso.checker;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class EgissoCheckerTest extends TestCase
{
    protected static final String relativePath = "./src/test/resources/";

    public void testXSDLocalMSZ()
    {
        String fileName = EgissoChecker.XSD_LOCAL_MSZ;
        File file = new File(EgissoChecker.class.getClassLoader().getResource(fileName).getFile());
        assertTrue("File is missing: " + file.getAbsolutePath(), file.exists());
    }

    public void testRuMessageBundle()
    {
        Locale ru = new Locale("ru");
    	ResourceBundle bundle = PropertyResourceBundle.getBundle("com.sun.org.apache.xerces.internal.impl.msg.XMLSchemaMessages", ru);
    	String msg = bundle.getString("cvc-type.3.1.3");
    	assertEquals(msg, "cvc-type.3.1.3: Значение ''{1}'' элемента ''{0}'' недопустимо.");
    	
    	System.out.println(bundle.getString("cvc-complex-type.2.4.d"));
    }
    
    public void testExample()
    {
        // example for PFR
        String fileName = "10.05.I-1.0.0.sample.1.xml"; 

        File file = new File(relativePath + fileName);
        assertTrue("File is missing: " + file.getAbsolutePath(), file.exists());
    }

    public void testLocalMSZ() throws SAXException, IOException
    {
        String fileName = "10.05.I-1.0.0.sample.1.xml";
        //String fileName = "local-MSZ.xml"; 

        EgissoChecker checker = new EgissoChecker();
        checker.checkLocalMSZ(relativePath + fileName);
    }

}
