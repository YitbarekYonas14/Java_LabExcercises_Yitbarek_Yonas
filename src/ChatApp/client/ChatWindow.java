package ChatApp.client;

import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.*;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatWindow {

    private final Stage stage;
    private final String me;
    private final ClientNetwork net;
    private final LoginApp loginApp;

    private String activePeer = null;
    private String activeGroup = "General";
    private final List<String> myGroups = new ArrayList<>();

    private final Map<String, List<HBox>> privateMessages = new HashMap<>();
    private final Map<String, List<HBox>> groupMessages = new HashMap<>();
    
    private final Set<String> loadedPrivateHistory = new HashSet<>();
    private final Set<String> loadedGroupHistory = new HashSet<>();

    private File pendingFile = null;

    private ListView<String> userListView;
    private ListView<String> groupListView;
    private VBox privateChatBox;
    private VBox groupChatBox;
    private ScrollPane privateScroll;
    private ScrollPane groupScroll;
    private TextField privateInput;
    private TextField groupInput;
    private Label typingLabel;
    private Label privateChatHeader;
    private Label groupChatHeader;
    private Label statusLabel;
    private TabPane tabPane;

    private static final String[] EMOJIS = {
        "😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😌","😍",
        "🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🤩",
        "🥳","😏","😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢",
        "😭","😤","😠","😡","🤬","🤯","😳","🥵","🥶","😱","😨","😰","😥","😓","🤗",
        "🤔","🤭","🤫","🤥","😶","😐","😑","😬","🙄","😯","😦","😧","😮","😲","🥱",
        "😴","🤤","😪","😵","🤐","🥴","🤢","🤮","🤧","😷","🤒","🤕","👍","👎","❤️",
        "🔥","✨","🎉","🙏","👏","💪","🤝","👋","🫡","💯"
    };

    public ChatWindow(Stage stage, String username, String[] groups,
                      ClientNetwork net, LoginApp loginApp) {
        this.stage = stage;
        this.me = username;
        this.net = net;
        this.loginApp = loginApp;
        myGroups.add("General");
        for (String g : groups) if (!myGroups.contains(g)) myGroups.add(g);
        
        net.startListening(new ClientNetwork.MessageListener() {
            @Override
            public void onMessage(String line) { ChatWindow.this.onMessage(line); }
            @Override
            public void onDisconnected() {
                Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("● Disconnected");
                        statusLabel.setTextFill(Color.web("#e74c3c"));
                    }
                });
            }
        });
    }

    public void show() {
        stage.setTitle("Telegram Clone — " + me);
        stage.setResizable(true);

        Label nameLabel = new Label("✈ " + me);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));

        statusLabel = new Label("● Connected");
        statusLabel.setTextFill(Color.web("#4CAF50"));
        statusLabel.setFont(Font.font(12));

        Button btnLogout = smallBtn("Logout", "#e74c3c");
        btnLogout.setOnAction(e -> logout());

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox topBar = new HBox(12, nameLabel, topSpacer, statusLabel, btnLogout);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 14, 8, 14));
        topBar.setStyle("-fx-background-color:#0d0d1a;");

        VBox sidebar = buildSidebar();
        sidebar.setPrefWidth(190);

        Tab privateTab = new Tab("💬 Private", buildPrivateTab());
        Tab groupTab = new Tab("👥 Group", buildGroupTab());
        privateTab.setClosable(false);
        groupTab.setClosable(false);

        tabPane = new TabPane(privateTab, groupTab);
        tabPane.setStyle("-fx-background-color:#1e1e2e;");

        HBox centerRow = new HBox(sidebar, tabPane);
        HBox.setHgrow(tabPane, Priority.ALWAYS);

        VBox root = new VBox(topBar, centerRow);
        VBox.setVgrow(centerRow, Priority.ALWAYS);

        stage.setScene(new Scene(root, 1050, 700));
        stage.setMinWidth(750);
        stage.setMinHeight(520);

        net.send("GET_ONLINE_USERS");
        groupListView.getItems().setAll(myGroups);
        selectGroup("General");
    }

    private VBox buildSidebar() {
        Label usersTitle = sideLabel("ONLINE USERS");
        userListView = new ListView<>();
        userListView.setStyle("-fx-background-color:#161626;-fx-border-color:transparent;");
        userListView.setCellFactory(lv -> new OnlineUserCell());
        userListView.setPrefHeight(220);
        userListView.setOnMouseClicked(e -> {
            String sel = userListView.getSelectionModel().getSelectedItem();
            if (sel != null && !sel.equals(me)) {
                selectPeer(sel);
                tabPane.getSelectionModel().select(0);
            }
        });

        Label groupsTitle = sideLabel("GROUPS");
        groupListView = new ListView<>();
        groupListView.setStyle("-fx-background-color:#161626;-fx-border-color:transparent;");
        groupListView.setCellFactory(lv -> new ColorCell("#ff8f00", "#"));
        groupListView.setPrefHeight(140);
        groupListView.setOnMouseClicked(e -> {
            String sel = groupListView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                selectGroup(sel);
                tabPane.getSelectionModel().select(1);
            }
        });

        Button btnCreate = sideBtn("+ Create", "#4CAF50");
        Button btnJoin = sideBtn("Join", "#2196F3");
        Button btnLeave = sideBtn("Leave", "#e74c3c");
        Button btnAddUser = sideBtn("+ User", "#9c27b0");

        btnCreate.setOnAction(e -> promptCreateGroup());
        btnJoin.setOnAction(e -> promptJoinGroup());
        btnLeave.setOnAction(e -> leaveGroup());
        btnAddUser.setOnAction(e -> promptAddUserToGroup());

        HBox groupBtns = new HBox(4, btnCreate, btnJoin, btnLeave, btnAddUser);
        groupBtns.setPadding(new Insets(5, 6, 5, 6));
        groupBtns.setStyle("-fx-background-color:#0d0d1a;");

        VBox sidebar = new VBox(usersTitle, userListView, groupsTitle, groupListView, groupBtns);
        sidebar.setStyle("-fx-background-color:#161626;");
        return sidebar;
    }

    private VBox buildPrivateTab() {
        privateChatHeader = new Label("← Select a user from the sidebar");
        privateChatHeader.setStyle("-fx-text-fill:white;-fx-font-size:15;-fx-font-weight:bold;-fx-padding:10 14;");
        HBox header = new HBox(privateChatHeader);
        header.setStyle("-fx-background-color:#0d0d1a;");
        header.setAlignment(Pos.CENTER_LEFT);

        privateChatBox = new VBox(6);
        privateChatBox.setPadding(new Insets(10));
        privateChatBox.setStyle("-fx-background-color:#1e1e2e;");

        privateScroll = new ScrollPane(privateChatBox);
        privateScroll.setFitToWidth(true);
        privateScroll.setStyle("-fx-background-color:#1e1e2e;-fx-border-color:transparent;");

        typingLabel = new Label(" ");
        typingLabel.setStyle("-fx-text-fill:#888;-fx-font-size:11;-fx-padding:2 14;");

        privateInput = new TextField();
        privateInput.setPromptText("Message...");
        styleInput(privateInput);
        privateInput.setOnAction(e -> sendPrivateText());
        privateInput.setOnKeyTyped(e -> sendTyping());
        HBox.setHgrow(privateInput, Priority.ALWAYS);

        Button btnEmoji = emojiBtn();
        Button btnFile = iconBtn("📎", "#8e44ad");
        Button btnSend = iconBtn("➤", "#2196F3");

        btnEmoji.setOnAction(e -> showEmojiPicker(privateInput));
        btnFile.setOnAction(e -> chooseFile("PRIVATE"));
        btnSend.setOnAction(e -> sendPrivateText());

        HBox inputRow = new HBox(8, btnEmoji, privateInput, btnFile, btnSend);
        inputRow.setPadding(new Insets(8, 12, 8, 12));
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setStyle("-fx-background-color:#0d0d1a;");

        VBox tab = new VBox(header, privateScroll, typingLabel, inputRow);
        VBox.setVgrow(privateScroll, Priority.ALWAYS);
        return tab;
    }

    private VBox buildGroupTab() {
        groupChatHeader = new Label("# General");
        groupChatHeader.setStyle("-fx-text-fill:white;-fx-font-size:15;-fx-font-weight:bold;-fx-padding:10 14;");
        HBox header = new HBox(groupChatHeader);
        header.setStyle("-fx-background-color:#0d0d1a;");
        header.setAlignment(Pos.CENTER_LEFT);

        groupChatBox = new VBox(6);
        groupChatBox.setPadding(new Insets(10));
        groupChatBox.setStyle("-fx-background-color:#1e1e2e;");

        groupScroll = new ScrollPane(groupChatBox);
        groupScroll.setFitToWidth(true);
        groupScroll.setStyle("-fx-background-color:#1e1e2e;-fx-border-color:transparent;");

        groupInput = new TextField();
        groupInput.setPromptText("Group message...");
        styleInput(groupInput);
        groupInput.setOnAction(e -> sendGroupText());
        HBox.setHgrow(groupInput, Priority.ALWAYS);

        Button btnEmoji = emojiBtn();
        Button btnFile = iconBtn("📎", "#8e44ad");
        Button btnSend = iconBtn("➤", "#2196F3");

        btnEmoji.setOnAction(e -> showEmojiPicker(groupInput));
        btnFile.setOnAction(e -> chooseFile("GROUP"));
        btnSend.setOnAction(e -> sendGroupText());

        HBox inputRow = new HBox(8, btnEmoji, groupInput, btnFile, btnSend);
        inputRow.setPadding(new Insets(8, 12, 8, 12));
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setStyle("-fx-background-color:#0d0d1a;");

        VBox tab = new VBox(header, groupScroll, inputRow);
        VBox.setVgrow(groupScroll, Priority.ALWAYS);
        return tab;
    }

    private void onMessage(String line) {
        Platform.runLater(() -> {
            String[] p = line.split("\\|", -1);
            System.out.println("[ChatWindow] Received: " + line);
            switch (p[0]) {
                case "ONLINE_USERS" -> {
                    userListView.getItems().clear();
                    if (p.length > 1 && !p[1].isEmpty()) {
                        for (String u : p[1].split(",")) {
                            if (!u.equals(me)) userListView.getItems().add(u);
                        }
                    }
                }
                case "USER_JOINED" -> {
                    if (!p[1].equals(me) && !userListView.getItems().contains(p[1])) {
                        userListView.getItems().add(p[1]);
                    }
                }
                case "USER_LEFT" -> userListView.getItems().remove(p[1]);
                
                case "PRIVATE_HISTORY" -> {
                    if (activePeer != null && !loadedPrivateHistory.contains(activePeer)) {
                        privateMessages.putIfAbsent(activePeer, new ArrayList<>());
                        List<HBox> messages = privateMessages.get(activePeer);
                        messages.clear();
                        for (int i = 1; i < p.length; i++) {
                            String[] parts = p[i].split("::");
                            if (parts.length >= 2) {
                                boolean isMine = parts[0].equals(me);
                                boolean isFile = Boolean.parseBoolean(parts[3]);
                                if (isFile && parts.length > 4 && !parts[4].isEmpty()) {
                                    messages.add(buildFileBubble(parts[0], parts[4], isMine, false));
                                } else {
                                    messages.add(buildTextBubble(parts[0], parts[1], isMine, false));
                                }
                            }
                        }
                        loadedPrivateHistory.add(activePeer);
                        refreshPrivateChatDisplay();
                    }
                }
                
                case "GROUP_HISTORY" -> {
                    if (activeGroup != null && !loadedGroupHistory.contains(activeGroup)) {
                        groupMessages.putIfAbsent(activeGroup, new ArrayList<>());
                        List<HBox> messages = groupMessages.get(activeGroup);
                        messages.clear();
                        for (int i = 1; i < p.length; i++) {
                            String[] parts = p[i].split("::");
                            if (parts.length >= 2) {
                                boolean isMine = parts[0].equals(me);
                                boolean isFile = Boolean.parseBoolean(parts[3]);
                                if (isFile && parts.length > 4 && !parts[4].isEmpty()) {
                                    messages.add(buildFileBubble(parts[0], parts[4], isMine, true));
                                } else {
                                    messages.add(buildTextBubble(parts[0], parts[1], isMine, true));
                                }
                            }
                        }
                        loadedGroupHistory.add(activeGroup);
                        refreshGroupChatDisplay();
                    }
                }
                
                case "PRIVATE_TEXT" -> {
                    String sender = p[1];
                    String msg = p.length > 3 ? p[3] : "";
                    String peer = sender.equals(me) ? p[2] : sender;
                    boolean isMine = sender.equals(me);
                    HBox bubble = buildTextBubble(sender, msg, isMine, false);
                    privateMessages.putIfAbsent(peer, new ArrayList<>());
                    privateMessages.get(peer).add(bubble);
                    if (peer.equals(activePeer)) {
                        privateChatBox.getChildren().add(bubble);
                        scrollBottom(privateScroll);
                    }
                    if (!isMine) typingLabel.setText(" ");
                }
                
                case "GROUP_TEXT" -> {
                    String sender = p[1];
                    String group = p[2];
                    String msg = p.length > 3 ? p[3] : "";
                    boolean isMine = sender.equals(me);
                    HBox bubble = buildTextBubble(sender, msg, isMine, true);
                    groupMessages.putIfAbsent(group, new ArrayList<>());
                    groupMessages.get(group).add(bubble);
                    if (group.equals(activeGroup)) {
                        groupChatBox.getChildren().add(bubble);
                        scrollBottom(groupScroll);
                    }
                }
                
                case "FILE_READY" -> {
                    if (p.length < 6) return;
                    String scope = p[1];
                    String sender = p[2];
                    String target = p[3];
                    String filename = p[4];
                    String serverPath = p[5];
                    boolean isMine = sender.equals(me);
                    
                    HBox bubble = buildFileBubble(sender, serverPath, isMine, true);
                    
                    if ("PRIVATE".equals(scope)) {
                        String peer = isMine ? target : sender;
                        privateMessages.putIfAbsent(peer, new ArrayList<>());
                        privateMessages.get(peer).add(bubble);
                        if (peer.equals(activePeer)) {
                            privateChatBox.getChildren().add(bubble);
                            scrollBottom(privateScroll);
                        }
                    } else {
                        groupMessages.putIfAbsent(target, new ArrayList<>());
                        groupMessages.get(target).add(bubble);
                        if (target.equals(activeGroup)) {
                            groupChatBox.getChildren().add(bubble);
                            scrollBottom(groupScroll);
                        }
                    }
                }
                
                case "FILE_DOWNLOADED" -> {
                    String savedPath = p.length > 2 ? p[2] : null;
                    if (savedPath != null) {
                        File file = new File(savedPath);
                        if (file.exists()) {
                            HBox notif = buildSystemBubble("✓ File saved: " + file.getName());
                            if (activeGroup != null) {
                                groupMessages.putIfAbsent(activeGroup, new ArrayList<>());
                                groupMessages.get(activeGroup).add(notif);
                                groupChatBox.getChildren().add(notif);
                                scrollBottom(groupScroll);
                            } else if (activePeer != null) {
                                privateMessages.putIfAbsent(activePeer, new ArrayList<>());
                                privateMessages.get(activePeer).add(notif);
                                privateChatBox.getChildren().add(notif);
                                scrollBottom(privateScroll);
                            }
                        }
                    }
                }
                
                case "READY_FOR_FILE" -> {
                    if (pendingFile != null) {
                        File f = pendingFile;
                        pendingFile = null;
                        new Thread(() -> {
                            try {
                                net.sendFile(f);
                                Platform.runLater(() -> addSystemMessage("✓ File sent: " + f.getName()));
                            } catch (Exception ex) { ex.printStackTrace(); }
                        }).start();
                    }
                }
                
                case "TYPING" -> {
                    typingLabel.setText(p[1] + " is typing...");
                    new Thread(() -> {
                        try { Thread.sleep(2000); } catch (Exception e) {}
                        Platform.runLater(() -> typingLabel.setText(" "));
                    }).start();
                }
                
                case "GROUP_CREATE_OK" -> {
                    String g = p[1];
                    if (!myGroups.contains(g)) {
                        myGroups.add(g);
                        groupListView.getItems().add(g);
                    }
                    selectGroup(g);
                    addSystemMessage("Group created: " + g);
                }
                
                case "GROUP_JOIN_OK" -> {
                    String g = p[1];
                    if (!myGroups.contains(g)) {
                        myGroups.add(g);
                        groupListView.getItems().add(g);
                    }
                    selectGroup(g);
                    addSystemMessage("Joined group: " + g);
                }
                
                case "GROUP_JOIN_FAIL" -> alert("Group Error", p[1]);
                case "GROUP_LIST" -> showJoinDialog(p.length > 1 ? p[1].split(",") : new String[0]);
                case "GROUP_LEFT" -> {
                    String g = p[1];
                    myGroups.remove(g);
                    groupListView.getItems().remove(g);
                    groupMessages.remove(g);
                    loadedGroupHistory.remove(g);
                    selectGroup("General");
                    addSystemMessage("Left group: " + g);
                }
                
                case "YOU_WERE_ADDED_TO_GROUP" -> {
                    String group = p[1];
                    if (!myGroups.contains(group)) {
                        myGroups.add(group);
                        groupListView.getItems().add(group);
                    }
                    alert("Added to Group", "You were added to group: " + group);
                    addSystemMessage("You were added to group: " + group);
                }
                
                case "GROUP_USER_ADDED" -> alert("User Added", "User " + p[2] + " added to " + p[1]);
                case "GROUP_ERROR" -> alert("Group Error", p[1]);
            }
        });
    }
    
    private void addSystemMessage(String message) {
        HBox bubble = buildSystemBubble(message);
        if (activeGroup != null) {
            groupMessages.putIfAbsent(activeGroup, new ArrayList<>());
            groupMessages.get(activeGroup).add(bubble);
            groupChatBox.getChildren().add(bubble);
            scrollBottom(groupScroll);
        } else if (activePeer != null) {
            privateMessages.putIfAbsent(activePeer, new ArrayList<>());
            privateMessages.get(activePeer).add(bubble);
            privateChatBox.getChildren().add(bubble);
            scrollBottom(privateScroll);
        }
    }
    
    private HBox buildSystemBubble(String message) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-background-color:#555; -fx-text-fill:#ccc; -fx-background-radius:12; -fx-padding:6 12; -fx-font-size:11;");
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill:#555;-fx-font-size:10;");
        VBox bubble = new VBox(3, msgLabel, timeLabel);
        HBox row = new HBox(bubble);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(2, 12, 2, 12));
        return row;
    }
    
    private HBox buildTextBubble(String sender, String text, boolean isMine, boolean showName) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        VBox bubble = new VBox(3);
        
        if (showName && !isMine) {
            Label nameLabel = new Label(sender);
            nameLabel.setStyle("-fx-text-fill:#ff8f00;-fx-font-size:11;-fx-font-weight:bold;");
            bubble.getChildren().add(nameLabel);
        } else if (!isMine) {
            Label nameLabel = new Label(sender);
            nameLabel.setStyle("-fx-text-fill:#4fc3f7;-fx-font-size:11;-fx-font-weight:bold;");
            bubble.getChildren().add(nameLabel);
        }

        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(420);
        msgLabel.setStyle("-fx-background-color:" + (isMine ? "#2196F3" : "#2a2a3e") + 
                        ";-fx-text-fill:white;-fx-background-radius:16 4 16 16;-fx-padding:9 13;");
        bubble.getChildren().add(msgLabel);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill:#555;-fx-font-size:10;");
        bubble.getChildren().add(timeLabel);

        HBox row = new HBox(bubble);
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 12, 2, 12));
        return row;
    }

    private HBox buildFileBubble(String sender, String filePath, boolean isMine, boolean showName) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        VBox bubble = new VBox(3);
        
        if (showName && !isMine) {
            Label nameLabel = new Label(sender);
            nameLabel.setStyle("-fx-text-fill:#ff8f00;-fx-font-size:11;-fx-font-weight:bold;");
            bubble.getChildren().add(nameLabel);
        } else if (!isMine) {
            Label nameLabel = new Label(sender);
            nameLabel.setStyle("-fx-text-fill:#4fc3f7;-fx-font-size:11;-fx-font-weight:bold;");
            bubble.getChildren().add(nameLabel);
        }

        File file = new File(filePath);
        String fileName = file.getName();
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot > 0) ext = fileName.substring(dot + 1).toLowerCase();
        
        boolean isImage = ext.matches("jpg|jpeg|png|gif|bmp|webp");
        
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color:" + (isMine ? "#1565c0" : "#2a2a3e") + 
                          ";-fx-background-radius:12;-fx-padding:8;");
        
        if (isImage && file.exists()) {
            try {
                Image img = new Image("file:" + filePath, 200, 150, true, true);
                ImageView iv = new ImageView(img);
                iv.setPreserveRatio(true);
                iv.setFitWidth(180);
                iv.setFitHeight(135);
                iv.setStyle("-fx-cursor:hand;");
                iv.setOnMouseClicked(e -> openFile(filePath));
                
                Label nameLabel = new Label(fileName);
                nameLabel.setTextFill(Color.web("#aaa"));
                nameLabel.setStyle("-fx-font-size:10;-fx-cursor:hand;");
                nameLabel.setOnMouseClicked(e -> openFile(filePath));
                
                container.getChildren().addAll(iv, nameLabel);
            } catch (Exception e) {
                Button btn = new Button("🖼️ " + fileName);
                btn.setStyle("-fx-background-color:transparent;-fx-text-fill:white;-fx-cursor:hand;");
                btn.setOnAction(ev -> openFile(filePath));
                container.getChildren().add(btn);
            }
        } else {
            String icon = getFileIcon(ext);
            Button btn = new Button(icon + " " + fileName);
            btn.setStyle("-fx-background-color:transparent;-fx-text-fill:white;-fx-cursor:hand;");
            btn.setOnAction(e -> openFile(filePath));
            container.getChildren().add(btn);
            
            if (file.exists()) {
                long sizeKB = file.length() / 1024;
                String sizeText = sizeKB < 1024 ? sizeKB + " KB" : (sizeKB / 1024) + " MB";
                Label sizeLabel = new Label(sizeText);
                sizeLabel.setStyle("-fx-text-fill:#888;-fx-font-size:10;");
                container.getChildren().add(sizeLabel);
            }
        }
        
        bubble.getChildren().add(container);
        
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-text-fill:#555;-fx-font-size:10;");
        bubble.getChildren().add(timeLabel);

        HBox row = new HBox(bubble);
        row.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 12, 2, 12));
        return row;
    }
    
    private String getFileIcon(String ext) {
        switch (ext) {
            case "pdf": return "📄";
            case "doc": case "docx": return "📝";
            case "xls": case "xlsx": return "📊";
            case "ppt": case "pptx": return "📽️";
            case "mp3": case "wav": return "🎵";
            case "mp4": case "avi": return "🎬";
            case "zip": case "rar": return "📦";
            default: return "📎";
        }
    }

    private void refreshPrivateChatDisplay() {
        privateChatBox.getChildren().clear();
        if (activePeer != null && privateMessages.containsKey(activePeer)) {
            privateChatBox.getChildren().addAll(privateMessages.get(activePeer));
            scrollBottom(privateScroll);
        }
    }
    
    private void refreshGroupChatDisplay() {
        groupChatBox.getChildren().clear();
        if (activeGroup != null && groupMessages.containsKey(activeGroup)) {
            groupChatBox.getChildren().addAll(groupMessages.get(activeGroup));
            scrollBottom(groupScroll);
        }
    }

    private void selectPeer(String peer) {
        activePeer = peer;
        privateChatHeader.setText("💬 " + peer);
        refreshPrivateChatDisplay();
        if (!loadedPrivateHistory.contains(peer)) {
            net.send("GET_PRIVATE_HISTORY|" + peer);
        }
    }

    private void selectGroup(String group) {
        activeGroup = group;
        groupChatHeader.setText("# " + group);
        refreshGroupChatDisplay();
        groupListView.getSelectionModel().select(group);
        if (!loadedGroupHistory.contains(group)) {
            net.send("GET_GROUP_HISTORY|" + group);
        }
    }

    private void sendPrivateText() {
        if (activePeer == null) { alert("No User", "Select a user first."); return; }
        String text = privateInput.getText().trim();
        if (text.isEmpty()) return;
        
        HBox bubble = buildTextBubble(me, text, true, false);
        privateMessages.putIfAbsent(activePeer, new ArrayList<>());
        privateMessages.get(activePeer).add(bubble);
        privateChatBox.getChildren().add(bubble);
        scrollBottom(privateScroll);
        privateInput.clear();
        net.send("PRIVATE_TEXT|" + me + "|" + activePeer + "|" + text);
    }

    private void sendGroupText() {
        if (activeGroup == null) return;
        String text = groupInput.getText().trim();
        if (text.isEmpty()) return;
        
        HBox bubble = buildTextBubble(me, text, true, true);
        groupMessages.putIfAbsent(activeGroup, new ArrayList<>());
        groupMessages.get(activeGroup).add(bubble);
        groupChatBox.getChildren().add(bubble);
        scrollBottom(groupScroll);
        groupInput.clear();
        net.send("GROUP_TEXT|" + me + "|" + activeGroup + "|" + text);
    }

    private void sendTyping() {
        if (activePeer != null) net.send("TYPING|" + me + "|" + activePeer);
    }

    private void chooseFile(String mode) {
        if ("PRIVATE".equals(mode) && activePeer == null) {
            alert("No User", "Select a user first."); return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose File");
        File file = fc.showOpenDialog(stage);
        if (file == null) return;

        pendingFile = file;
        addSystemMessage("Sending file: " + file.getName());
        
        if ("PRIVATE".equals(mode)) {
            net.send("PRIVATE_FILE|" + me + "|" + activePeer + "|" + file.getName() + "|" + file.length());
        } else {
            net.send("GROUP_FILE|" + me + "|" + activeGroup + "|" + file.getName() + "|" + file.length());
        }
    }

    private void promptCreateGroup() {
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Create Group");
        dlg.setHeaderText("Enter group name:");
        dlg.showAndWait().ifPresent(name -> {
            name = name.trim();
            if (!name.isEmpty()) net.send("GROUP_CREATE|" + me + "|" + name);
        });
    }

    private void promptJoinGroup() {
        net.send("GROUP_LIST|" + me);
    }

    private void promptAddUserToGroup() {
        if (activeGroup == null || "General".equals(activeGroup)) {
            alert("Cannot Add", "Create your own group first.");
            return;
        }
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Add User");
        dlg.setHeaderText("Add user to: " + activeGroup);
        dlg.setContentText("Username:");
        dlg.showAndWait().ifPresent(username -> {
            if (!username.trim().isEmpty()) {
                net.send("ADD_USER_TO_GROUP|" + activeGroup + "|" + username.trim().toLowerCase());
            }
        });
    }

    private void leaveGroup() {
        if (activeGroup == null || "General".equals(activeGroup)) {
            alert("Cannot Leave", "You cannot leave General group.");
            return;
        }
        net.send("GROUP_LEAVE|" + me + "|" + activeGroup);
    }

    private void showJoinDialog(String[] allGroups) {
        Stage popup = new Stage();
        popup.initOwner(stage);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Join Group");

        ListView<String> list = new ListView<>();
        list.getItems().addAll(allGroups);
        list.setStyle("-fx-background-color:#1e1e2e;");
        
        Button joinBtn = new Button("Join");
        joinBtn.setStyle("-fx-background-color:#2196F3;-fx-text-fill:white;-fx-cursor:hand;");
        joinBtn.setOnAction(e -> {
            String sel = list.getSelectionModel().getSelectedItem();
            if (sel != null) {
                net.send("GROUP_JOIN|" + me + "|" + sel);
                popup.close();
            }
        });

        VBox box = new VBox(10, new Label("Available groups:"), list, joinBtn);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#12121e;");
        popup.setScene(new Scene(box, 280, 360));
        popup.show();
    }

    private void showEmojiPicker(TextField targetField) {
        Stage popup = new Stage();
        popup.initOwner(stage);
        popup.initModality(Modality.NONE);
        popup.setTitle("Emojis");
        
        FlowPane pane = new FlowPane();
        pane.setPadding(new Insets(8));
        pane.setHgap(4);
        pane.setVgap(4);
        pane.setPrefWrapLength(350);
        pane.setStyle("-fx-background-color:#1e1e2e;");

        for (String emoji : EMOJIS) {
            Button b = new Button(emoji);
            b.setStyle("-fx-font-size:18;-fx-background-color:transparent;-fx-cursor:hand;");
            b.setOnAction(e -> {
                targetField.insertText(targetField.getCaretPosition(), emoji);
                popup.close();
            });
            pane.getChildren().add(b);
        }
        popup.setScene(new Scene(pane, 370, 270));
        popup.show();
    }

    private void logout() {
        net.send("LOGOUT|" + me);
        net.disconnect();
        loginApp.showLogin();
    }

    private void scrollBottom(ScrollPane sp) { 
        Platform.runLater(() -> sp.setVvalue(1.0)); 
    }
    
    private void openFile(String path) { 
        if (path == null) return;
        try { 
            File file = new File(path);
            if (file.exists()) Desktop.getDesktop().open(file);
            else alert("File Error", "File not found: " + path);
        } catch (Exception e) { 
            alert("Cannot Open", e.getMessage());
        } 
    }
    
    static void alert(String title, String msg) { 
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait(); 
    }
    
    private static void styleInput(TextField f) { 
        f.setStyle("-fx-background-color:#2a2a3e;-fx-text-fill:white;-fx-prompt-text-fill:#555;-fx-background-radius:20;-fx-padding:9 14;"); 
    }
    
    private static Button emojiBtn() { 
        Button b = new Button("😊"); 
        b.setStyle("-fx-background-color:transparent;-fx-font-size:18;-fx-cursor:hand;"); 
        return b; 
    }
    
    private static Button iconBtn(String icon, String color) { 
        Button b = new Button(icon); 
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-background-radius:20;-fx-font-size:14;-fx-cursor:hand;-fx-padding:6 11;"); 
        return b; 
    }
    
    private static Button smallBtn(String text, String color) { 
        Button b = new Button(text); 
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-background-radius:6;-fx-padding:5 12;-fx-cursor:hand;"); 
        return b; 
    }
    
    private static Button sideBtn(String text, String color) { 
        Button b = new Button(text); 
        b.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-background-radius:5;-fx-font-size:11;-fx-cursor:hand;-fx-padding:4 7;"); 
        return b; 
    }
    
    private static Label sideLabel(String text) { 
        Label l = new Label(text); 
        l.setStyle("-fx-text-fill:#666;-fx-font-size:10;-fx-font-weight:bold;-fx-padding:8 8 3 8;"); 
        l.setMaxWidth(Double.MAX_VALUE); 
        return l; 
    }
    
    static class OnlineUserCell extends ListCell<String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            Label label = new Label("● " + item);
            label.setTextFill(Color.web("#4CAF50"));
            label.setStyle("-fx-font-size:13;-fx-padding:6;");
            setGraphic(label);
            setStyle("-fx-background-color:transparent;");
        }
    }
    
    static class ColorCell extends ListCell<String> {
        private final String color, prefix;
        ColorCell(String color, String prefix) { this.color = color; this.prefix = prefix; }
        @Override protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) { setText(null); setStyle(""); return; }
            setText(prefix + " " + item);
            setStyle("-fx-text-fill:" + color + ";-fx-background-color:transparent;-fx-font-size:13;-fx-padding:6;");
        }
    }
}
