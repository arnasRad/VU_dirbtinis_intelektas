package com.arnasrad.labyrinth;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("view/main-view.fxml"));
        primaryStage.setTitle("Labyrinth");
//        Scene scene = new Scene(root, 300, 275);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(this.getClass().getResource("style/style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
