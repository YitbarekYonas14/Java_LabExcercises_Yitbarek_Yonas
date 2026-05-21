package ChatApp.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

/**
 javac -cp ".:mysql-connector-j-9.6.0.jar"     --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib     --add-modules javafx.controls,javafx.fxml     ChatApp/server/*.java ChatApp/client/*.java 
 java -cp ".:mysql-connector-j-9.6.0.jar" ChatApp.server.Server
 java -cp ".:mysql-connector-j-9.6.0.jar"     --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib     --add-modules javafx.controls,javafx.fxml,javafx.base,javafx.graphics     ChatApp.client.LoginApp

 */
public class LoginApp extends Application {

    Stage primaryStage;

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Telegram Clone");
        stage.setResizable(false);
        showLogin();
        stage.show();
    }


    public void showLogin() {
        primaryStage.setTitle("Telegram Clone — Login");


        Label logo = new Label("✈");
        logo.setFont(Font.font(48));
        logo.setTextFill(Color.web("#2196F3"));

        Label title = new Label("Telegram Clone");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.WHITE);

        Label sub = new Label("Sign in to continue");
        sub.setTextFill(Color.web("#888"));


        TextField     usernameField = field("Username");
        PasswordField passwordField = passField("Password");

        Label errorLabel = new Label(" ");
        errorLabel.setTextFill(Color.web("#ff6b6b"));
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);

        Button btnLogin  = btn("Login",              "#2196F3");
        Button btnSignup = btn("Create New Account", "#37474f");
        btnLogin .setMaxWidth(Double.MAX_VALUE);
        btnSignup.setMaxWidth(Double.MAX_VALUE);

        btnLogin.setOnAction(e -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText();
            if (u.isEmpty() || p.isEmpty()) {
                err(errorLabel, "Please enter username and password.");
                return;
            }
            doLogin(u, p, errorLabel);
        });

        passwordField.setOnAction(e -> btnLogin.fire());

        btnSignup.setOnAction(e -> showSignup());


        VBox header = new VBox(4, logo, title, sub);
        header.setAlignment(Pos.CENTER);

        VBox card = new VBox(12, header, spacer(8),
                             usernameField, passwordField,
                             errorLabel, spacer(4),
                             btnLogin, btnSignup);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(32, 36, 32, 36));
        card.setStyle("-fx-background-color:#1e1e2e;-fx-background-radius:14;");
        card.setMaxWidth(340);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:#12121e;");
        root.setPrefSize(440, 500);

        primaryStage.setScene(new Scene(root));
    }

    // ── Attempt login via TCP ──────────────────────────────────────────────────
    private void doLogin(String user, String pass, Label errorLabel) {
        ClientNetwork net = new ClientNetwork();
        if (!net.connect("localhost", 12345)) {
            err(errorLabel, "Cannot reach server. Is it running?");
            return;
        }

        net.send("LOGIN|" + user + "|" + pass);

        // Read response in background, then switch to JavaFX thread
        Thread t = new Thread(() -> {
            String response = net.readOneLine();
            Platform.runLater(() -> {
                if (response == null) {
                    err(errorLabel, "Server did not respond.");
                    net.disconnect();
                    return;
                }
                String[] p = response.split("\\|", -1);
                if ("AUTH_OK".equals(p[0])) {
                    String   username = p[1];
                    String[] groups   = (p.length > 2 && !p[2].isEmpty())
                                        ? p[2].split(",") : new String[]{"General"};
                    // Open the main chat window
                    ChatWindow chat = new ChatWindow(primaryStage, username, groups, net, this);
                    chat.show();
                } else {
                    err(errorLabel, p.length > 1 ? p[1] : "Login failed.");
                    net.disconnect();
                }
            });
        }, "LoginThread");
        t.setDaemon(true);
        t.start();
    }


    public void showSignup() {
        primaryStage.setTitle("Telegram Clone — Create Account");

        Label title = new Label("Create Account");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.setTextFill(Color.WHITE);

        TextField     usernameField = field("Username (min 3 chars)");
        PasswordField passwordField = passField("Password");
        PasswordField confirmField  = passField("Confirm Password");

        Label msgLabel = new Label(" ");
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(300);
        msgLabel.setTextFill(Color.web("#ff6b6b"));

        Button btnReg  = btn("Register",         "#4CAF50");
        Button btnBack = btn("← Back to Login",  "#37474f");
        btnReg .setMaxWidth(Double.MAX_VALUE);
        btnBack.setMaxWidth(Double.MAX_VALUE);

        btnReg.setOnAction(e -> {
            String u  = usernameField.getText().trim();
            String p  = passwordField.getText();
            String p2 = confirmField.getText();

            if (u.isEmpty() || p.isEmpty()) {
                err(msgLabel, "All fields are required."); return;
            }
            if (u.length() < 3) {
                err(msgLabel, "Username must be at least 3 characters."); return;
            }
            if (!p.equals(p2)) {
                err(msgLabel, "Passwords do not match."); return;
            }
            doRegister(u, p, msgLabel);
        });

        btnBack.setOnAction(e -> showLogin());

        VBox card = new VBox(12, title, spacer(6),
                             usernameField, passwordField, confirmField,
                             msgLabel, spacer(4), btnReg, btnBack);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(32, 36, 32, 36));
        card.setStyle("-fx-background-color:#1e1e2e;-fx-background-radius:14;");
        card.setMaxWidth(340);

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color:#12121e;");
        root.setPrefSize(440, 520);
        primaryStage.setScene(new Scene(root));
    }


    private void doRegister(String user, String pass, Label msgLabel) {
        ClientNetwork net = new ClientNetwork();
        if (!net.connect("localhost", 12345)) {
            err(msgLabel, "Cannot reach server."); return;
        }

        net.send("REGISTER|" + user + "|" + pass);

        Thread t = new Thread(() -> {
            String response = net.readOneLine();
            Platform.runLater(() -> {
                net.disconnect();
                if (response == null) { err(msgLabel, "Server error."); return; }
                String[] p = response.split("\\|", -1);
                if ("REG_OK".equals(p[0])) {
                    msgLabel.setTextFill(Color.web("#4CAF50"));
                    msgLabel.setText("✓ " + (p.length > 1 ? p[1] : "Account created!"));
                } else {
                    err(msgLabel, p.length > 1 ? p[1] : "Registration failed.");
                }
            });
        }, "RegisterThread");
        t.setDaemon(true);
        t.start();
    }


    static TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setMaxWidth(300);
        f.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:white;" +
                   "-fx-prompt-text-fill:#555;-fx-background-radius:8;" +
                   "-fx-padding:10;-fx-font-size:14;");
        return f;
    }

    static PasswordField passField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setMaxWidth(300);
        f.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:white;" +
                   "-fx-prompt-text-fill:#555;-fx-background-radius:8;" +
                   "-fx-padding:10;-fx-font-size:14;");
        return f;
    }

    static Button btn(String text, String color) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;" +
                   "-fx-font-size:14;-fx-background-radius:8;" +
                   "-fx-padding:10 20;-fx-cursor:hand;");
        return b;
    }

    static Region spacer(int h) {
        Region r = new Region();
        r.setMinHeight(h);
        return r;
    }

    static void err(Label label, String msg) {
        label.setTextFill(Color.web("#ff6b6b"));
        label.setText(msg);
    }
}
