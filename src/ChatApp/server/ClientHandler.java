package ChatApp.server;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Map<String, ClientHandler> onlineMap;
    private final DatabaseManager db;

    private BufferedReader reader;
    private PrintWriter writer;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private String username;

    private static final String FILES_DIR = "server_files/";
    private static final int CHUNK = 4096;

    public ClientHandler(Socket socket, Map<String, ClientHandler> onlineMap, DatabaseManager db) {
        this.socket = socket;
        this.onlineMap = onlineMap;
        this.db = db;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());

            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                if ("LOGIN".equals(p[0])) {
                    if (handleLogin(p)) break;
                } else if ("REGISTER".equals(p[0])) {
                    handleRegister(p);
                }
            }

            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|", -1);
                System.out.println("[Server] From " + username + ": " + line);
                switch (p[0]) {
                    case "PRIVATE_TEXT" -> handlePrivateText(p);
                    case "PRIVATE_FILE" -> handlePrivateFile(p);
                    case "GROUP_TEXT" -> handleGroupText(p);
                    case "GROUP_FILE" -> handleGroupFile(p);
                    case "GROUP_CREATE" -> handleGroupCreate(p);
                    case "GROUP_JOIN" -> handleGroupJoin(p);
                    case "GROUP_LEAVE" -> handleGroupLeave(p);
                    case "GROUP_LIST" -> send("GROUP_LIST|" + String.join(",", db.getAllGroups()));
                    case "GET_ONLINE_USERS" -> sendOnlineList();
                    case "GET_PRIVATE_HISTORY" -> sendPrivateHistory(p);
                    case "GET_GROUP_HISTORY" -> sendGroupHistory(p);
                    case "ADD_USER_TO_GROUP" -> handleAddUserToGroup(p);
                    case "TYPING" -> handleTyping(p);
                    case "LOGOUT" -> { disconnect(); return; }
                }
            }
        } catch (IOException e) {
            System.out.println("[Server] " + username + " disconnected");
        } finally {
            disconnect();
        }
    }

    private boolean handleLogin(String[] p) {
        if (p.length < 3) { send("AUTH_FAIL|Missing fields"); return false; }
        String user = p[1].trim().toLowerCase();
        String pass = p[2].trim();

        if (onlineMap.containsKey(user)) {
            send("AUTH_FAIL|User already logged in");
            return false;
        }
        if (!db.validateUser(user, pass)) {
            send("AUTH_FAIL|Invalid username or password");
            return false;
        }

        this.username = user;
        onlineMap.put(user, this);
        db.addMember("General", user);

        List<String> groups = db.getUserGroups(user);
        send("AUTH_OK|" + user + "|" + String.join(",", groups));
        broadcastAll("USER_JOINED|" + user);
        broadcastOnlineList();

        System.out.println("[Server] " + user + " logged in. Online: " + onlineMap.size());
        return true;
    }

    private void handleRegister(String[] p) {
        if (p.length < 3) { send("REG_FAIL|Missing fields"); return; }
        String user = p[1].trim().toLowerCase();
        String pass = p[2].trim();

        if (user.isEmpty() || pass.isEmpty()) {
            send("REG_FAIL|Username and password required");
            return;
        }
        if (user.length() < 3) {
            send("REG_FAIL|Username must be at least 3 characters");
            return;
        }
        if (db.registerUser(user, pass)) {
            send("REG_OK|Account created! You can now log in.");
        } else {
            send("REG_FAIL|Username already taken");
        }
    }

    private void handlePrivateText(String[] p) {
        if (p.length < 4) return;
        String receiver = p[2];
        String message = p[3];
        
        db.savePrivateMessage(username, receiver, message, null, false);
        String out = "PRIVATE_TEXT|" + username + "|" + receiver + "|" + message;
        sendTo(receiver, out);
        System.out.println("[Server] Private msg " + username + " -> " + receiver);
    }

    private void handlePrivateFile(String[] p) {
        if (p.length < 5) return;
        String receiver = p[2];
        String filename = p[3];
        long fileSize = Long.parseLong(p[4]);

        String savedPath = receiveFile(filename, fileSize);
        if (savedPath == null) return;

        db.savePrivateMessage(username, receiver, "[File: " + filename + "]", savedPath, true);
        String notify = "FILE_READY|PRIVATE|" + username + "|" + receiver + "|" + filename + "|" + savedPath;
        sendTo(receiver, notify);
        System.out.println("[Server] File " + username + " -> " + receiver + ": " + filename);
    }

    private void handleGroupText(String[] p) {
        if (p.length < 4) return;
        String group = p[2];
        String message = p[3];
        
        db.saveGroupMessage(username, group, message, null, false);
        String out = "GROUP_TEXT|" + username + "|" + group + "|" + message;
        broadcastToGroupExceptSender(group, out);
    }

    private void handleGroupFile(String[] p) {
        if (p.length < 5) return;
        String group = p[2];
        String filename = p[3];
        long fileSize = Long.parseLong(p[4]);

        String savedPath = receiveFile(filename, fileSize);
        if (savedPath == null) return;

        db.saveGroupMessage(username, group, "[File: " + filename + "]", savedPath, true);
        String notify = "FILE_READY|GROUP|" + username + "|" + group + "|" + filename + "|" + savedPath;
        broadcastToGroupExceptSender(group, notify);
    }

    private void handleGroupCreate(String[] p) {
        if (p.length < 3) return;
        String group = p[2].trim();
        db.addMember(group, username);
        send("GROUP_CREATE_OK|" + group);
    }

    private void handleGroupJoin(String[] p) {
        if (p.length < 3) return;
        String group = p[2].trim();
        if (!db.groupExists(group)) {
            send("GROUP_JOIN_FAIL|Group does not exist");
            return;
        }
        db.addMember(group, username);
        send("GROUP_JOIN_OK|" + group);
    }

    private void handleGroupLeave(String[] p) {
        if (p.length < 3) return;
        String group = p[2].trim();
        if ("General".equals(group)) {
            send("ERROR|Cannot leave General group");
            return;
        }
        db.removeMember(group, username);
        send("GROUP_LEFT|" + group);
    }

    private void handleAddUserToGroup(String[] p) {
        if (p.length < 3) return;
        String group = p[1].trim();
        String targetUser = p[2].trim().toLowerCase();
        
        if (!db.groupExists(group)) {
            send("GROUP_ERROR|Group does not exist");
            return;
        }
        if (!db.isMember(group, targetUser)) {
            db.addMember(group, targetUser);
            send("GROUP_USER_ADDED|" + group + "|" + targetUser);
            sendTo(targetUser, "YOU_WERE_ADDED_TO_GROUP|" + group);
        } else {
            send("GROUP_ERROR|User already in group");
        }
    }

    private void sendPrivateHistory(String[] p) {
        if (p.length < 2) return;
        String otherUser = p[1];
        List<String> history = db.getPrivateHistory(username, otherUser);
        StringBuilder sb = new StringBuilder("PRIVATE_HISTORY|");
        for (String msg : history) {
            sb.append(msg).append("|");
        }
        send(sb.toString());
    }

    private void sendGroupHistory(String[] p) {
        if (p.length < 2) return;
        String group = p[1];
        List<String> history = db.getGroupHistory(group);
        StringBuilder sb = new StringBuilder("GROUP_HISTORY|");
        for (String msg : history) {
            sb.append(msg).append("|");
        }
        send(sb.toString());
    }

    private void handleTyping(String[] p) {
        if (p.length < 3) return;
        sendTo(p[2], "TYPING|" + username);
    }

    private String receiveFile(String filename, long fileSize) {
        try {
            send("READY_FOR_FILE");
            new File(FILES_DIR).mkdirs();
            String safeName = System.currentTimeMillis() + "_" + filename;
            String path = FILES_DIR + safeName;

            try (FileOutputStream fos = new FileOutputStream(path)) {
                byte[] buf = new byte[CHUNK];
                long rem = fileSize;
                int n;
                while (rem > 0 && (n = dataIn.read(buf, 0, (int) Math.min(CHUNK, rem))) != -1) {
                    fos.write(buf, 0, n);
                    rem -= n;
                }
            }
            System.out.println("[Server] File saved: " + path);
            return path;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized void send(String line) {
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    private void sendTo(String target, String line) {
        ClientHandler ch = onlineMap.get(target);
        if (ch != null) ch.send(line);
    }

    private void broadcastAll(String line) {
        for (ClientHandler ch : onlineMap.values()) {
            ch.send(line);
        }
    }

    private void broadcastOnlineList() {
        String line = "ONLINE_USERS|" + String.join(",", onlineMap.keySet());
        broadcastAll(line);
    }

    private void sendOnlineList() {
        send("ONLINE_USERS|" + String.join(",", onlineMap.keySet()));
    }

    private void broadcastToGroup(String group, String line) {
        List<String> members = db.getMembers(group);
        for (String member : members) {
            sendTo(member, line);
        }
    }
    
    private void broadcastToGroupExceptSender(String group, String line) {
        List<String> members = db.getMembers(group);
        for (String member : members) {
            if (!member.equals(username)) {
                sendTo(member, line);
            }
        }
    }

    private void disconnect() {
        if (username != null) {
            onlineMap.remove(username);
            broadcastAll("USER_LEFT|" + username);
            broadcastOnlineList();
            System.out.println("[Server] " + username + " disconnected");
        }
        try { socket.close(); } catch (IOException ignored) {}
    }
}
