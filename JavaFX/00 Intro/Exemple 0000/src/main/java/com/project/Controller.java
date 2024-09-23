package com.project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class Controller {
    double primerNumero;
    double segundoNumero;


    double division;
    double multiplicacion;
    double suma;
    double resta;
    double punto;
    double resultado;
    double borrarTodo;
    double borrarUno;
    String operador;

    @FXML
    private Button boton1;
    private Button boton2;
    private Button boton3;
    private Button boton4;
    private Button boton5;
    private Button boton6;
    private Button boton7;
    private Button boton8;
    private Button boton9;
    private Button boton0;

    private Button botonSuma;
    private Button botonResta;
    private Button botonMultiplica;
    private Button botonDivide;
    private Button botonIgual;
    private Button botonBorrarUno;
    private Button botonBorrarTodo;
    private Button botonPunto;

    @FXML
    private TextField texto;

    @FXML
    private void addNum(ActionEvent event) {
        if (texto.getText().equals("...")){
            texto.setText(((Button) event.getSource()).getText());
        }else{
            texto.setText(texto.getText() + ((Button) event.getSource()).getText());
        }
    }

    @FXML
    private void divide(ActionEvent event) throws NumberFormatException{
        try {
            primerNumero = Double.parseDouble(texto.getText());
            operador = "/";
            texto.setText("");
        } catch (NumberFormatException e) {
            
        }
        
    }

    @FXML
    private void suma(ActionEvent event) throws NumberFormatException {
        try{
            primerNumero = Double.parseDouble(texto.getText());
            operador = "+";
            texto.setText("");
        } catch (NumberFormatException e) {
                
        }
    }

    @FXML
    private void multiplica(ActionEvent event) throws NumberFormatException {
        try{
            primerNumero = Double.parseDouble(texto.getText());
            operador = "*";
            texto.setText("");
        } catch (NumberFormatException e) {
                
    }
    }

    @FXML
    private void resta(ActionEvent event) throws NumberFormatException {
        try{
            primerNumero = Double.parseDouble(texto.getText());
            operador = "-";
            texto.setText("");
        } catch (NumberFormatException e) {
                
        }
    }

    @FXML
    private void punto(ActionEvent event) {
    }

    @FXML
    private void resultado(ActionEvent event) {
        if (operador == "-"){
            double segundoNumero = Double.parseDouble(texto.getText());
            resta = primerNumero - segundoNumero;
            texto.setText(String.valueOf(resta));
        }
        else if (operador == "+"){
            double segundoNumero = Double.parseDouble(texto.getText());
            suma = primerNumero + segundoNumero;
            texto.setText(String.valueOf(suma));
        }
        else if (operador == "*"){
            double segundoNumero = Double.parseDouble(texto.getText());
            multiplicacion = primerNumero * segundoNumero;
            texto.setText(String.valueOf(multiplicacion));
        }
        else if (operador == "/"){
            double segundoNumero = Double.parseDouble(texto.getText());
            division = primerNumero / segundoNumero;
            texto.setText(String.valueOf(division));
        }
        primerNumero = 0;
        segundoNumero = 0;
    }

    @FXML
    private void borraUno(ActionEvent event) {
        if (texto.getText().length() > 0 && !texto.getText().equals("...")) {
            texto.setText(texto.getText().substring(0, texto.getText().length() - 1));
        }
        if (texto.getText().isEmpty()) {
            texto.setText("...");
        }

    }

    @FXML
    private void borraTodo(ActionEvent event) {
        texto.setText("...");
    }
}
