package automationUi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.testng.*;
import utils.Utilities;

import java.io.*;
import java.util.*;

import static utils.Utilities.*;

public class AutomationUI extends Application {

    TextArea logArea = new TextArea();
    private TextFlow urlLogFlow = new TextFlow();
    private ScrollPane urlLogScrollPane = new ScrollPane();
    public static AutomationUI instance;
    public static String customerId = "";

    @Override
    public void start(Stage stage) {
        instance = this;
        redirectConsoleOutput();

        // === Title ===
        Label title = new Label(" ROASTER LEAD CREATION ");
        title.setStyle("-fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,107,107,0.6), 10, 0, 0, 2);");
        title.setFont(Font.font("Arial", 26));
        title.setTextFill(Color.web("#ff6b6b"));
        title.setPadding(new Insets(5, 0, 5, 0));

        Label envLabel = styledLabel("Environment");
        Label customerTypeLabel = styledLabel("Customer Type");
        Label cityLabel = styledLabel("City");
        Label customerNameLabel = styledLabel("Customer Name");
        Label customerMobileStartingTwoDigitsLabel = styledLabel("Customer Mobile Prefix (2 digits)");


        TextField customerName = styledTextField("Enter Name");
        TextField customerMobilePrefix = styledTextField("Enter Mobile Prefix (60-99)");

        ComboBox<String> envDropdown = styledComboBox("Select Environment", List.of("preProd", "prod"));
        ComboBox<String> customerTypeDropdown = styledComboBox("Select Customer Type", List.of("DC", "HL", "HFN"));
        ComboBox<String> cityDropdown = styledComboBox("Select City", new ArrayList<>());

        // ====== Buttons ======
        Button runButton = new Button("Create Lead");
        runButton.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        runButton.setMaxWidth(120);
        runButton.setDisable(true);
        Button clearButton = new Button("Clear Log");
        clearButton.setStyle("-fx-background-color: #4e4e4e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        clearButton.setMaxWidth(120);
        clearButton.setOnAction(e -> {
            logArea.clear();
            urlLogFlow.getChildren().clear();
        });

        // ====== Log Areas ======
        logArea.setPrefHeight(600);
        logArea.setPrefWidth(560);
        logArea.setEditable(false);
        logArea.setStyle("-fx-control-inner-background: #1e1e1e; -fx-text-fill: white;");

        urlLogFlow.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 5;");
        urlLogScrollPane.setContent(urlLogFlow);
        urlLogScrollPane.setFitToWidth(true);
        urlLogScrollPane.setPrefHeight(900);
        urlLogScrollPane.setPrefWidth(960);
        urlLogScrollPane.setStyle("-fx-background: #1e1e1e; -fx-border-color: gray;");

        String jsonPath = Utilities.getInputPath("cityDataMap.json");
        Map<String, Map<String, String>> cityData = Utilities.loadCityDataFromJson(jsonPath);

        customerTypeDropdown.setOnAction(e -> {
            String selectedType = customerTypeDropdown.getValue();
            cityDropdown.getItems().clear();
            if (selectedType != null && cityData.containsKey(selectedType)) {
                cityDropdown.getItems().addAll(cityData.get(selectedType).keySet());
            }
        });

        // ====== Run Button Action ======
// === Validation Tooltip ===
        Tooltip disableTip = new Tooltip("Please fill all fields before creating a lead");
        runButton.setTooltip(disableTip);
        runButton.setDisable(true);

// === Enable button dynamically when all fields are filled ===
        Runnable validateFields = () -> {
            boolean allFilled = !customerName.getText().trim().isEmpty()
                    && envDropdown.getValue() != null
                    && customerTypeDropdown.getValue() != null
                    && cityDropdown.getValue() != null
                    && !customerMobilePrefix.getText().trim().isEmpty();

            runButton.setDisable(!allFilled);
            if (allFilled) {
                runButton.setTooltip(null);
            } else {
                runButton.setTooltip(disableTip);
            }
        };

// Add listeners to all input fields
        customerName.textProperty().addListener((obs, oldV, newV) -> validateFields.run());
        customerMobilePrefix.textProperty().addListener((obs, oldV, newV) -> validateFields.run());
        envDropdown.valueProperty().addListener((obs, oldV, newV) -> validateFields.run());
        customerTypeDropdown.valueProperty().addListener((obs, oldV, newV) -> validateFields.run());
        cityDropdown.valueProperty().addListener((obs, oldV, newV) -> validateFields.run());

// === Button Action ===
        runButton.setOnAction(e -> {
            runButton.setDisable(true);
            runButton.setText("Running...");
            runButton.setStyle("-fx-background-color: grey; -fx-text-fill: white;");

            String custName = customerName.getText();
            String env = envDropdown.getValue();
            String custType = customerTypeDropdown.getValue();
            String cityName = cityDropdown.getValue();
            String cityCode = cityData.get(custType).get(cityName);
            String mobilePrefix = customerMobilePrefix.getText();

            updateProperties("configuration/userConfigurations.properties", Map.of(
                    "customerName", custName,
                    "environment", env,
                    "customerType", custType,
                    "userSelectedCityProperty", cityCode,
                    "mobileNoStarting2digitPrefix", mobilePrefix
            ));

            new Thread(() -> {
                runTestNGTest();
                Platform.runLater(() -> {
                    runButton.setDisable(false);
                    runButton.setText("Create Lead");
                    runButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold;");
                });
            }).start();
        });

        // ====== Layout ======
        HBox buttonBox = new HBox(15, runButton, clearButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox formSection = new VBox(10,
                title,
                customerNameLabel, customerName,
                envLabel, envDropdown,
                customerTypeLabel, customerTypeDropdown,
                cityLabel, cityDropdown,
                customerMobileStartingTwoDigitsLabel, customerMobilePrefix,
                buttonBox
        );
        formSection.setAlignment(Pos.CENTER);

        VBox logSection = new VBox(8,
                styledLabel("Plain Logs:"), logArea,
                styledLabel("Clickable URL Logs:"), urlLogScrollPane
        );
        logSection.setAlignment(Pos.CENTER);
        logSection.setPadding(new Insets(0, 0, 0, 0));

        VBox card = new VBox(5, formSection, logSection);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(30, 20, 30, 20));
        card.setStyle( "-fx-background-color: rgba(44,47,51,0.15);" + "-fx-background-radius: 12;" + "-fx-border-color: rgba(255,255,255,0.3);" + "-fx-border-radius: 12;" + "-fx-border-width: 1;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 3);" );


        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: rgba(255,255,255,0.25); /* reduced opacity for clarity */
            -fx-background-radius: 18;
            -fx-border-color: rgba(255,255,255,0.4);
            -fx-border-radius: 18;
            -fx-border-width: 1.2;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 12, 0, 0, 6);
        """);

        // === Background Image ===
        String imagePath = Utilities.getInputPath("HomeLane&DesignCafe.png");
        Image backgroundImage = new Image(imagePath.startsWith("file:") ? imagePath : "file:" + imagePath);

        BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, true, false)
        );

        StackPane root = new StackPane(card);
        root.setBackground(new Background(background));

        // === Dynamic sizing based on screen size ===
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = Math.min(800, screenBounds.getWidth() * 0.9);
        double height = Math.min(900, screenBounds.getHeight() * 0.9);

        Scene scene = new Scene(root, width, height);
        stage.setTitle("Automation Test Runner");
        stage.setScene(scene);
        stage.show();
    }

    private Label styledLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#2b2b2b"));
        label.setStyle("""
        -fx-font-family: 'Segoe UI Semibold';
        -fx-font-size: 13.5px;
        -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 1, 0, 0, 1);
        -fx-padding: 1 0 2 0; 
    """);
        return label;
    }


    private TextField styledTextField(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setMaxWidth(250);
        tf.setStyle("""
        -fx-font-family: 'Segoe UI';
        -fx-font-size: 13px;
        -fx-background-color: rgba(255,255,255,0.7);
        -fx-background-radius: 8;
        -fx-border-color: rgba(0,0,0,0.15);
        -fx-border-radius: 8;
        -fx-border-width: 1;
        -fx-padding: 1 2 1 2;  /* reduced padding */
        -fx-text-fill: #2b2b2b;
        -fx-prompt-text-fill: #777;
    """);

        tf.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                tf.setStyle("""
                -fx-font-family: 'Segoe UI';
                -fx-font-size: 13px;
                -fx-background-color: rgba(255,255,255,0.9);
                -fx-background-radius: 8;
                -fx-border-color: #4A90E2;
                -fx-border-width: 1.5;
                -fx-border-radius: 8;
                -fx-effect: dropshadow(gaussian, rgba(74,144,226,0.4), 6, 0, 0, 0);
                -fx-padding: 4 6 4 6;  /* reduced padding */
                -fx-text-fill: #2b2b2b;
                -fx-prompt-text-fill: #777;
            """);
            } else {
                tf.setStyle("""
                -fx-font-family: 'Segoe UI';
                -fx-font-size: 13px;
                -fx-background-color: rgba(255,255,255,0.7);
                -fx-background-radius: 8;
                -fx-border-color: rgba(0,0,0,0.15);
                -fx-border-radius: 8;
                -fx-border-width: 1;
                -fx-padding: 4 6 4 6;  /* reduced padding */
                -fx-text-fill: #2b2b2b;
                -fx-prompt-text-fill: #777;
            """);
            }
        });

        return tf;
    }


    private ComboBox<String> styledComboBox(String placeholder, List<String> items) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(items);
        combo.setPromptText(placeholder);
        combo.setMaxWidth(250);
        combo.setStyle("""
        -fx-font-family: 'Segoe UI';
        -fx-font-size: 13px;
        -fx-background-color: rgba(255,255,255,0.7);
        -fx-background-radius: 8;
        -fx-border-color: rgba(0,0,0,0.15);
        -fx-border-radius: 8;
        -fx-border-width: 1;
        -fx-padding: 1 3 1 3; 
        -fx-text-fill: #2b2b2b;
        -fx-prompt-text-fill: #777;
    """);

        combo.hoverProperty().addListener((obs, oldVal, hovering) -> {
            if (hovering) {
                combo.setStyle("""
                -fx-font-family: 'Segoe UI';
                -fx-font-size: 13px;
                -fx-background-color: rgba(255,255,255,0.9);
                -fx-background-radius: 8;
                -fx-border-color: #4A90E2;
                -fx-border-width: 1.3;
                -fx-border-radius: 8;
                -fx-effect: dropshadow(gaussian, rgba(74,144,226,0.4), 6, 0, 0, 0);
                -fx-padding: 1 3 1 3;  /* reduced padding */
                -fx-text-fill: #2b2b2b;
                -fx-prompt-text-fill: #777;
            """);
            } else {
                combo.setStyle("""
                -fx-font-family: 'Segoe UI';
                -fx-font-size: 13px;
                -fx-background-color: rgba(255,255,255,0.7);
                -fx-background-radius: 8;
                -fx-border-color: rgba(0,0,0,0.15);
                -fx-border-radius: 8;
                -fx-border-width: 1;
                -fx-padding: 1 3 1 3; 
                -fx-text-fill: #2b2b2b;
                -fx-prompt-text-fill: #777;
            """);
            }
        });

        return combo;
    }






    private void redirectConsoleOutput() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                handleConsoleText(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                handleConsoleText(new String(b, off, len));
            }
        };

        System.setOut(new PrintStream(out, true));

        // In your existing AutomationUI class
    }

    private void handleConsoleText(String text) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            if (line.contains("http")) {
                appendToUrlLog(line);
            }else if(line.contains("Customer ID:")){
                customerId = line.substring(line.indexOf("Customer ID:") + "Customer ID:".length()).trim();
            }
            else {
                appendToLogArea(line);
            }
        }
    }

    private void appendToLogArea(String text) {
        javafx.application.Platform.runLater(() -> logArea.appendText(text + "\n"));
    }

    /** Static helper to log messages from listeners */
    public static void log(String message) {
        if (instance != null) {
            javafx.application.Platform.runLater(() ->
                    instance.logArea.appendText(message + "\n"));
        }
    }



    /** Run TestNG test suite */
    private void runTestNGTest() {
        try {
            TestNG testng = new TestNG();
            testng.setVerbose(0);

            InputStream xmlInputStream = getClass().getClassLoader()
                    .getResourceAsStream("XMLFiles/CreateLeads.xml");
            if (xmlInputStream == null) {
                throw new IllegalArgumentException("TestNG XML not found in JAR");
            }

            File tempXmlFile = File.createTempFile("testng-", ".xml");
            tempXmlFile.deleteOnExit();

            try (OutputStream out = new FileOutputStream(tempXmlFile)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = xmlInputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }

            log("⏳ Test Execution Started...");
            testng.setTestSuites(Collections.singletonList(tempXmlFile.getAbsolutePath()));
            testng.run();
            log("✅ Test Execution Completed Successfully!");
            log("Customer ID: " + customerId + "\n");
        } catch (Exception ex) {
            log("❌ Test Failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    private void appendToUrlLog(String text) {
        javafx.application.Platform.runLater(() -> {
            String[] words = text.split("\\s+");
            for (String word : words) {
                Text t = new Text(word + " ");
                t.setFill(Color.WHITE);

                if (word.startsWith("http")) {
                    t.setFill(Color.LIMEGREEN);
                    t.setUnderline(true);
                    t.setOnMouseClicked(event -> {
                        if (event.getClickCount() == 1) {
                            try {
                                java.awt.Desktop.getDesktop().browse(new java.net.URI(word));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                urlLogFlow.getChildren().add(t);
            }
            urlLogScrollPane.layout();
            urlLogScrollPane.setVvalue(1.0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
