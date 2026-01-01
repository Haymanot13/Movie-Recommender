package com.example.moviesrecommendation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.util.function.Consumer;

public class VibeFinderController {

    private Consumer<String> onQuizFinished;
    private Runnable onBack;

    public void setOnQuizFinished(Consumer<String> onQuizFinished) {
        this.onQuizFinished = onQuizFinished;
    }

    public void setOnBack(Runnable onBack) {
        this.onBack = onBack;
    }

    @FXML
    void onAnswerSelected(ActionEvent event) {
        Button source = (Button) event.getSource();
        String selection = source.getUserData().toString();

        if (onQuizFinished != null) {
            onQuizFinished.accept(selection);
        }
    }

    @FXML
    void onBackClicked() {
        if (onBack != null) {
            onBack.run();
        }
    }
}