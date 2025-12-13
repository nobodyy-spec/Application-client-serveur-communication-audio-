import java.net.*;
import java.io.*;

public class Client {
    private Socket socket;
    private OutputStream out;
    private InputStream in;
    private static Audio audioModule = new Audio();

    // CONSTRUCTEUR pour NetworkController
    public Client(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = socket.getOutputStream();
        this.in = socket.getInputStream();
        startReceiverThread();
        System.out.println("Connecté au serveur " + host + ":" + port);
    }

    // MÉTHODE manquante pour envoyer texte
    public void sendMessage(String message) {
        try {
            out.write((message + "\n").getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // MÉTHODE manquante pour envoyer audio
    public void sendAudio(byte[] audioData) {
        if (audioData == null || audioData.length == 0) return;

        try {
            out.write("[AUDIO]".getBytes()); // identifier audio
            out.write(audioData);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Thread pour recevoir les messages du serveur
    private void startReceiverThread() {
        Thread receiver = new Thread(() -> {
            try {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    String check = new String(buffer, 0, Math.min(7, len));

                    if (check.startsWith("[AUDIO]")) {
                        byte[] audioData = new byte[len - 7];
                        System.arraycopy(buffer, 7, audioData, 0, audioData.length);
                        audioModule.playAudio(audioData);
                    } else {
                        String msg = new String(buffer, 0, len);
                        System.out.println("Serveur : " + msg.trim());
                    }
                }
            } catch (IOException e) {
                System.out.println("Connexion au serveur perdue");
            }
        });
        receiver.start();
    }

    // Pour fermer la connexion
    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) { }
    }

    // MAIN original (peut être gardé ou supprimé)
    public static void main(String[] args) {
        try {
            Client client = new Client("localhost", 5000);
            // ... reste du code ...
        } catch (IOException e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}