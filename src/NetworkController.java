import java.io.IOException;

public class NetworkController implements ChatController {

    private YapChatGUI gui;
    private final Audio audioModule = new Audio();
    private Client client;

    public void setGUI(YapChatGUI gui) {
        this.gui = gui;
    }

    @Override
    public void onConnect(String host, int port) {
        try {
            this.client = new Client(host, port);
            gui.enableChat();
        } catch (IOException e) {
            System.err.println("Erreur de connexion: " + e.getMessage());
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
            gui.appendVoiceMessage("YOU", audioData);
        }
    }

    // ✅ NEW METHOD
    @Override
    public void playAudio(byte[] audioData) {
        audioModule.playAudio(audioData);
    }

    // Called by Client when receiving text
    public void onMessageReceived(String msg) {
        if (gui != null) {
            gui.appendMessage("FRIEND", msg);
        }
    }

    // Called by Client when receiving audio
    public void onAudioReceived(byte[] audioData) {
        if (gui != null) {
            gui.appendVoiceMessage("FRIEND", audioData);
        }
    }
}