package com.developmentontheedge.egisso.checker;

import java.io.File;
import java.io.IOException;

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

    public void testExample()
    {
        // example for PFR
        String fileName = "10.05.I-1.0.0.sample.1.xml"; 

        File file = new File(relativePath + fileName);
        assertTrue("File is missing: " + file.getAbsolutePath(), file.exists());
    }

    public void testLocalMSZ() throws SAXException, IOException
    {
        //String fileName = "10.05.I-1.0.0.sample.1.xml";
        String fileName = "local-MSZ.xml"; 
        File file = new File(relativePath + fileName);

        EgissoChecker checker = new EgissoChecker();
        checker.checkLocalMSZ(file);
    }

}
