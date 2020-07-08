/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.scratchnest;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;

/**
 * Main class
 *
 * @author jeev8
 */

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        double width = gd.getDisplayMode().getWidth();
        double height = gd.getDisplayMode().getHeight();
        Parent root = FXMLLoader.load(getClass().getResource("fxml/armsTabView.fxml"));
        primaryStage.setTitle("Armory Management System");
        primaryStage.setScene(new Scene(root, width/1.5, height/1.5));
        primaryStage.setMaximized(true);
        primaryStage.show();
       
    }


    public static void main(String[] args) {
        launch(args);
    }
}
