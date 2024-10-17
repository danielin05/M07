package com.project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ControllerChat {

    @FXML
    public TextFlow aiText = new TextFlow();
    public BorderPane borderPane;
    public Label suicideAiLabel;

    public void addText(String text) {
        if (aiText == null) {
            System.out.println("El TextFlow no está iniciado.");
            return;
        }
        // Crear y añadirlo al TextFlow
        Text message = new Text(text);
        int num = 0;
        num = text.length();
        aiText.getChildren().add(message);
        setHeight(num,aiText.getHeight());
        System.out.println(borderPane.getHeight());

    }

    public void setHeight(int num,double height) {
        borderPane.setPrefHeight(aiText.getHeight() + suicideAiLabel.getHeight() + suicideAiLabel.getHeight());
        if (height == 0) {
            borderPane.setPrefHeight((num/3)+5);
        }
    }

}
