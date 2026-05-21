/*
# 1. Compile everything
javac -cp ".:mysql-connector-j-9.6.0.jar" --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml UniversityRMI/*.java

# 2. Open Terminal 1 - Start RMI Server
java -cp ".:mysql-connector-j-9.6.0.jar" UniversityRMI.RMIServer

# 3. Open Terminal 2 - Start Client
java -cp ".:mysql-connector-j-9.6.0.jar" --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml UniversityRMI.Main
*/
package UniversityRMI;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Main extends Application {
    
    private RemoteInterface server;
    private TabPane tabPane;
    
    private TextField studentIdField, studentNameField, studentDepartmentField, studentYearField;
    private ComboBox<String> studentSectionCombo;
    private TextArea studentOutputArea;
    
    private TextField teacherIdField, teacherNameField, teacherDepartmentField, teacherSubjectField, teacherExperienceField;
    private TextArea teacherOutputArea;
    
    @Override
    public void start(Stage primaryStage) {
        
        if (!connectToServer()) {
            showErrorAndExit("Could not connect to RMI Server!\nMake sure the server is running.");
            return;
        }
        
        primaryStage.setTitle("University Management System (RMI Client)");
        
        tabPane = new TabPane();
        
        
        Tab studentTab = new Tab("Student Management");
        Tab teacherTab = new Tab("Teacher Management");
        
        studentTab.setContent(createStudentPanel());
        teacherTab.setContent(createTeacherPanel());
        
        tabPane.getTabs().addAll(studentTab, teacherTab);
        
        Scene scene = new Scene(tabPane, 950, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        studentOutputArea.appendText("Connected to RMI Server successfully!\n");
    }
    
    private boolean connectToServer() {
        try {
            
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            
           
            server = (RemoteInterface) registry.lookup("UniversityManagementSystem");
            
            System.out.println("Connected to RMI Server successfully!");
            return true;
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private void showErrorAndExit(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Connection Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }
    
    private VBox createStudentPanel() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        
        
        Label titleLabel = new Label("Student Management System (RMI Client)");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        
        Label infoLabel = new Label("⚠️ Note: For 'Save to File' operation, all fields are required. For 'Update' and 'Delete', only ID is required.");
        infoLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-padding: 5px;");
        
        
        GridPane inputForm = new GridPane();
        inputForm.setHgap(10);
        inputForm.setVgap(10);
        inputForm.setPadding(new Insets(10));
        inputForm.setStyle("-fx-border-color: #3498db; -fx-border-radius: 5px; -fx-padding: 15px;");
        
        
        Label idLabel = new Label("Student ID:*");
        idLabel.setStyle("-fx-font-weight: bold;");
        studentIdField = new TextField();
        studentIdField.setPromptText("Enter student ID (Required for all operations)");
        
        Label nameLabel = new Label("Student Name:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        studentNameField = new TextField();
        studentNameField.setPromptText("Enter student name (Required for Add/Save)");
        
        Label departmentLabel = new Label("Department:");
        departmentLabel.setStyle("-fx-font-weight: bold;");
        studentDepartmentField = new TextField();
        studentDepartmentField.setPromptText("Enter department (Required for Add/Save)");
        
        Label sectionLabel = new Label("Section:");
        sectionLabel.setStyle("-fx-font-weight: bold;");
        studentSectionCombo = new ComboBox<>();
        studentSectionCombo.getItems().addAll("A", "B", "C", "D", "E");
        studentSectionCombo.setValue("A");
        
        Label yearLabel = new Label("Year:");
        yearLabel.setStyle("-fx-font-weight: bold;");
        studentYearField = new TextField();
        studentYearField.setPromptText("Enter year (Required for Add/Save)");
        
        inputForm.add(idLabel, 0, 0);
        inputForm.add(studentIdField, 1, 0);
        inputForm.add(nameLabel, 0, 1);
        inputForm.add(studentNameField, 1, 1);
        inputForm.add(departmentLabel, 0, 2);
        inputForm.add(studentDepartmentField, 1, 2);
        inputForm.add(sectionLabel, 0, 3);
        inputForm.add(studentSectionCombo, 1, 3);
        inputForm.add(yearLabel, 0, 4);
        inputForm.add(studentYearField, 1, 4);
        
       
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        
        Button addToFileBtn = new Button("💾 Save Student to File");
        Button showFromFileBtn = new Button("📂 Show Students from File");
        Button saveToDbBtn = new Button("🗄️ Save Student to Database");
        Button readFromDbBtn = new Button("🔍 Read Students from Database");
        Button updateInDbBtn = new Button("✏️ Update Student in Database");
        Button deleteFromDbBtn = new Button("🗑️ Delete Student from Database");
        Button clearOutputBtn = new Button("🧹 Clear Output Area");
        
        styleButton(addToFileBtn, "#27ae60");
        styleButton(showFromFileBtn, "#3498db");
        styleButton(saveToDbBtn, "#e67e22");
        styleButton(readFromDbBtn, "#9b59b6");
        styleButton(updateInDbBtn, "#f39c12");
        styleButton(deleteFromDbBtn, "#e74c3c");
        styleButton(clearOutputBtn, "#95a5a6");
        
        buttonBox.getChildren().addAll(addToFileBtn, showFromFileBtn, saveToDbBtn, readFromDbBtn, 
                                      updateInDbBtn, deleteFromDbBtn, clearOutputBtn);
        
        
        Label outputLabel = new Label("Output:");
        outputLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        studentOutputArea = new TextArea();
        studentOutputArea.setEditable(false);
        studentOutputArea.setPrefHeight(300);
        studentOutputArea.setStyle("-fx-font-family: monospace;");
        
        
        addToFileBtn.setOnAction(e -> addStudentToFile());
        showFromFileBtn.setOnAction(e -> showStudentsFromFile());
        saveToDbBtn.setOnAction(e -> saveStudentToDatabase());
        readFromDbBtn.setOnAction(e -> readStudentsFromDatabase());
        updateInDbBtn.setOnAction(e -> updateStudent());
        deleteFromDbBtn.setOnAction(e -> deleteStudent());
        clearOutputBtn.setOnAction(e -> clearStudentOutput());
        
        mainLayout.getChildren().addAll(titleLabel, infoLabel, inputForm, buttonBox, outputLabel, studentOutputArea);
        
        return mainLayout;
    }
    
    private VBox createTeacherPanel() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        
        
        Label titleLabel = new Label("Teacher Management System (RMI Client)");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        
        Label infoLabel = new Label("⚠️ Note: For 'Save to File' operation, all fields are required. For 'Update' and 'Delete', only ID is required.");
        infoLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px; -fx-padding: 5px;");
        
        
        GridPane inputForm = new GridPane();
        inputForm.setHgap(10);
        inputForm.setVgap(10);
        inputForm.setPadding(new Insets(10));
        inputForm.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5px; -fx-padding: 15px;");
        
        
        Label idLabel = new Label("Teacher ID:*");
        idLabel.setStyle("-fx-font-weight: bold;");
        teacherIdField = new TextField();
        teacherIdField.setPromptText("Enter teacher ID (Required for all operations)");
        
        Label nameLabel = new Label("Teacher Name:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        teacherNameField = new TextField();
        teacherNameField.setPromptText("Enter teacher name (Required for Add/Save)");
        
        Label departmentLabel = new Label("Department:");
        departmentLabel.setStyle("-fx-font-weight: bold;");
        teacherDepartmentField = new TextField();
        teacherDepartmentField.setPromptText("Enter department (Required for Add/Save)");
        
        Label subjectLabel = new Label("Subject:");
        subjectLabel.setStyle("-fx-font-weight: bold;");
        teacherSubjectField = new TextField();
        teacherSubjectField.setPromptText("Enter subject (Required for Add/Save)");
        
        Label experienceLabel = new Label("Experience (years):");
        experienceLabel.setStyle("-fx-font-weight: bold;");
        teacherExperienceField = new TextField();
        teacherExperienceField.setPromptText("Enter experience (Required for Add/Save)");
        
        inputForm.add(idLabel, 0, 0);
        inputForm.add(teacherIdField, 1, 0);
        inputForm.add(nameLabel, 0, 1);
        inputForm.add(teacherNameField, 1, 1);
        inputForm.add(departmentLabel, 0, 2);
        inputForm.add(teacherDepartmentField, 1, 2);
        inputForm.add(subjectLabel, 0, 3);
        inputForm.add(teacherSubjectField, 1, 3);
        inputForm.add(experienceLabel, 0, 4);
        inputForm.add(teacherExperienceField, 1, 4);
        
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 10, 0));
        
        Button addToFileBtn = new Button("💾 Save Teacher to File");
        Button showFromFileBtn = new Button("📂 Show Teachers from File");
        Button saveToDbBtn = new Button("🗄️ Save Teacher to Database");
        Button readFromDbBtn = new Button("🔍 Read Teachers from Database");
        Button updateInDbBtn = new Button("✏️ Update Teacher in Database");
        Button deleteFromDbBtn = new Button("🗑️ Delete Teacher from Database");
        Button clearOutputBtn = new Button("🧹 Clear Output Area");
        
        styleButton(addToFileBtn, "#27ae60");
        styleButton(showFromFileBtn, "#3498db");
        styleButton(saveToDbBtn, "#e67e22");
        styleButton(readFromDbBtn, "#9b59b6");
        styleButton(updateInDbBtn, "#f39c12");
        styleButton(deleteFromDbBtn, "#e74c3c");
        styleButton(clearOutputBtn, "#95a5a6");
        
        buttonBox.getChildren().addAll(addToFileBtn, showFromFileBtn, saveToDbBtn, readFromDbBtn, 
                                      updateInDbBtn, deleteFromDbBtn, clearOutputBtn);
        
       
        Label outputLabel = new Label("Output:");
        outputLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        teacherOutputArea = new TextArea();
        teacherOutputArea.setEditable(false);
        teacherOutputArea.setPrefHeight(300);
        teacherOutputArea.setStyle("-fx-font-family: monospace;");
        
        
        addToFileBtn.setOnAction(e -> addTeacherToFile());
        showFromFileBtn.setOnAction(e -> showTeachersFromFile());
        saveToDbBtn.setOnAction(e -> saveTeacherToDatabase());
        readFromDbBtn.setOnAction(e -> readTeachersFromDatabase());
        updateInDbBtn.setOnAction(e -> updateTeacher());
        deleteFromDbBtn.setOnAction(e -> deleteTeacher());
        clearOutputBtn.setOnAction(e -> clearTeacherOutput());
        
        mainLayout.getChildren().addAll(titleLabel, infoLabel, inputForm, buttonBox, outputLabel, teacherOutputArea);
        
        return mainLayout;
    }
    
    private void styleButton(Button button, String color) {
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-cursor: hand; -fx-font-size: 12px;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-cursor: hand; -fx-opacity: 0.8; -fx-font-size: 12px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 12px; -fx-cursor: hand; -fx-opacity: 1; -fx-font-size: 12px;"));
    }
    
    // ==================== Student RMI Methods ====================
    
    private void addStudentToFile() {
        try {
            if (studentIdField.getText().isEmpty() || studentNameField.getText().isEmpty() || 
                studentDepartmentField.getText().isEmpty() || studentYearField.getText().isEmpty()) {
                studentOutputArea.appendText("❌ Error: All fields (ID, Name, Department, Year) are required to save to file!\n");
                return;
            }
            
            int id = Integer.parseInt(studentIdField.getText());
            String name = studentNameField.getText();
            String department = studentDepartmentField.getText();
            char section = studentSectionCombo.getValue().charAt(0);
            int year = Integer.parseInt(studentYearField.getText());
            
            Student student = new Student(id, name, department, section, year);
            
            
            String result = server.addStudentToFile(student);
            studentOutputArea.appendText(result);
            
            if (result.contains("✅")) {
                clearStudentForm();
            }
        } catch (NumberFormatException e) {
            studentOutputArea.appendText("❌ Error: Please enter valid numeric values for ID and Year!\n");
        } catch (Exception e) {
            studentOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void showStudentsFromFile() {
        try {
            String result = server.showStudentsFromFile();
            studentOutputArea.clear();
            studentOutputArea.appendText(result);
        } catch (Exception e) {
            studentOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void saveStudentToDatabase() {
        try {
            if (studentIdField.getText().isEmpty() || studentNameField.getText().isEmpty() || 
                studentDepartmentField.getText().isEmpty() || studentYearField.getText().isEmpty()) {
                studentOutputArea.appendText("❌ Error: All fields (ID, Name, Department, Year) are required to save to database!\n");
                return;
            }
            
            int id = Integer.parseInt(studentIdField.getText());
            String name = studentNameField.getText();
            String department = studentDepartmentField.getText();
            char section = studentSectionCombo.getValue().charAt(0);
            int year = Integer.parseInt(studentYearField.getText());
            
            Student student = new Student(id, name, department, section, year);
            
            String result = server.saveStudentToDatabase(student);
            studentOutputArea.appendText(result);
            
            if (result.contains("✓")) {
                clearStudentForm();
            }
        } catch (NumberFormatException e) {
            studentOutputArea.appendText("❌ Error: Please enter valid numeric values for ID and Year!\n");
        } catch (Exception e) {
            studentOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void readStudentsFromDatabase() {
        try {
            String result = server.readStudentsFromDatabase();
            studentOutputArea.clear();
            studentOutputArea.appendText(result);
        } catch (Exception e) {
            studentOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void updateStudent() {
        try {
            if (studentIdField.getText().isEmpty()) {
                studentOutputArea.appendText("❌ Error: Student ID is required for update operation!\n");
                return;
            }
            
            int id = Integer.parseInt(studentIdField.getText());
            String name = studentNameField.getText().isEmpty() ? null : studentNameField.getText();
            int year = studentYearField.getText().isEmpty() ? 0 : Integer.parseInt(studentYearField.getText());
            
            if ((name == null || name.isEmpty()) && year == 0) {
                studentOutputArea.appendText("❌ Error: Please provide either Name or Year to update!\n");
                return;
            }
            
            String result = server.updateStudentInDatabase(id, name, year);
            studentOutputArea.appendText(result);
            clearStudentForm();
        } catch (NumberFormatException e) {
            studentOutputArea.appendText("❌ Error: Please enter valid numeric values for ID and Year!\n");
        } catch (Exception e) {
            studentOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void deleteStudent() {
        try {
            if (studentIdField.getText().isEmpty()) {
                studentOutputArea.appendText("❌ Error: Student ID is required for delete operation!\n");
                return;
            }
            
            int id = Integer.parseInt(studentIdField.getText());
            
            String result = server.deleteStudentFromDatabase(id);
            studentOutputArea.appendText(result);
            clearStudentForm();
        } catch (NumberFormatException e) {
            studentOutputArea.appendText("❌ Error: Please enter a valid numeric ID!\n");
        } catch (Exception e) {
            studentOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void clearStudentOutput() {
        studentOutputArea.clear();
        studentOutputArea.appendText("Output area cleared.\n");
    }
    
    private void clearStudentForm() {
        studentIdField.clear();
        studentNameField.clear();
        studentDepartmentField.clear();
        studentYearField.clear();
        studentSectionCombo.setValue("A");
    }
    
    // ==================== Teacher RMI Methods ====================
    
    private void addTeacherToFile() {
        try {
            if (teacherIdField.getText().isEmpty() || teacherNameField.getText().isEmpty() || 
                teacherDepartmentField.getText().isEmpty() || teacherSubjectField.getText().isEmpty() || 
                teacherExperienceField.getText().isEmpty()) {
                teacherOutputArea.appendText("❌ Error: All fields (ID, Name, Department, Subject, Experience) are required to save to file!\n");
                return;
            }
            
            int id = Integer.parseInt(teacherIdField.getText());
            String name = teacherNameField.getText();
            String department = teacherDepartmentField.getText();
            String subject = teacherSubjectField.getText();
            int experience = Integer.parseInt(teacherExperienceField.getText());
            
            Teacher teacher = new Teacher(id, name, department, subject, experience);
            
            String result = server.addTeacherToFile(teacher);
            teacherOutputArea.appendText(result);
            
            if (result.contains("✅")) {
                clearTeacherForm();
            }
        } catch (NumberFormatException e) {
            teacherOutputArea.appendText("❌ Error: Please enter valid numeric values for ID and Experience!\n");
        } catch (Exception e) {
            teacherOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void showTeachersFromFile() {
        try {
            String result = server.showTeachersFromFile();
            teacherOutputArea.clear();
            teacherOutputArea.appendText(result);
        } catch (Exception e) {
            teacherOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void saveTeacherToDatabase() {
        try {
            if (teacherIdField.getText().isEmpty() || teacherNameField.getText().isEmpty() || 
                teacherDepartmentField.getText().isEmpty() || teacherSubjectField.getText().isEmpty() || 
                teacherExperienceField.getText().isEmpty()) {
                teacherOutputArea.appendText("❌ Error: All fields (ID, Name, Department, Subject, Experience) are required to save to database!\n");
                return;
            }
            
            int id = Integer.parseInt(teacherIdField.getText());
            String name = teacherNameField.getText();
            String department = teacherDepartmentField.getText();
            String subject = teacherSubjectField.getText();
            int experience = Integer.parseInt(teacherExperienceField.getText());
            
            Teacher teacher = new Teacher(id, name, department, subject, experience);
            
            String result = server.saveTeacherToDatabase(teacher);
            teacherOutputArea.appendText(result);
            
            if (result.contains("✓")) {
                clearTeacherForm();
            }
        } catch (NumberFormatException e) {
            teacherOutputArea.appendText("❌ Error: Please enter valid numeric values for ID and Experience!\n");
        } catch (Exception e) {
            teacherOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void readTeachersFromDatabase() {
        try {
            String result = server.readTeachersFromDatabase();
            teacherOutputArea.clear();
            teacherOutputArea.appendText(result);
        } catch (Exception e) {
            teacherOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void updateTeacher() {
        try {
            if (teacherIdField.getText().isEmpty()) {
                teacherOutputArea.appendText("❌ Error: Teacher ID is required for update operation!\n");
                return;
            }
            
            int id = Integer.parseInt(teacherIdField.getText());
            String name = teacherNameField.getText().isEmpty() ? null : teacherNameField.getText();
            int experience = teacherExperienceField.getText().isEmpty() ? -1 : Integer.parseInt(teacherExperienceField.getText());
            
            if ((name == null || name.isEmpty()) && experience == -1) {
                teacherOutputArea.appendText("❌ Error: Please provide either Name or Experience to update!\n");
                return;
            }
            
            String result = server.updateTeacherInDatabase(id, name, experience);
            teacherOutputArea.appendText(result);
            clearTeacherForm();
        } catch (NumberFormatException e) {
            teacherOutputArea.appendText("❌ Error: Please enter valid numeric values for ID and Experience!\n");
        } catch (Exception e) {
            teacherOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void deleteTeacher() {
        try {
            if (teacherIdField.getText().isEmpty()) {
                teacherOutputArea.appendText("❌ Error: Teacher ID is required for delete operation!\n");
                return;
            }
            
            int id = Integer.parseInt(teacherIdField.getText());
            
            String result = server.deleteTeacherFromDatabase(id);
            teacherOutputArea.appendText(result);
            clearTeacherForm();
        } catch (NumberFormatException e) {
            teacherOutputArea.appendText("❌ Error: Please enter a valid numeric ID!\n");
        } catch (Exception e) {
            teacherOutputArea.appendText("❌ Error calling remote method: " + e.getMessage() + "\n");
        }
    }
    
    private void clearTeacherOutput() {
        teacherOutputArea.clear();
        teacherOutputArea.appendText("Output area cleared.\n");
    }
    
    private void clearTeacherForm() {
        teacherIdField.clear();
        teacherNameField.clear();
        teacherDepartmentField.clear();
        teacherSubjectField.clear();
        teacherExperienceField.clear();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
