import java.net.*;
import java.io.*;

public class Serveur {
    public static void main(String[] args) {
        try(ServerSocket server = new ServerSocket(5000)){

            System.out.println("En attente de connexion...");
            Socket clientSocket = server.accept();
            System.out.println("Client connecté !");

            // Flux bytenon
            InputStream in = clientSocket.getInputStream();
            OutputStream out = clientSocket.getOutputStream();

            // Lecture clavier (pour envoyer des données)
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

            // Thread d'envoi (écriture vers client)
            Thread sender = new Thread(() -> {
                String message;
                try{
                    while((message = keyboard.readLine()) != null){
                        out.write((message + "\n").getBytes()); // envoyer sous forme de bytes
                        out.flush();
                    }
                }catch(IOException e){ e.printStackTrace(); }
            });
            sender.start();

            // Thread de réception (lecture depuis client)
            Thread receiver = new Thread(() -> {
                try{
                    byte[] buffer = new byte[1024];
                    int len;
                    while((len = in.read(buffer)) != -1){
                        String message = new String(buffer, 0, len);
                        System.out.println("Client : " + message);
                    }
                }catch(IOException e){
                    System.out.println("Erreur de lecture : " + e.getMessage());
                }
            });
            receiver.start();

        }catch(IOException e){ e.printStackTrace(); }
    }
}
