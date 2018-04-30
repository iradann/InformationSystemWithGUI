/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemfx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

/**
 *
 * @author user
 */
public class XMLReader {

    public String userName = "";
    public String apiKey = "";
    public String projectKey = "";
    public ArrayList<String> usersNameList = new ArrayList<String>();
    public ArrayList<String> projectIDsList = new ArrayList<String>();
    public ArrayList<String> projectNameList = new ArrayList<String>();
    public ArrayList<String> apiKeysList = new ArrayList<String>();
    public ArrayList<ProjectOwner> owners = new ArrayList<ProjectOwner>();

    public void setUserName(String name) {
        this.userName = name;
    }

    public void setApiKey(String apikey) {
        this.apiKey = apikey;
    }

    public void setProjectKey(String projectkey) {
        this.projectKey = projectkey;
    }

    public void readXML(String filePath) {
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(filePath);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            //xpath for gettings names
            XPathExpression exprOwners = xpath.compile(".//supervisor/@name");
            NodeList nl = (NodeList) exprOwners.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nl.getLength(); i++) {
                String owner = nl.item(i).getTextContent();
                owners.add(new ProjectOwner(owner));
                //get all project by supervisor
                XPathExpression exprProjects = xpath.compile(".//supervisor[@name='" + owner + "']/project");
                NodeList hisProjects = (NodeList) exprProjects.evaluate(doc, XPathConstants.NODESET);
                for (int j = 0; j < hisProjects.getLength(); j++) {
                    Element e = (Element) hisProjects.item(j);
                    String name = e.getAttribute("name");
                    String id = e.getAttribute("id");
                    owners.get(i).getHisProjects().add(new Project(name, id));
                }
                //add apikey
                XPathExpression exprApiKey = xpath.compile(".//supervisor[@name='" + owner + "']/apiKey");
                NodeList hisApikey = (NodeList) exprApiKey.evaluate(doc, XPathConstants.NODESET);
                owners.get(i).setApiKey(hisApikey.item(0).getTextContent());
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XMLReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(XMLReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(XMLReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XMLReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        //тут собрали Arraylist владельцев проектов с их проектами и ключами.
        owners.forEach((ProjectOwner p) -> {
            System.out.println(p);
        });

    }
    
    private static String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }
}
