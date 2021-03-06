/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.taskadapter.redmineapi.RedmineException;
/**
 *
 * @author user
 */
public class SystemFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {

            Parent root = FXMLLoader.load(getClass().getResource("FXMLDocument.fxml"));
            Scene scene = new Scene(root);
            stage.setTitle("Program for Redmine issues checking");
            stage.setScene(scene);
            stage.show();
        
    }

    public static void main(String[] args) throws RedmineException {
        if (args.length!=0) {
            Properties.projectKey = args[1];
            Properties.url = args[0];
            Properties.apiAccessKey = args[2];
        }
        // загрузка конфигов
        // открытие сцены
        launch(args);
    }

}
