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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import org.apache.commons.codec.Charsets;
import org.xml.sax.SAXException;

/**
 *
 * @author user
 */
public class FXMLDocumentController implements Initializable {

    XMLReader r = new XMLReader();
    ArrayList<Project> projects = new ArrayList<Project>();

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

    @FXML
    private TextField textFieldJavaErrorAmount;

    @FXML
    private CheckBox checkBoxPerevod;

    @FXML
    private ComboBox comboBoxPythonRating;

    @FXML
    private RadioButton radioButtonStatusClosed;

    @FXML
    private RadioButton radioButtonStatusApproved;

    @FXML
    private RadioButton radioButtonAppointForStudent;

    @FXML
    private RadioButton radioButtonAppointForProfessor;

    @FXML
    private CheckBox checkBoxJavaErrScan;

    @FXML
    private CheckBox checkBoxPythonRateScan;

    private ConnectionWithRedmine connectionToRedmine;
    private RedmineJournalsReader journalReader;

    public void initialize(URL url, ResourceBundle bn) {

        r.readXML("ProjectKey.xml");
        r.owners.forEach((ProjectOwner p) -> {
            r.usersNameList.add(p.getName());
        });
        ObservableList<String> userNames = FXCollections.observableArrayList(r.usersNameList);
        comboxUserName.setItems(userNames);

        ArrayList<Float> ratingValues = new ArrayList<Float>();
        for (float a = 10; a >= -2.00; a = (float) (a - 0.25)) {
            ratingValues.add(a);
        }

        ObservableList<Float> pythonRatingValues = FXCollections.observableArrayList(ratingValues);
        comboBoxPythonRating.setItems(pythonRatingValues);

        final ToggleGroup groupIssueStatus = new ToggleGroup();
        radioButtonStatusClosed.setToggleGroup(groupIssueStatus);
        radioButtonStatusClosed.setSelected(true);
        radioButtonStatusClosed.requestFocus();
        radioButtonStatusApproved.setToggleGroup(groupIssueStatus);

        final ToggleGroup groupAppointment = new ToggleGroup();
        radioButtonAppointForStudent.setToggleGroup(groupAppointment);
        radioButtonAppointForStudent.setSelected(true);
        radioButtonAppointForStudent.requestFocus();
        radioButtonAppointForProfessor.setToggleGroup(groupAppointment);
    }

    @FXML
    private void handleUserChoice() {

        if (!comboxUserName.getValue().toString().isEmpty()) {
            for (ProjectOwner o : r.owners) {
                if (comboxUserName.getValue().toString().equals(o.getName())) {
                    projects = o.getHisProjects();
                    Properties.apiAccessKey = o.getApiKey();
                    break;
                }
            }
            for (Project p : projects) {
                r.projectIDsList.add(p.getId());
                r.projectNameList.add(p.getProjectName());
            }
        }
        ObservableList<String> projectNames = FXCollections.observableArrayList(r.projectNameList);
        comboxProject.setItems(projectNames);
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {

        if (!comboxUserName.getValue().toString().isEmpty()
                && !comboxProject.getValue().toString().isEmpty()
                && !comboxVersion.getValue().toString().isEmpty()) {

            List<Issue> issues = null;
            ArrayList<String> journals = null;
            try {
                issues = connectionToRedmine.getIssues();
            } catch (RedmineException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }

            for (Issue issue : issues) {
                if (issue.getStatusName() != "Closed" && issue.getStatusName() != "Approved") {
                    System.out.println(issue.toString());
                    connectionToRedmine.setVersionForCheck((String) comboxVersion.getValue(), issue);
                    journals = journalReader.getJournals(issue.getId().toString());
                    for (String journal : journals) {
                        if (!journal.equals(comboxUserName.getValue().toString())) {
                            connectionToRedmine.setStudentName(journal);
                            break;
                        }
                    }

                }
            }

            connectionToRedmine.setProfessorName(comboxUserName.getValue().toString());
        }

    }

    @FXML
    private void handleProjectChoice() {

        if (textFieldURL.getText().isEmpty()) {
            Properties.url = "http://www.hostedredmine.com";
        } else {
            Properties.url = textFieldURL.getText();
        }

        if (!comboxProject.getValue().toString().isEmpty()) {
            for (Project p : projects) {
                if (p.getProjectName().equals(comboxProject.getValue().toString())) {
                    Properties.projectKey = p.getId();
                    break;
                }
            }
        }

        Collection<Version> versions = new ArrayList();

        connectionToRedmine = new ConnectionWithRedmine(Properties.apiAccessKey, Properties.projectKey, Properties.url);
        journalReader = new RedmineJournalsReader(Properties.url, Properties.apiAccessKey);
        try {
            versions = connectionToRedmine.getVersions(Properties.projectKey);
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

    @FXML
    private void handleJavaErrorScanCheck() {
        if (checkBoxJavaErrScan.isSelected()) {
            connectionToRedmine.setProverka(textFieldJavaErrorAmount.getText().toString());
        }
        //connectionToRedmine.setJavaErrorsAmount(Integer.parseInt(textFieldJavaErrorAmount.getText()));

    }
    @FXML
    private void handlePyhtonRatingScanCheck() {
        if (checkBoxPythonRateScan.isSelected()) {
            connectionToRedmine.setRating(Float.parseFloat(comboBoxPythonRating.getValue().toString()));
        }
    }

    @FXML
    private void handlePerevodCheck() {

        if (checkBoxPerevod.isSelected()) {
            connectionToRedmine.setPerevod(true);
        } else {
            connectionToRedmine.setPerevod(false);
        }
    }

    @FXML
    private void handleIssueStatusRadioButton() {

        if (radioButtonStatusApproved.isSelected()) {
            connectionToRedmine.setIssueStatus(4);
        } else if (radioButtonStatusClosed.isSelected()) {
            connectionToRedmine.setIssueStatus(5);
        }
    }

    @FXML
    private void handeRadioButtonAppointments() {

        if (radioButtonAppointForStudent.isSelected()) {
            connectionToRedmine.setAssigneeName(connectionToRedmine.getStudentName());
        } else if (radioButtonAppointForProfessor.isSelected()) {
            connectionToRedmine.setAssigneeName(connectionToRedmine.getProfessorName());
        }
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
