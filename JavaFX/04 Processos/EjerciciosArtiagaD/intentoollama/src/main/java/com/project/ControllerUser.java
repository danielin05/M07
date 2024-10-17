package com.project;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ControllerUser {
    @FXML
    public TextFlow userText = new TextFlow();

    public String establecerTexto(String text) {
        Text message = new Text(text);
        message.wrappingWidthProperty().bind(userText.widthProperty());
        userText.getChildren().add(message);
        return text;
    }
}
