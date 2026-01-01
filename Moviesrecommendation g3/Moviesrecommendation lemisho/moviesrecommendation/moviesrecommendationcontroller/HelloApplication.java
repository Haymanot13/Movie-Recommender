package com.example.moviesrecommendation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Optional;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 650);
        
        // --- FIX: Pass HostServices to Controller ---
        HelloController controller = fxmlLoader.getController();
        controller.setHostServices(getHostServices());
        // --------------------------------------------

        stage.initStyle(StageStyle.UNDECORATED);
        
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setTitle("Movie Recommender");
        stage.setScene(scene);
        stage.show();

        // Prompt for Server IP
        TextInputDialog dialog = new TextInputDialog("localhost");
        dialog.setTitle("Server Connection");
        dialog.setHeaderText("Enter Server IP Address");
        dialog.setContentText("IP Address:");
        dialog.initOwner(stage);

        Optional<String> result = dialog.showAndWait();
        String serverIp = result.orElse("localhost");
        controller.connectToServer(serverIp);
    }

    public static void main(String[] args) {
        launch();
    }
}
