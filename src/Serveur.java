import  java.net.*;
import java.io.*;
public class Serveur {
    public static void main(String[] args) {
    try (ServerSocket server =  new ServerSocket(5000)){
        System.out.println("en attente de connexion ");
        Socket clientSocket = server.accept();
        System.out.println(" connected!");
        //flux entree sorties//
        BufferedReader in= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out= new PrintWriter(clientSocket.getOutputStream(),true);
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        //thread de sender
        Thread sender = new Thread(() -> {
            String message;
            try {


            while((message = keyboard.readLine()) != null) {
                System.out.println("Client: " + message);
            } }
            catch (IOException e) {
                e.printStackTrace();
            }
        });
        sender.start();
        Thread receiver = new Thread(() -> {
           String message;
           try{
               while ((message = in.readLine()) != null) {
                   System.out.println("Serveur: " + message);
               }
           } catch (IOException e) {
               System.out.println("Erreur de lecture : " + e.getMessage());}


        });
        receiver.start();




        System.out.println("Connexion ended.");
    } catch (IOException e) { e.printStackTrace(); } }}




