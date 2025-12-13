import java.net.*;
import java.io.*;

public class Serveur {
    private static Audio audioModule = new Audio(); // Audio instance

    public static void main(String[] args) {
        try(ServerSocket server = new ServerSocket(5000)) {

            System.out.println("En attente de connexion...");
            Socket clientSocket = server.accept();
            System.out.println("Client connecté !");

            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();

            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

            // Thread envoyer texte/audio → client
            Thread sender = new Thread(() -> {
                String message;
                try {
                    while((message = keyboard.readLine()) != null){
                        if(message.equalsIgnoreCase("VOICE")) {
                            System.out.println("🎤 Enregistrement serveur... (Entrée pour stopper)");
                            audioModule.startRecording();
                            keyboard.readLine();
                            byte[] audioData = audioModule.stopRecording();
                            if(audioData != null && audioData.length > 0) {
                                out.write("[AUDIO]".getBytes());
                                out.write(audioData);
                                out.flush();
                                System.out.println("✓ Audio serveur envoyé !");
                            }
                        } else {
                            out.write((message + "\n").getBytes());
                            out.flush();
                        }
                    }
                } catch(IOException e){ e.printStackTrace(); }
            });
            sender.start();

            // Thread recevoir texte/audio ← client
            Thread receiver = new Thread(() -> {
                try {
                    byte[] buffer = new byte[8192];
                    int len;
                    while((len = in.read(buffer)) != -1) {
                        String check = new String(buffer, 0, Math.min(7, len));
                        if(check.startsWith("[AUDIO]")) {
                            byte[] audioData = new byte[len - 7];
                            System.arraycopy(buffer, 7, audioData, 0, audioData.length);
                            System.out.println("🎤 Message vocal reçu du client - lecture...");
                            audioModule.playAudio(audioData);
                        } else {
                            String msg = new String(buffer, 0, len);
                            System.out.println("Client : " + msg.trim());
                        }
                    }
                } catch(IOException e) {
                    System.out.println("Erreur de lecture : " + e.getMessage());
                }
            });
            receiver.start();

        } catch(IOException e){ e.printStackTrace(); }
    }
}
