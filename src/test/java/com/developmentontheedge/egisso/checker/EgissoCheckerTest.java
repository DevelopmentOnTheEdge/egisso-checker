package com.developmentontheedge.egisso.checker;

import java.io.File;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

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
