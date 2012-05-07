package pt.uc.dei.fincos.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ConnectionsFileParser {
    /**
     * Opens and parses a FINCoS xml file containing connections configuratios-
     *
     * @param path  Path to xml file containing configured connections
     * @return      the list of configured connections
     *
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static ConnectionConfig[] getConnections(String path) throws ParserConfigurationException, SAXException, IOException {
        ConnectionConfig[] ret = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        File configFile = new File(path);
        Document doc = builder.parse(configFile);
        Element xmlFileRoot = doc.getDocumentElement();
        if (xmlFileRoot != null) {
            Element connectionsList = (Element) xmlFileRoot.getElementsByTagName("Connections").item(0);

            NodeList connections = connectionsList.getElementsByTagName("Connection");
            ret = new ConnectionConfig[connections.getLength()];
            Element conn;
            String alias;
            String typeStr;
            int type;
            String customProp;
            for (int i = 0; i < connections.getLength(); i++) {
                LinkedHashMap<String, String> connProps = new LinkedHashMap<String, String>();
                conn = (Element) connections.item(i);
                alias = conn.getAttribute("alias");
                typeStr = conn.getAttribute("type");
                if (typeStr.equalsIgnoreCase("CEP")) {
                    customProp = conn.getAttribute("engine");
                    connProps.put("engine", customProp);
                    type = ConnectionConfig.CEP_ADAPTER;
                } else if (typeStr.equalsIgnoreCase("JMS")) {
                    customProp = conn.getAttribute("cfName");
                    connProps.put("cfName", customProp);
                    type = ConnectionConfig.JMS;
                } else {
                    throw new InvalidParameterException("Invalid connection type \"" + typeStr + "\".");
                }
                Element propList = (Element) conn.getElementsByTagName("Properties").item(0);
                NodeList properties = propList.getElementsByTagName("Property");
                for (int j = 0; j < properties.getLength(); j++) {
                    Element prop = (Element) properties.item(j);
                    String name = prop.getAttribute("name");
                    String value = prop.getAttribute("value");
                    connProps.put(name, value);
                }
                ret[i] = new ConnectionConfig(alias, type, connProps);
            }
        }

        return ret;
    }

    /**
     * Saves a list of connection configurations into a XML file.
     *
     * @param connCfgs  the list of connections
     * @param filePath  the path of XML file
     *
     * @throws ParserConfigurationException     if an error occurs during creation of the XML document
     * @throws IOException                      if an error occurs when writing data to the file at disk
     * @throws TransformerException
     */
    public static void saveToFile(ConnectionConfig[] connCfgs, String filePath) throws ParserConfigurationException, IOException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("FINCoS");
        doc.appendChild(root);

        Element connList = doc.createElement("Connections");
        root.appendChild(connList);

        Element conn;
        for (ConnectionConfig c : connCfgs) {
            conn = doc.createElement("Connection");
            conn.setAttribute("alias", c.alias);
            conn.setAttribute("type", c.type == ConnectionConfig.CEP_ADAPTER ? "CEP" : "JMS");
            if (c.type == ConnectionConfig.CEP_ADAPTER) {
                conn.setAttribute("engine", c.properties.get("engine"));
            } else {
                conn.setAttribute("cfName", c.properties.get("cfName"));
            }
            Element properties = doc.createElement("Properties");
            for (Map.Entry<String, String> prop : c.properties.entrySet()) {
                Element property = doc.createElement("Property");
                property.setAttribute("name", prop.getKey());
                property.setAttribute("value", prop.getValue());
                properties.appendChild(property);
            }
            conn.appendChild(properties);
            connList.appendChild(conn);
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath), false));
        bw.write(ConfigurationParser.fromXMLDocToString(doc));
        bw.flush();
        bw.close();
    }

    /**
     * Creates a empty connections file.
     *
     * @param filePath  the path of XML file
     *
     * @throws ParserConfigurationException     if an error occurs during creation of the XML document
     * @throws IOException                      if an error occurs when writing data to the file at disk
     * @throws TransformerException
     */
    public static void createEmptyFile(String filePath) throws ParserConfigurationException, IOException, TransformerException {
        File f = new File(filePath);
        if (!f.exists()) {
            f.mkdirs();
            f.createNewFile();
        }
        saveToFile(new ConnectionConfig[0], filePath);
    }
}
