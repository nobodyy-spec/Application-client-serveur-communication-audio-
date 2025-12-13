import java.io.IOException;

public class NetworkController implements ChatController {

    private YapChatGUI gui;
    private final Audio audioModule = new Audio(); // ajouté 'final'
    private Client client; // maintenant initialisé dans onConnect

    public void setGUI(YapChatGUI gui) {
        this.gui = gui;
    }

    @Override
    public void onConnect(String host, int port) {
        // INITIALISATION du client
        try {
            this.client = new Client(host, port);
            gui.enableChat();
        } catch (IOException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
            // Afficher un message d'erreur dans le GUI
        }
    }

    @Override
    public void onSendText(String message) {
        if (client != null) {
            client.sendMessage(message);
        }
    }

    @Override
    public void onStartRecording() {
        audioModule.startRecording();
    }

    @Override
    public void onStopRecording() {
        byte[] audioData = audioModule.stopRecording();
        if (client != null && audioData != null) {
            client.sendAudio(audioData);
        }
    }

    public void onMessageReceived(String msg) {
        if (gui != null) {
            gui.appendMessage("FRIEND", msg);
        }
    }
}