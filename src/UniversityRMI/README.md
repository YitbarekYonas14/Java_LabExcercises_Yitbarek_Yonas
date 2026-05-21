
```markdown
## 🚀 How to Run UniversityRMI

### Prerequisites
- MySQL running (XAMPP)
- Database `university_db` created with students and teachers tables

### Compile & Run

```bash
cd ~/JavaFxProject/src
javac -cp ".:mysql-connector-j-9.6.0.jar" --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml UniversityRMI/*.java
```

**Terminal 1 - Start RMI Server:**
```bash
cd ~/JavaFxProject/src
java -cp ".:mysql-connector-j-9.6.0.jar" UniversityRMI.RMIServer
```

**Terminal 2 - Start Client:**
```bash
cd ~/JavaFxProject/src
java -cp ".:mysql-connector-j-9.6.0.jar" --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml UniversityRMI.Main
```
```