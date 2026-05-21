/*
javac --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml app/PokerGame/*.java
java --module-path /home/user/Downloads/javafx-sdk-25.0.2/lib --add-modules javafx.controls,javafx.fxml app.PokerGame.Main
*/
package PokerGame;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;
import java.util.ArrayList;

public class MainApp extends Application {

    private Stage primaryStage;
    private boolean paused = false;
    private GameController game;

    private Label statusLabel, potLabel, betLabel, streetLabel;
    private FlowPane communityPane;
    private VBox playersPane;
    private Button btnFold, btnCall, btnRaise, btnCheck, btnPause, btnResume;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("♠ Texas Hold'em Poker");
        stage.setResizable(false);
        showSetupScreen();
        stage.show();
    }

    
    private void showSetupScreen() {
        VBox layout = new VBox(14);
        layout.setPadding(new Insets(28));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: #0f2a0f;");

        Label title = new Label("♠  TEXAS HOLD'EM  ♠");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 26));
        title.setTextFill(Color.GOLD);

        Label sub = new Label("Game Setup");
        sub.setFont(Font.font("Georgia", FontWeight.NORMAL, 14));
        sub.setTextFill(Color.web("#90ee90"));

        Separator sep1 = new Separator();
        sep1.setStyle("-fx-background-color: #336633;");


        Label lTotal = lbl("Total players (2–5):");
        Spinner<Integer> spTotal = intSpinner(2, 5, 2);

        Label lHuman = lbl("Human players:");
        Spinner<Integer> spHuman = intSpinner(1, 2, 1);

        VBox playerConfigBox = new VBox(8);
        playerConfigBox.setStyle("-fx-background-color: #172817; -fx-padding: 14; -fx-background-radius: 8;");

  
        spTotal.valueProperty().addListener((o, ov, nv) -> {
            SpinnerValueFactory.IntegerSpinnerValueFactory f =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, nv, Math.min(spHuman.getValue(), nv));
            spHuman.setValueFactory(f);
            rebuildConfig(spTotal, spHuman, playerConfigBox);
        });
        spHuman.valueProperty().addListener((o, ov, nv) -> rebuildConfig(spTotal, spHuman, playerConfigBox));

        rebuildConfig(spTotal, spHuman, playerConfigBox);

        Button btnStart = new Button("▶   Start Game");
        btnStart.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        btnStart.setStyle(btnStyle("#2d7d2d", "gold"));
        btnStart.setPrefWidth(200);
        btnStart.setOnAction(e -> {
            ArrayList<Player> players = buildPlayers(spTotal, spHuman, playerConfigBox);
            if (players != null) startGame(players);
        });

        layout.getChildren().addAll(
            title, sub, sep1,
            hrow(lTotal, spTotal),
            hrow(lHuman, spHuman),
            playerConfigBox,
            btnStart
        );

        ScrollPane scroll = new ScrollPane(layout);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #0f2a0f; -fx-background: #0f2a0f;");
        primaryStage.setScene(new Scene(scroll, 700, 540));
    }

    private void rebuildConfig(Spinner<Integer> spTotal, Spinner<Integer> spHuman, VBox box) {
        box.getChildren().clear();
        int total  = spTotal.getValue();
        int humans = Math.min(spHuman.getValue(), total);
        int bNum   = 1;
        for (int i = 0; i < total; i++) {
            boolean isHuman = i < humans;
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label nameTag = lbl(isHuman ? "Player " + (i+1) : "Bot " + bNum);
            nameTag.setMinWidth(68);
            nameTag.setTextFill(isHuman ? Color.LIGHTBLUE : Color.LIGHTCORAL);
            nameTag.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
            row.getChildren().add(nameTag);
            if (isHuman) {
                TextField tfName  = tf("Player" + (i+1), "name_"  + i, 90);
                TextField tfChips = tf("1000",            "chips_" + i, 65);
                TextField tfRaise = tf("50",              "raise_" + i, 55);
                row.getChildren().addAll(
                    lbl("Name:"), tfName,
                    lbl("Chips $:"), tfChips,
                    lbl("Raise $:"), tfRaise
                );
            } else {
                row.getChildren().add(lbl("  Chips: $1000 | Raise: $50  (defaults)"));
                bNum++;
            }
            box.getChildren().add(row);
        }
    }

    private ArrayList<Player> buildPlayers(Spinner<Integer> spTotal, Spinner<Integer> spHuman, VBox cfgBox) {
        ArrayList<Player> list = new ArrayList<>();
        int total  = spTotal.getValue();
        int humans = Math.min(spHuman.getValue(), total);
        int bNum   = 1;
        for (int i = 0; i < total; i++) {
            if (i < humans) {
                TextField tfName  = (TextField) cfgBox.lookup("#name_"  + i);
                TextField tfChips = (TextField) cfgBox.lookup("#chips_" + i);
                TextField tfRaise = (TextField) cfgBox.lookup("#raise_" + i);
                try {
                    String nm    = tfName.getText().trim().isEmpty() ? "Player" + (i+1) : tfName.getText().trim();
                    int chips    = Integer.parseInt(tfChips.getText().trim());
                    int raise    = Integer.parseInt(tfRaise.getText().trim());
                    if (chips <= 0 || raise <= 0) throw new NumberFormatException();
                    list.add(new Player(nm, chips, raise, false));
                } catch (NumberFormatException ex) {
                    alert("Invalid numbers for Player " + (i+1) + ". Use positive integers."); return null;
                }
            } else {
                list.add(new Player("Bot" + bNum++, 1000, 50, true));
            }
        }
        return list;
    }


    private void startGame(ArrayList<Player> players) {
        paused = false;
        buildGameScene(players);
        game = new GameController(players);
        game.onStateChanged = this::refresh;
        game.onHandOver     = this::onHandOver;
        game.onBotAction    = this::refresh;
        game.startNewHand();
    }

    private void buildGameScene(ArrayList<Player> players) {
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: #0d2b0d;");


        Label titleLbl = new Label("♠ TEXAS HOLD'EM ♠");
        titleLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        titleLbl.setTextFill(Color.GOLD);

        streetLabel = new Label("");
        streetLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        streetLabel.setTextFill(Color.web("#aaffaa"));

        statusLabel = new Label("Dealing...");
        statusLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        statusLabel.setTextFill(Color.LIGHTYELLOW);
        statusLabel.setWrapText(true);

        potLabel = new Label("Pot: $0");
        potLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        potLabel.setTextFill(Color.GOLD);

        betLabel = new Label("To call: $0");
        betLabel.setFont(Font.font("Georgia", 13));
        betLabel.setTextFill(Color.web("#90ee90"));

        HBox infoRow = new HBox(28, potLabel, betLabel, streetLabel);
        infoRow.setAlignment(Pos.CENTER_LEFT);


        Label commLbl = new Label("Community Cards");
        commLbl.setTextFill(Color.LIGHTGRAY);
        commLbl.setFont(Font.font("Georgia", 12));

        communityPane = new FlowPane(8, 4);
        communityPane.setMinHeight(82);
        communityPane.setStyle("-fx-background-color: #163d16; -fx-padding: 10; -fx-background-radius: 8;");


        playersPane = new VBox(7);

        btnFold   = btn("Fold",     "#8b0000", e -> { if (!paused && game.isHumanTurn()) game.humanFold(); });
        btnCall   = btn("Call",     "#1a5276", e -> { if (!paused && game.isHumanTurn()) game.humanCall(); });
        btnRaise  = btn("Raise",    "#6e2fa1", e -> { if (!paused && game.isHumanTurn()) game.humanRaise(); });
        btnCheck  = btn("Check",    "#1a6b3a", e -> { if (!paused && game.isHumanTurn() && game.canCheck()) game.humanCheck(); });
        btnPause  = btn("⏸ Pause",  "#555555", e -> { paused = true;  updateBtns(); });
        btnResume = btn("▶ Resume", "#336633", e -> { paused = false; updateBtns(); });
        Button btnExit = btn("✕ Exit", "#333333", e -> primaryStage.close());

        HBox actions = new HBox(10, btnFold, btnCall, btnRaise, btnCheck,
                                    new Separator(Orientation.VERTICAL),
                                    btnPause, btnResume, btnExit);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(8, 0, 0, 0));

        root.getChildren().addAll(
            titleLbl, statusLabel, infoRow,
            commLbl, communityPane,
            new Separator(), playersPane,
            new Separator(), actions
        );

        primaryStage.setScene(new Scene(root, 800, 660));
    }


    private void refresh() {
        statusLabel.setText(game.statusMessage);
        potLabel.setText("Pot: $" + game.pot);
        betLabel.setText("To call: $" + game.currentBet);
        streetLabel.setText("[ " + game.street.name().replace("_", "-") + " ]");


        communityPane.getChildren().clear();
        for (Card c : game.community) communityPane.getChildren().add(cardNode(c));


        playersPane.getChildren().clear();
        for (int i = 0; i < game.players.size(); i++) {
            Player p = game.players.get(i);
            boolean active = (i == game.currentPlayerIndex) && !game.handOver;
            playersPane.getChildren().add(playerRow(p, active));
        }

        updateBtns();
    }

    private void updateBtns() {
        boolean ht = !game.handOver && game.isHumanTurn() && !paused;
        btnFold.setDisable(!ht);
        btnCall.setDisable(!ht || !game.canCall());
        btnRaise.setDisable(!ht || !game.canRaise());
        btnCheck.setDisable(!ht || !game.canCheck());
        btnPause.setDisable(paused || game.handOver);
        btnResume.setDisable(!paused);
    }

    private HBox playerRow(Player p, boolean active) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 12, 7, 12));
        String bg = active ? "#1e5c1e" : (p.folded ? "#3a1f1f" : "#1a3322");
        row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 8;");


        String arrow = active ? "▶ " : "   ";
        String icon  = p.isBot ? " 🤖" : " 👤";
        Label nameL  = new Label(arrow + p.name + icon);
        nameL.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        nameL.setTextFill(active ? Color.GOLD : (p.folded ? Color.GRAY : Color.WHITE));
        nameL.setMinWidth(130);

        Label chipsL = new Label("$" + p.chips);
        chipsL.setTextFill(Color.LIGHTGREEN);
        chipsL.setFont(Font.font("Georgia", 13));
        chipsL.setMinWidth(70);

        Label betL = new Label("Bet: $" + p.currentBet);
        betL.setTextFill(Color.LIGHTYELLOW);
        betL.setMinWidth(75);


        HBox cardsBox = new HBox(5);
        for (Card c : p.hand)
            cardsBox.getChildren().add(p.folded ? backCard() : cardNode(c));


        String tag = p.folded ? " [FOLDED]" : (p.allIn ? " [ALL-IN]" : "");
        Label tagL = new Label(tag);
        tagL.setTextFill(Color.SALMON);
        tagL.setFont(Font.font("Georgia", FontWeight.BOLD, 11));

        row.getChildren().addAll(nameL, chipsL, betL, cardsBox, tagL);
        return row;
    }

    private StackPane cardNode(Card c) {
        StackPane card = new StackPane();
        card.setMinSize(46, 64); card.setMaxSize(46, 64);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 6; " +
                      "-fx-effect: dropshadow(gaussian,rgba(0,0,0,.55),5,0,1,1);");
        Label lbl = new Label(c.rank.symbol + "\n" + c.suitSymbol());
        lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        lbl.setTextFill(c.isRed() ? Color.CRIMSON : Color.BLACK);
        lbl.setTextAlignment(TextAlignment.CENTER);
        lbl.setAlignment(Pos.CENTER);
        card.getChildren().add(lbl);
        return card;
    }

    private StackPane backCard() {
        StackPane card = new StackPane();
        card.setMinSize(46, 64); card.setMaxSize(46, 64);
        card.setStyle("-fx-background-color: #4a4a8a; -fx-background-radius: 6;");
        Label lbl = new Label("🂠");
        lbl.setTextFill(Color.web("#9999cc"));
        lbl.setFont(Font.font(16));
        card.getChildren().add(lbl);
        return card;
    }

    

    private void onHandOver() {
        refresh();

        ArrayList<String> bankrupt = new ArrayList<>();
        for (Player p : game.players) if (p.chips <= 0) bankrupt.add(p.name);
        game.removeBankrupt();

        StringBuilder msg = new StringBuilder(game.statusMessage + "\n\n");
        if (!bankrupt.isEmpty()) {
            for (String n : bankrupt) msg.append("💸 ").append(n).append(" is bankrupt and leaves the table.\n");
            msg.append("\n");
        }
        msg.append("Updated chip counts:\n");
        for (Player p : game.players) msg.append("  ").append(p.name).append(": $").append(p.chips).append("\n");
        msg.append("\nPlay another hand?");

        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setTitle("Hand Complete");
        dlg.setHeaderText("Round Over");
        dlg.setContentText(msg.toString());
        dlg.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        dlg.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                if (game.players.size() < 2) {
                    alert("Not enough players remaining. Returning to setup.");
                    showSetupScreen();
                } else {
                    game.startNewHand();
                }
            } else {
                showSetupScreen();
            }
        });
    }

   
    private Button btn(String text, String bg, javafx.event.EventHandler<javafx.event.ActionEvent> h) {
        Button b = new Button(text);
        b.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        b.setStyle(btnStyle(bg, "white"));
        b.setOnAction(h);
        return b;
    }

    private String btnStyle(String bg, String fg) {
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
               "-fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand;";
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web("#cccccc"));
        return l;
    }

    private TextField tf(String def, String id, double w) {
        TextField t = new TextField(def);
        t.setId(id);
        t.setMaxWidth(w);
        t.setStyle("-fx-background-color: #243824; -fx-text-fill: white; -fx-prompt-text-fill: gray;");
        return t;
    }

    private Spinner<Integer> intSpinner(int min, int max, int def) {
        Spinner<Integer> s = new Spinner<>(min, max, def);
        s.setMaxWidth(80);
        s.setStyle("-fx-background-color: #243824;");
        return s;
    }

    private HBox hrow(javafx.scene.Node... nodes) {
        HBox h = new HBox(10, nodes);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
