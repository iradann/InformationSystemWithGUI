/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemfx;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Version;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import org.apache.commons.codec.Charsets;
import org.xml.sax.SAXException;

/**
 *
 * @author user
 */
public class FXMLDocumentController implements Initializable {
    
    XMLReader r = new XMLReader();
    
    @FXML
    private Button buttonDown;

    @FXML
    private ComboBox comboxVersion;

    @FXML
    private ComboBox comboxProject;

    @FXML
    private ComboBox comboxUserName;

    @FXML
    private TextField textFieldURL;

    private ConnectionWithRedmine connection;

    public void initialize(URL url, ResourceBundle bn) {
        
        r.readXML("ProjectKey.xml");
        r.owners.forEach((ProjectOwner p) -> {
            r.usersNameList.add(p.getName());
        });
        ObservableList<String> userNames = FXCollections.observableArrayList(r.usersNameList);
        comboxUserName.setItems(userNames);
        
        
    }
    
    @FXML
    private void handleUserChoice() {
        
        if (!comboxUserName.getValue().toString().isEmpty()) {
            
        }
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
       
        System.out.println(r.projectIDsList);
        System.out.println(r.projectNameList);
        /*
        handleProjectChoice();
        List<Issue> issues = null;
        try {
            issues = connection.getIssues();
        } catch (RedmineException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<Integer> attachID = new ArrayList<>();

        for (Issue issue : issues) {
            if (issue.getStatusName() != "Closed" && issue.getStatusName() != "Approved") {
                System.out.println(issue.toString());
                connection.setVersionForCheck((String) comboxVersion.getValue(), issue);

            }
        }
        */
    }

    @FXML
    private void handleProjectChoice() {

        if (textFieldURL.getText().isEmpty()) {
            Properties.url = "http://www.hostedredmine.com";
        } else {
            Properties.url = textFieldURL.getText();
        }
        String valueProjectKey = comboxProject.getValue().toString();
        Collection<Version> versions = new ArrayList();

        ConnectionWithRedmine connection = new ConnectionWithRedmine(Properties.apiAccessKey, valueProjectKey, Properties.url);
        try {
            versions = connection.getVersions(valueProjectKey);
        } catch (RedmineException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Collection<String> versii = new ArrayList<>();
        if (versii != null || !versii.isEmpty()) {
            for (Version ver : versions) {
                versii.add(ver.getName());
            }
        }
        ObservableList<String> targetVersionLost = FXCollections.observableArrayList(versii);
        comboxVersion.setItems(targetVersionLost);
    }

    

    public Collection<String> readFile(String fileDir) {
        Collection<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(Paths.get(fileDir), Charsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lines;
    }

}
