
```markdown
## 🚀 How to Run ChatApp

### Prerequisites
- MySQL running (XAMPP)
- JavaFX SDK installed
- Database `university_db` created with students and teachers tables (for user login)

### Compile

```bash
cd ~/JavaFxProject/src
javac -cp ".:mysql-connector-j-9.6.0.jar" --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml ChatApp/server/*.java ChatApp/client/*.java
```

### Run

**Terminal 1 - Start the Server:**
```bash
cd ~/JavaFxProject/src
java -cp ".:mysql-connector-j-9.6.0.jar" ChatApp.server.Server
```

**Terminal 2, 3, 4... - Start Clients (multiple clients supported):**
```bash
cd ~/JavaFxProject/src
java -cp ".:mysql-connector-j-9.6.0.jar" --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics ChatApp.client.LoginApp
```

### Notes
- Keep the server terminal running at all times
- You can open multiple client terminals for different users
- Each client connects to the same server for real-time chat
- Login uses credentials from the `university_db` database
```
