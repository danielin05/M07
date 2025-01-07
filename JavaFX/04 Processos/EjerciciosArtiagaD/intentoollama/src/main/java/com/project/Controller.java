package com.project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;

import org.json.JSONObject;

public class Controller{

    @FXML
    private Button sendButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button filesButton;
    @FXML
    private TextField writeHere;
    @FXML
    private VBox box;
    @FXML
    private ScrollPane scroll;
    @FXML
    private ImageView image;

    private CompletableFuture<Void> streamRequest;
    private CompletableFuture<HttpResponse<String>> completeRequest;
    private AtomicBoolean isCancelled = new AtomicBoolean(false);
    private InputStream currentInputStream;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<?> streamReadingTask;
    private boolean isFirst = true;
    private Map<Node, ControllerChat> layoutControllerMap = new HashMap<>();
    private String b64Image;

    @FXML
    public void initialize() {
        // Configura el evento cuando se presiona una tecla en el campo de texto
        writeHere.setOnKeyPressed(event -> {
            // Si se presiona "Enter"
            if (event.getCode() == KeyCode.ENTER) {
                // Si el campo de texto no está vacío
                if (!writeHere.getText().trim().isEmpty()) {
                    sendAmessage(); // Envía el mensaje
                }
            }
        });
    }
    
    // Método que se encarga de enviar el mensaje y mostrarlo en la interfaz
    @FXML
    public void sendAmessage() {
        try {
            // Carga el diseño del mensaje desde el archivo FXML
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/assets/UserMssg.fxml")));
            Node layout2 = loader.load(); // Carga el diseño en un nodo

            // Obtiene el controlador del layout
            ControllerUser controllerUser = loader.getController();
            controllerUser.establecerTexto(writeHere.getText()); // Establece el texto del mensaje en el controlador
            box.getChildren().add(layout2); // Agrega el nuevo mensaje a la caja

            // Verifica si hay una imagen cargada
            if (b64Image != null) {
                imageMessage(writeHere.getText(), b64Image); // Envía el mensaje con imagen
            } else {
                sendMessagetoAI(writeHere.getText()); // Envía solo el mensaje
            }
            scroll.layout(); // Actualiza el diseño del ScrollPane
            scroll.setVvalue(1.0); // Desplaza la vista hacia abajo
        } catch (IOException e) {
            e.printStackTrace(); // Imprime el error en caso de una excepción de entrada/salida
        } catch (InterruptedException e) {
            throw new RuntimeException(e); // Lanza una excepción si se interrumpe
        }

        writeHere.clear(); // Limpia el campo de texto
        isCancelled.set(false); // Restablece el estado de cancelación
    }

    // Esto es para cargar la imagen
    @FXML
    private void fileLoad() {
        File initialDirectory = new File("./");
        FileChooser fileChooser = new FileChooser();
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) filesButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            image.setVisible(true);
            image.setImage(new Image(selectedFile.toURI().toString()));
            try {
                // Read the image file
                BufferedImage bufferedImage = ImageIO.read(selectedFile);

                // Create a ByteArrayOutputStream to store the image bytes
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                // Write the image to the output stream in PNG format
                ImageIO.write(bufferedImage, "png", outputStream);

                // Get the byte array
                byte[] imageBytes = outputStream.toByteArray();

                // Encode to Base64
                b64Image = Base64.getEncoder().encodeToString(imageBytes);

                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void imageMessage(String userMessage, String b64Image) {
        isCancelled.set(false);

        HttpClient client = HttpClient.newHttpClient();
        String requestBody = String.format("{\"model\": \"llava-phi3\", \"prompt\": \"%s\", \"images\": [\"%s\"]}", userMessage, b64Image);

        Platform.runLater(() -> writeHere.setText("Processing..."));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        streamRequest = client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(HttpResponse::body)
                .thenAccept(inputStream -> {
                    currentInputStream = inputStream;

                    streamReadingTask = executorService.submit(() -> {
                        StringBuilder responseContent = new StringBuilder();
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (isCancelled.get()) {
                                    break;
                                }
                                responseContent.append(line);
                            }

                            // Aquí se separa los fragmentos de la respuesta
                            String[] responses = responseContent.toString().split("\\}\\{");
                            boolean isComplete = false;
                            StringBuilder finalResponse = new StringBuilder();

                            for (String response : responses) {
                                if (isCancelled.get()) {
                                    break;
                                }
                                String jsonString = response.trim();
                                if (!jsonString.startsWith("{")) {
                                    jsonString = "{" + jsonString;
                                }
                                if (!jsonString.endsWith("}")) {
                                    jsonString = jsonString + "}";
                                }

                                JSONObject jsonResponse = new JSONObject(jsonString);
                                finalResponse.append(jsonResponse.getString("response"));

                                if (jsonResponse.getBoolean("done")) {
                                    isComplete = true;
                                }
                            }

                            if (!isCancelled.get() && isComplete) { // Verifica que no esté cancelado antes de actualizar la UI
                                Platform.runLater(() -> {
                                    try {
                                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/assets/ChatMssg.fxml")));
                                        Node layout4 = loader.load();
                                        ControllerChat controllerChat = loader.getController();
                                        box.getChildren().add(layout4);
                                        layoutControllerMap.put(layout4, controllerChat);
                                        controllerChat.addText(finalResponse.toString());
                                        scroll.setVvalue(1.0);
                                        writeHere.setPromptText("Completed");

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            } else if (isCancelled.get()) {
                                Platform.runLater(() -> writeHere.setPromptText("Request was cancelled."));
                            } else {
                                Platform.runLater(() -> writeHere.setPromptText("Response not complete yet."));
                            }
                        } catch (IOException e) {
                            if (isCancelled.get()) {
                                Platform.runLater(() -> writeHere.setPromptText("Request was cancelled"));
                            } else {
                                e.printStackTrace();
                                Platform.runLater(() -> writeHere.setPromptText("Error during processing"));
                            }
                        }
                    });
                })
                .exceptionally(ex -> {
                    if (ex.getCause() instanceof java.util.concurrent.CancellationException) {
                        Platform.runLater(() -> writeHere.setPromptText("Request was cancelled"));
                    } else {
                        ex.printStackTrace();
                        Platform.runLater(() -> writeHere.setPromptText("Error during request: " + ex.getMessage()));
                    }
                    return null;
                });
    }

    public void clearScreen(ActionEvent actionEvent) {
        b64Image = null;
        image.setVisible(false);
        box.getChildren().clear();
    }

    @FXML
    public void sendMessagetoAI(String userMessage) throws IOException, InterruptedException {
        isFirst = true;
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = String.format("{\"model\": \"llama3.2:1b\", \"prompt\": \"%s\", \"stream\": true}", userMessage);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:11434/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(HttpResponse::body)
                .thenAccept(inputStream -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (isCancelled.get()) {
                                break;
                            }
                            JSONObject jsonResponse = new JSONObject(line);
                            String responseText = jsonResponse.getString("response");

                            Platform.runLater(() -> {
                                try {
                                    if (isFirst) {
                                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/assets/ChatMssg.fxml")));
                                        Node layout3 = loader.load();
                                        ControllerChat controllerChat = loader.getController();
                                        box.getChildren().add(layout3);
                                        layoutControllerMap.put(layout3, controllerChat);
                                        controllerChat.addText(responseText);
                                        isFirst = false;
                                    } else {
                                        Node lastLayout = box.getChildren().get(box.getChildren().size() - 1);
                                        ControllerChat controllerChat = layoutControllerMap.get(lastLayout);
                                        if (controllerChat != null) {
                                            controllerChat.addText(responseText);
                                        }
                                    }
                                    scroll.setVvalue(1.0);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }
    @FXML
    public void cancelRequest() {
        isCancelled.set(true);
        if (streamRequest != null && !streamRequest.isDone()) {
            streamRequest.cancel(true);
        }
        if (completeRequest != null && !completeRequest.isDone()) {
            completeRequest.cancel(true);
        }
        if (streamReadingTask != null && !streamReadingTask.isDone()) {
            streamReadingTask.cancel(true);
        }
        Platform.runLater(() -> writeHere.setPromptText("Request cancelled"));
    }
}