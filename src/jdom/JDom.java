/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdom;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Dominique
 */
public class JDom extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
        Parent root = FXMLLoader.load(getClass().getResource("jDomXML.fxml"));
        
        
        stage.getIcons().add(new Image("/resources/jSmith.png"));
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.setTitle("Cura PDFs");
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
