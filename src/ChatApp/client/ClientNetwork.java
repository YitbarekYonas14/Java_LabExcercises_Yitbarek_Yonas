package ChatApp.client;

import java.io.*;
import java.net.Socket;

public class ClientNetwork implements Runnable {

    public interface MessageListener {
        void onMessage(String line);
        void onDisconnected();
    }

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private DataInputStream dataIn;
    private DataOutputStream dataOut;
    private MessageListener listener;
    private volatile boolean running = false;

    private static final int CHUNK = 4096;
    public static final String FILES_DIR = "client_files/";

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
            return true;
        } catch (IOException e) {
            System.err.println("[Client] Cannot connect: " + e.getMessage());
            return false;
        }
    }

    public String readOneLine() {
        try { return reader.readLine(); }
        catch (IOException e) { return null; }
    }

    public void startListening(MessageListener listener) {
        this.listener = listener;
        this.running = true;
        Thread t = new Thread(this, "NetworkReader");
        t.setDaemon(true);
        t.start();
    }

    @Override
    public void run() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                if (line.startsWith("INCOMING_FILE|")) {
                    String[] p = line.split("\\|", -1);
                    String fname = p[1];
                    long fsize = Long.parseLong(p[2]);
                    sendRaw("CLIENT_READY");
                    String savedAs = receiveFileBinary(fname, fsize);
                    listener.onMessage("FILE_DOWNLOADED|" + fname + "|" + savedAs);
                } else {
                    listener.onMessage(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (running && listener != null) listener.onDisconnected();
        }
    }

    public synchronized void send(String line) {
        if (writer != null) {
            writer.println(line);
            writer.flush();
        }
    }

    private void sendRaw(String line) {
        writer.println(line);
        writer.flush();
    }

    public synchronized void sendFile(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buf = new byte[CHUNK];
            int n;
            while ((n = fis.read(buf)) != -1) {
                dataOut.write(buf, 0, n);
            }
            dataOut.flush();
        }
    }

    private String receiveFileBinary(String serverFilename, long fileSize) {
        try {
            new File(FILES_DIR).mkdirs();
            
            String originalName = serverFilename;
            if (serverFilename.contains("_")) {
                int underscoreIndex = serverFilename.indexOf('_');
                if (underscoreIndex > 0 && underscoreIndex < serverFilename.length() - 1) {
                    originalName = serverFilename.substring(underscoreIndex + 1);
                }
            }
            
            String fullPath = FILES_DIR + originalName;
            File file = new File(fullPath);
            int counter = 1;
            while (file.exists()) {
                String name = originalName;
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex > 0) {
                    String baseName = originalName.substring(0, dotIndex);
                    String extension = originalName.substring(dotIndex);
                    fullPath = FILES_DIR + baseName + "_" + counter + extension;
                } else {
                    fullPath = FILES_DIR + originalName + "_" + counter;
                }
                file = new File(fullPath);
                counter++;
            }

            try (FileOutputStream fos = new FileOutputStream(fullPath)) {
                byte[] buf = new byte[CHUNK];
                long rem = fileSize;
                int n;
                while (rem > 0 && (n = dataIn.read(buf, 0, (int) Math.min(CHUNK, rem))) != -1) {
                    fos.write(buf, 0, n);
                    rem -= n;
                }
            }
            System.out.println("[Client] File saved: " + fullPath);
            return fullPath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void disconnect() {
        running = false;
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
}
