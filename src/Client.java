import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket sock_client = new Socket("localhost", 5000);
            System.out.println("Connecté au serveur !");

            // Flux byte
            InputStream in = sock_client.getInputStream();
            OutputStream out = sock_client.getOutputStream();

            // Envoi automatique d'un premier message
            out.write("connected to server\n".getBytes());
            out.flush();

            // Clavier pour envoyer des messages
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

            // Thread envoyer messages → serveur
            Thread sender = new Thread(() -> {
                String message;
                try {
                    while ((message = keyboard.readLine()) != null) {
                        out.write((message + "\n").getBytes());  // send bytes
                        out.flush();
                    }
                } catch (IOException e) { e.printStackTrace(); }
            });
            sender.start();

            // Thread recevoir messages ← serveur
            Thread receiver = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        String message = new String(buffer, 0, len);
                        System.out.println("Serveur : " + message);
                    }
                } catch (IOException e) {
                    System.out.println("Erreur de lecture : " + e.getMessage());
                }
            });
            receiver.start();

        } catch (IOException e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}
