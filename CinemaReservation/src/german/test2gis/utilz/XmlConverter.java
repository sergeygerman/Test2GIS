package german.test2gis.utilz;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by s_german on 16.12.2018.
 * Converter Xml to String and String to Xml (org.w3c.dom.Document)
 */
public class XmlConverter {
    private static final TransformerFactory tf;
    private static final DocumentBuilderFactory dbf;

    static {
        tf = TransformerFactory.newInstance();
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
    }

    public static String xmlToString(Document xml) {
        try {
            Transformer transformer;
            synchronized (tf) {
                transformer = tf.newTransformer();
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(xml), new StreamResult(writer));
            String result = writer.getBuffer().toString();
            writer.close();
            return result;
        }catch (Exception ex){
            throw new RuntimeException("Failed to convert XML-document to string", ex);
        }
    }

    public static Document stringToDocument(String source) {
        if (source == null || source.isEmpty())
            throw new IllegalArgumentException("XML string is empty");
        try {
            DocumentBuilder builder;
            synchronized (dbf) {
                builder = dbf.newDocumentBuilder();
            }

            StringReader sr = new StringReader(source);
            Document result = builder.parse(new InputSource(sr));
            sr.close();
            return result;
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to build XML Document from a string source", ex);
        }
    }
}
