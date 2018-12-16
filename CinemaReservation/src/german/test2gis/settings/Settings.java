package german.test2gis.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by s_german on 15.12.2018.
 * Config reader class
 */
public class Settings {
    private final static String CONFIG_LOCATION = "config.xml";
    private final static String sName="\t[SettingsReader] ";
    private Logger logger;
    private OracleDBSettings oracleDBSettings;
    private Boolean jsonMode;

    private static volatile Settings instance = null;
    public static Settings getInstance(){
        if (instance == null) {
            synchronized(Settings.class) {
                instance = new Settings();
            }
        }
        return instance;
    }

    private Settings(){
        this.logger = LogManager.getLogger(this.getClass());
        ReadSettings();
    }

    private void ReadSettings(){
        try {
            logger.info(sName + "Read settings...");

            Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(CONFIG_LOCATION);

            //get the root element
            Element rootElem = dom.getDocumentElement();

            //get settings for Storage
            Node nStorage = rootElem.getElementsByTagName("Storage").item(0);
            String host = ((Element)nStorage).getElementsByTagName("Host").item(0).getTextContent();
            Integer port = Integer.parseInt(((Element)nStorage).getElementsByTagName("Port").item(0).getTextContent());
            String sid = ((Element)nStorage).getElementsByTagName("Sid").item(0).getTextContent();
            String username = ((Element)nStorage).getElementsByTagName("Username").item(0).getTextContent();
            String passwd = ((Element)nStorage).getElementsByTagName("Password").item(0).getTextContent();

            oracleDBSettings = new OracleDBSettings(host, port, sid, username, passwd);

            jsonMode = false;
            if(rootElem.getElementsByTagName("JsonMode").getLength() > 0)
                jsonMode = Boolean.valueOf(rootElem.getElementsByTagName("JsonMode").item(0).getTextContent());

            logger.info(sName + "Reading settings completed.\n");
        }catch(ParserConfigurationException pce) {
            this.logger.error(sName + "ParserConfiguration Error: " + pce.getMessage());
        }catch(SAXException se) {
            this.logger.error(sName + "SAX Error: " + se.getMessage());
        }catch(IOException ioe) {
            this.logger.error(sName + "IO Error: " + ioe.getMessage());
        }catch (Exception e){
            this.logger.error(sName + "Unexpected Error: " + e.getMessage());
        }
    }

    public OracleDBSettings getOracleDBSettings(){
        return this.oracleDBSettings;
    }

    public Boolean getJsonMode() {
        return jsonMode;
    }
}
