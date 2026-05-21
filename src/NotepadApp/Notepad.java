//javac --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml app/NotepadApp/*.java
//java --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml app.NotepadApp.Notepad
package NotepadApp;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCombination;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.Insets;
import java.io.*;
import java.util.Optional;

public class Notepad extends Application {
    

    private TextArea textArea;
    private Stage primaryStage;
    private File currentFile = null;
    private Label statusLabel;
    private Label wordCountLabel;
    private Label lineColLabel;
    private boolean isFullScreen = false;
    private boolean wordWrap = false;
    private double fontSize = 14.0;
    

    private String lastText = "";
    private String redoText = "";
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        textArea = new TextArea();
        textArea.getStyleClass().add("text-area");
        textArea.setFont(Font.font("Consolas", fontSize));
        textArea.setWrapText(wordWrap);
        
        textArea.textProperty().addListener((obs, old, newVal) -> {
            updateStatus();
            if (!newVal.equals(lastText)) {
                redoText = lastText;
                lastText = newVal;
            }
        });

        textArea.caretPositionProperty().addListener((obs, old, newVal) -> {
            updateLineColumn();
        });
        
        root.setCenter(textArea);
        
        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        ToolBar toolBar = createToolBar();
        root.setTop(new VBox(menuBar, toolBar));
        
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
        primaryStage.setTitle("Notepad - Untitled");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        updateStatus();
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("menu-bar");

        Menu fileMenu = new Menu("File");
        
        MenuItem newItem = new MenuItem("New");
        newItem.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newItem.setOnAction(e -> newFile());
        
        MenuItem openItem = new MenuItem("Open...");
        openItem.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        openItem.setOnAction(e -> openFile());
        
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveItem.setOnAction(e -> saveFile());
        
        MenuItem saveAsItem = new MenuItem("Save As...");
        saveAsItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
        saveAsItem.setOnAction(e -> saveAsFile());
        
        SeparatorMenuItem sep1 = new SeparatorMenuItem();
        
        MenuItem printItem = new MenuItem("Print...");
        printItem.setAccelerator(KeyCombination.keyCombination("Ctrl+P"));
        printItem.setOnAction(e -> printDocument());
        
        SeparatorMenuItem sep2 = new SeparatorMenuItem();
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        exitItem.setOnAction(e -> exitApplication());
        
        fileMenu.getItems().addAll(newItem, openItem, saveItem, saveAsItem, sep1, printItem, sep2, exitItem);

        Menu editMenu = new Menu("Edit");
        
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Z"));
        undoItem.setOnAction(e -> undo());
        
        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(KeyCombination.keyCombination("Ctrl+Y"));
        redoItem.setOnAction(e -> redo());
        
        SeparatorMenuItem sep3 = new SeparatorMenuItem();
        
        MenuItem cutItem = new MenuItem("Cut");
        cutItem.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        cutItem.setOnAction(e -> cut());
        
        MenuItem copyItem = new MenuItem("Copy");
        copyItem.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        copyItem.setOnAction(e -> copy());
        
        MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setAccelerator(KeyCombination.keyCombination("Ctrl+V"));
        pasteItem.setOnAction(e -> paste());
        
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setAccelerator(KeyCombination.keyCombination("Del"));
        deleteItem.setOnAction(e -> delete());
        
        SeparatorMenuItem sep4 = new SeparatorMenuItem();
        
        MenuItem selectAllItem = new MenuItem("Select All");
        selectAllItem.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        selectAllItem.setOnAction(e -> selectAll());
        
        SeparatorMenuItem sep5 = new SeparatorMenuItem();
        
        MenuItem findItem = new MenuItem("Find...");
        findItem.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
        findItem.setOnAction(e -> findText());
        
        MenuItem replaceItem = new MenuItem("Replace...");
        replaceItem.setAccelerator(KeyCombination.keyCombination("Ctrl+H"));
        replaceItem.setOnAction(e -> replaceText());
        
        MenuItem findNextItem = new MenuItem("Find Next");
        findNextItem.setAccelerator(KeyCombination.keyCombination("F3"));
        findNextItem.setOnAction(e -> findNext());
        
        MenuItem findPrevItem = new MenuItem("Find Previous");
        findPrevItem.setAccelerator(KeyCombination.keyCombination("Shift+F3"));
        findPrevItem.setOnAction(e -> findPrevious());
        
        editMenu.getItems().addAll(undoItem, redoItem, sep3, cutItem, copyItem, pasteItem, deleteItem, 
                                    sep4, selectAllItem, sep5, findItem, replaceItem, findNextItem, findPrevItem);

        Menu formatMenu = new Menu("Format");
        
        Menu fontMenu = new Menu("Font");
        
        MenuItem fontArial = new MenuItem("Arial");
        fontArial.setOnAction(e -> changeFont("Arial"));
        
        MenuItem fontTimes = new MenuItem("Times New Roman");
        fontTimes.setOnAction(e -> changeFont("Times New Roman"));
        
        MenuItem fontConsolas = new MenuItem("Consolas");
        fontConsolas.setOnAction(e -> changeFont("Consolas"));
        
        MenuItem fontCourier = new MenuItem("Courier New");
        fontCourier.setOnAction(e -> changeFont("Courier New"));
        
        fontMenu.getItems().addAll(fontArial, fontTimes, fontConsolas, fontCourier);
        
        Menu fontSizeMenu = new Menu("Font Size");
        
        MenuItem size10 = new MenuItem("10");
        size10.setOnAction(e -> changeFontSize(10));
        MenuItem size12 = new MenuItem("12");
        size12.setOnAction(e -> changeFontSize(12));
        MenuItem size14 = new MenuItem("14");
        size14.setOnAction(e -> changeFontSize(14));
        MenuItem size16 = new MenuItem("16");
        size16.setOnAction(e -> changeFontSize(16));
        MenuItem size18 = new MenuItem("18");
        size18.setOnAction(e -> changeFontSize(18));
        MenuItem size20 = new MenuItem("20");
        size20.setOnAction(e -> changeFontSize(20));
        MenuItem size24 = new MenuItem("24");
        size24.setOnAction(e -> changeFontSize(24));
        
        fontSizeMenu.getItems().addAll(size10, size12, size14, size16, size18, size20, size24);
        
        SeparatorMenuItem sep6 = new SeparatorMenuItem();
        
        MenuItem boldItem = new MenuItem("Bold");
        boldItem.setAccelerator(KeyCombination.keyCombination("Ctrl+B"));
        boldItem.setOnAction(e -> toggleBold());
        
        MenuItem italicItem = new MenuItem("Italic");
        italicItem.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        italicItem.setOnAction(e -> toggleItalic());
        
        MenuItem underlineItem = new MenuItem("Underline");
        underlineItem.setAccelerator(KeyCombination.keyCombination("Ctrl+U"));
        underlineItem.setOnAction(e -> toggleUnderline());
        
        SeparatorMenuItem sep7 = new SeparatorMenuItem();
        
        MenuItem textColorItem = new MenuItem("Text Color...");
        textColorItem.setOnAction(e -> changeTextColor());
        
        MenuItem bgColorItem = new MenuItem("Background Color...");
        bgColorItem.setOnAction(e -> changeBackgroundColor());
        
        SeparatorMenuItem sep8 = new SeparatorMenuItem();
        
        CheckMenuItem wordWrapItem = new CheckMenuItem("Word Wrap");
        wordWrapItem.setSelected(false);
        wordWrapItem.setOnAction(e -> toggleWordWrap(wordWrapItem));
        
        Menu alignmentMenu = new Menu("Alignment");
        
        MenuItem alignLeft = new MenuItem("Left");
        alignLeft.setOnAction(e -> setAlignment("left"));
        MenuItem alignCenter = new MenuItem("Center");
        alignCenter.setOnAction(e -> setAlignment("center"));
        MenuItem alignRight = new MenuItem("Right");
        alignRight.setOnAction(e -> setAlignment("right"));
        MenuItem alignJustify = new MenuItem("Justify");
        alignJustify.setOnAction(e -> setAlignment("justify"));
        
        alignmentMenu.getItems().addAll(alignLeft, alignCenter, alignRight, alignJustify);
        
        formatMenu.getItems().addAll(fontMenu, fontSizeMenu, sep6, boldItem, italicItem, underlineItem, 
                                      sep7, textColorItem, bgColorItem, sep8, wordWrapItem, alignmentMenu);

        Menu viewMenu = new Menu("View");
        
        MenuItem zoomInItem = new MenuItem("Zoom In");
        zoomInItem.setAccelerator(KeyCombination.keyCombination("Ctrl+="));
        zoomInItem.setOnAction(e -> zoomIn());
        
        MenuItem zoomOutItem = new MenuItem("Zoom Out");
        zoomOutItem.setAccelerator(KeyCombination.keyCombination("Ctrl+-"));
        zoomOutItem.setOnAction(e -> zoomOut());
        
        MenuItem zoomResetItem = new MenuItem("Reset Zoom");
        zoomResetItem.setAccelerator(KeyCombination.keyCombination("Ctrl+0"));
        zoomResetItem.setOnAction(e -> resetZoom());
        
        SeparatorMenuItem sep9 = new SeparatorMenuItem();
        
        CheckMenuItem statusBarItem = new CheckMenuItem("Status Bar");
        statusBarItem.setSelected(true);
        statusBarItem.setOnAction(e -> toggleStatusBar(statusBarItem));
        
        CheckMenuItem lineNumbersItem = new CheckMenuItem("Line Numbers");
        lineNumbersItem.setSelected(false);
        lineNumbersItem.setOnAction(e -> toggleLineNumbers(lineNumbersItem));
        
        SeparatorMenuItem sep10 = new SeparatorMenuItem();
        
        MenuItem fullScreenItem = new MenuItem("Full Screen");
        fullScreenItem.setAccelerator(KeyCombination.keyCombination("F11"));
        fullScreenItem.setOnAction(e -> toggleFullScreen());
        
        viewMenu.getItems().addAll(zoomInItem, zoomOutItem, zoomResetItem, sep9, statusBarItem, 
                                    lineNumbersItem, sep10, fullScreenItem);
        
        Menu helpMenu = new Menu("Help");
        
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> showAbout());
        
        MenuItem shortcutsItem = new MenuItem("Keyboard Shortcuts");
        shortcutsItem.setOnAction(e -> showShortcuts());
        
        helpMenu.getItems().addAll(aboutItem, shortcutsItem);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, formatMenu, viewMenu, helpMenu);
        return menuBar;
    }
    
    private ToolBar createToolBar() {
        ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("tool-bar");
        
        Button newBtn = createToolButton("📄 New", "new", e -> newFile());
        Button openBtn = createToolButton("📂 Open", "open", e -> openFile());
        Button saveBtn = createToolButton("💾 Save", "save", e -> saveFile());
        Button printBtn = createToolButton("🖨️ Print", "print", e -> printDocument());
        
        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        Button cutBtn = createToolButton("✂️ Cut", "cut", e -> cut());
        Button copyBtn = createToolButton("📋 Copy", "copy", e -> copy());
        Button pasteBtn = createToolButton("📌 Paste", "paste", e -> paste());
        
        Separator sep2 = new Separator();
        sep2.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        Button boldBtn = createToolButton("B", "bold", e -> toggleBold());
        boldBtn.setStyle("-fx-font-weight: bold;");
        Button italicBtn = createToolButton("I", "italic", e -> toggleItalic());
        italicBtn.setStyle("-fx-font-style: italic;");
        Button underlineBtn = createToolButton("U", "underline", e -> toggleUnderline());
        underlineBtn.setStyle("-fx-underline: true;");
        
        Separator sep3 = new Separator();
        sep3.setOrientation(javafx.geometry.Orientation.VERTICAL);
        
        Button findBtn = createToolButton("🔍 Find", "find", e -> findText());
        
        toolBar.getItems().addAll(newBtn, openBtn, saveBtn, printBtn, sep, cutBtn, copyBtn, pasteBtn, 
                                   sep2, boldBtn, italicBtn, underlineBtn, sep3, findBtn);
        
        return toolBar;
    }
    
    private Button createToolButton(String text, String id, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setId(id);
        btn.setOnAction(handler);
        btn.getStyleClass().add("tool-button");
        return btn;
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(20);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        
        wordCountLabel = new Label("Words: 0");
        wordCountLabel.getStyleClass().add("status-label");
        
        lineColLabel = new Label("Line: 1, Col: 1");
        lineColLabel.getStyleClass().add("status-label");
        
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusBar.getChildren().addAll(wordCountLabel, lineColLabel, spacer, statusLabel);
        return statusBar;
    }
    
    
    private void newFile() {
        if (!textArea.getText().isEmpty()) {
            Alert confirm = new Alert(AlertType.CONFIRMATION);
            confirm.setTitle("New File");
            confirm.setHeaderText("Create new file?");
            confirm.setContentText("Unsaved changes will be lost!");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() != ButtonType.OK) return;
        }
        textArea.clear();
        currentFile = null;
        primaryStage.setTitle("Notepad - Untitled");
        lastText = "";
        redoText = "";
        updateStatus();
    }
    
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("Java Files", "*.java"),
            new FileChooser.ExtensionFilter("HTML Files", "*.html", "*.htm"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                textArea.setText(content.toString());
                currentFile = file;
                primaryStage.setTitle("Notepad - " + file.getName());
                statusLabel.setText("Opened: " + file.getName());
                lastText = textArea.getText();
                redoText = "";
            } catch (IOException e) {
                showError("Error opening file: " + e.getMessage());
            }
        }
    }
    
    private void saveFile() {
        if (currentFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                writer.write(textArea.getText());
                statusLabel.setText("Saved: " + currentFile.getName());
                lastText = textArea.getText();
            } catch (IOException e) {
                showError("Error saving file: " + e.getMessage());
            }
        } else {
            saveAsFile();
        }
    }
    
    private void saveAsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(textArea.getText());
                currentFile = file;
                primaryStage.setTitle("Notepad - " + file.getName());
                statusLabel.setText("Saved: " + file.getName());
                lastText = textArea.getText();
            } catch (IOException e) {
                showError("Error saving file: " + e.getMessage());
            }
        }
    }
    
    private void printDocument() {
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(primaryStage)) {
            job.printPage(textArea);
            job.endJob();
            statusLabel.setText("Document printed");
        }
    }
    
    private void exitApplication() {
        if (!textArea.getText().equals(lastText)) {
            Alert confirm = new Alert(AlertType.CONFIRMATION);
            confirm.setTitle("Exit");
            confirm.setHeaderText("Save changes before exiting?");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.get() == ButtonType.OK) {
                saveFile();
            }
        }
        System.exit(0);
    }
    
    // ==================== EDIT OPERATIONS ====================
    
    private void undo() {
        if (!redoText.isEmpty()) {
            String current = textArea.getText();
            textArea.setText(redoText);
            redoText = current;
            updateStatus();
        }
    }
    
    private void redo() {
        if (!redoText.isEmpty()) {
            String current = textArea.getText();
            textArea.setText(redoText);
            redoText = current;
            updateStatus();
        }
    }
    
    private void cut() {
        String selected = textArea.getSelectedText();
        if (!selected.isEmpty()) {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(selected);
            clipboard.setContent(content);
            
            int start = textArea.getSelection().getStart();
            int end = textArea.getSelection().getEnd();
            textArea.setText(textArea.getText().substring(0, start) + textArea.getText().substring(end));
        }
    }
    
    private void copy() {
        String selected = textArea.getSelectedText();
        if (!selected.isEmpty()) {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(selected);
            clipboard.setContent(content);
        }
    }
    
    private void paste() {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        String content = clipboard.getString();
        if (content != null) {
            int pos = textArea.getCaretPosition();
            textArea.insertText(pos, content);
        }
    }
    
    private void delete() {
        String selected = textArea.getSelectedText();
        if (!selected.isEmpty()) {
            int start = textArea.getSelection().getStart();
            int end = textArea.getSelection().getEnd();
            textArea.setText(textArea.getText().substring(0, start) + textArea.getText().substring(end));
        }
    }
    
    private void selectAll() {
        textArea.selectAll();
    }
    
    private void findText() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Find");
        dialog.setHeaderText("Find Text");
        dialog.setContentText("Find:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(searchText -> {
            String text = textArea.getText();
            int index = text.indexOf(searchText, textArea.getCaretPosition());
            if (index != -1) {
                textArea.selectRange(index, index + searchText.length());
                statusLabel.setText("Found: " + searchText);
            } else {
                statusLabel.setText("Text not found: " + searchText);
            }
        });
    }
    
    private void replaceText() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Replace");
        dialog.setHeaderText("Find and Replace");
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        
        TextField findField = new TextField();
        findField.setPromptText("Find:");
        TextField replaceField = new TextField();
        replaceField.setPromptText("Replace with:");
        
        Button replaceBtn = new Button("Replace");
        Button replaceAllBtn = new Button("Replace All");
        
        replaceBtn.setOnAction(e -> {
            String find = findField.getText();
            String replace = replaceField.getText();
            String text = textArea.getText();
            int pos = textArea.getCaretPosition();
            int index = text.indexOf(find, pos);
            if (index != -1) {
                textArea.replaceText(index, index + find.length(), replace);
                statusLabel.setText("Replaced one occurrence");
            }
        });
        
        replaceAllBtn.setOnAction(e -> {
            String find = findField.getText();
            String replace = replaceField.getText();
            String newText = textArea.getText().replace(find, replace);
            textArea.setText(newText);
            statusLabel.setText("Replaced all occurrences");
        });
        
        vbox.getChildren().addAll(findField, replaceField, replaceBtn, replaceAllBtn);
        dialog.getDialogPane().setContent(vbox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void findNext() {
        String selected = textArea.getSelectedText();
        if (!selected.isEmpty()) {
            String text = textArea.getText();
            int index = text.indexOf(selected, textArea.getCaretPosition());
            if (index != -1) {
                textArea.selectRange(index, index + selected.length());
            }
        }
    }
    
    private void findPrevious() {
        String selected = textArea.getSelectedText();
        if (!selected.isEmpty()) {
            String text = textArea.getText();
            int index = text.lastIndexOf(selected, textArea.getCaretPosition() - selected.length() - 1);
            if (index != -1) {
                textArea.selectRange(index, index + selected.length());
            }
        }
    }
    
    
    private void changeFont(String fontName) {
        textArea.setFont(Font.font(fontName, fontSize));
    }
    
    private void changeFontSize(double size) {
        fontSize = size;
        textArea.setFont(Font.font(textArea.getFont().getFamily(), fontSize));
    }
    
    private void toggleBold() {
        Font font = textArea.getFont();
        if (font.getStyle().contains("Bold")) {
            textArea.setFont(Font.font(font.getFamily(), fontSize));
        } else {
            textArea.setFont(Font.font(font.getFamily(), FontWeight.BOLD, fontSize));
        }
    }
    
    private void toggleItalic() {
        Font font = textArea.getFont();
        if (font.getStyle().contains("Italic")) {
            textArea.setFont(Font.font(font.getFamily(), fontSize));
        } else {
            textArea.setFont(Font.font(font.getFamily(), FontPosture.ITALIC, fontSize));
        }
    }
    
    private void toggleUnderline() {
        statusLabel.setText("Underline not fully supported in TextArea");
    }
    
    private void changeTextColor() {
        ColorPicker colorPicker = new ColorPicker(Color.BLACK);
        Stage colorStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().add(colorPicker);
        
        Button applyBtn = new Button("Apply");
        applyBtn.setOnAction(e -> {
            String color = toHex(colorPicker.getValue());
            textArea.setStyle("-fx-text-fill: " + color + ";");
            colorStage.close();
        });
        
        vbox.getChildren().add(applyBtn);
        Scene scene = new Scene(vbox, 200, 100);
        colorStage.setScene(scene);
        colorStage.setTitle("Text Color");
        colorStage.show();
    }
    
    private void changeBackgroundColor() {
        ColorPicker colorPicker = new ColorPicker(Color.WHITE);
        Stage colorStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().add(colorPicker);
        
        Button applyBtn = new Button("Apply");
        applyBtn.setOnAction(e -> {
            String color = toHex(colorPicker.getValue());
            textArea.setStyle("-fx-control-inner-background: " + color + ";");
            colorStage.close();
        });
        
        vbox.getChildren().add(applyBtn);
        Scene scene = new Scene(vbox, 200, 100);
        colorStage.setScene(scene);
        colorStage.setTitle("Background Color");
        colorStage.show();
    }
    
    private String toHex(Color color) {
        return String.format("#%02X%02X%02X", 
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
    
    private void toggleWordWrap(CheckMenuItem item) {
        wordWrap = item.isSelected();
        textArea.setWrapText(wordWrap);
    }
    
    private void setAlignment(String alignment) {
        statusLabel.setText("Alignment set to " + alignment + " (limited support in TextArea)");
    }
    
    
    
    private void zoomIn() {
        fontSize += 2;
        textArea.setFont(Font.font(textArea.getFont().getFamily(), fontSize));
    }
    
    private void zoomOut() {
        fontSize = Math.max(8, fontSize - 2);
        textArea.setFont(Font.font(textArea.getFont().getFamily(), fontSize));
    }
    
    private void resetZoom() {
        fontSize = 14;
        textArea.setFont(Font.font(textArea.getFont().getFamily(), fontSize));
    }
    
    private void toggleStatusBar(CheckMenuItem item) {
        VBox root = (VBox) primaryStage.getScene().getRoot();
        if (item.isSelected()) {
            // Status bar is already visible
        }
    }
    
    private void toggleLineNumbers(CheckMenuItem item) {

        statusLabel.setText("Line numbers " + (item.isSelected() ? "enabled" : "disabled") + " (limited support)");
    }
    
    private void toggleFullScreen() {
        isFullScreen = !isFullScreen;
        primaryStage.setFullScreen(isFullScreen);
    }
    
    
    private void showAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About Notepad");
        alert.setHeaderText("JavaFX Notepad");
        alert.setContentText("A complete notepad application built with JavaFX\n\n" +
                             "Features:\n" +
                             "- File operations (New, Open, Save, Print)\n" +
                             "- Edit operations (Undo, Redo, Cut, Copy, Paste)\n" +
                             "- Format options (Font, Size, Colors)\n" +
                             "- Find and Replace\n" +
                             "- Zoom In/Out\n" +
                             "- Full Screen mode\n\n" +
                             "Version 1.0");
        alert.showAndWait();
    }
    
    private void showShortcuts() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Keyboard Shortcuts");
        alert.setHeaderText("Keyboard Shortcuts");
        alert.setContentText(
            "File:\n" +
            "  Ctrl+N - New File\n" +
            "  Ctrl+O - Open File\n" +
            "  Ctrl+S - Save File\n" +
            "  Ctrl+Shift+S - Save As\n" +
            "  Ctrl+P - Print\n" +
            "  Ctrl+Q - Exit\n\n" +
            "Edit:\n" +
            "  Ctrl+Z - Undo\n" +
            "  Ctrl+Y - Redo\n" +
            "  Ctrl+X - Cut\n" +
            "  Ctrl+C - Copy\n" +
            "  Ctrl+V - Paste\n" +
            "  Del - Delete\n" +
            "  Ctrl+A - Select All\n" +
            "  Ctrl+F - Find\n" +
            "  Ctrl+H - Replace\n" +
            "  F3 - Find Next\n" +
            "  Shift+F3 - Find Previous\n\n" +
            "Format:\n" +
            "  Ctrl+B - Bold\n" +
            "  Ctrl+I - Italic\n" +
            "  Ctrl+U - Underline\n\n" +
            "View:\n" +
            "  Ctrl+= - Zoom In\n" +
            "  Ctrl+- - Zoom Out\n" +
            "  Ctrl+0 - Reset Zoom\n" +
            "  F11 - Full Screen"
        );
        alert.showAndWait();
    }
  
    
    private void updateStatus() {
        String text = textArea.getText();
        int words = text.isEmpty() ? 0 : text.trim().split("\\s+").length;
        wordCountLabel.setText("Words: " + words);
        updateLineColumn();
    }
    
    private void updateLineColumn() {
        String text = textArea.getText();
        int pos = textArea.getCaretPosition();
        int line = 1;
        int col = 1;
        
        for (int i = 0; i < pos && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
                col = 1;
            } else {
                col++;
            }
        }
        
        lineColLabel.setText("Line: " + line + ", Col: " + col);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
