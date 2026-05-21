Here's the `.md` file for UniversityJavaFX:

```markdown
## 🚀 How to Run UniversityJavaFX

### Prerequisites
- MySQL running (XAMPP)
- Database `university_db` created with students and teachers tables

### Compile & Run

```bash
cd ~/JavaFxProject/src
javac -cp ".:mysql-connector-j-9.6.0.jar" --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml UniversityJavaFX/*.java
```

```bash
cd ~/JavaFxProject/src
java -cp ".:mysql-connector-j-9.6.0.jar" --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml UniversityJavaFX.Main
```
```