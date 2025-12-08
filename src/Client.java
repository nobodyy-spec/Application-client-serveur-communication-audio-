import java.net.*;
import java.io.*;

public class Client {
    public static void main(String[] args) {
        try {
            // 1. Connexion au serveur et envoi de message au serveur
            Socket sock_client = new Socket("localhost", 5000);
            PrintWriter out = new PrintWriter(sock_client.getOutputStream(), true);
            out.println("Bonjour serveur"); // <-- envoie direct


            // 2. Réception du message du serveur
            InputStream in = sock_client.getInputStream();
            byte[] message = new byte[1000];
            int nbrbitsrecus = in.read(message);

            if (nbrbitsrecus > 0) {
                System.out.println("Message du serveur : " +
                        new String(message, 0, nbrbitsrecus));

                // 3. Envoi d'une réponse au serveur
               // Important pour envoyer les données
            }

            // 4. Fermeture des ressources
            in.close();
            sock_client.close();

        } catch (IOException e) {
            System.err.println("Erreur client : " + e.getMessage());
        }
    }
}