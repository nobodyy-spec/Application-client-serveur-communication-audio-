public interface ChatController {

    void onConnect(String host, int port);

    void onSendText(String message);

    void onStartRecording();

    void onStopRecording();
    void playAudio(byte[] audioData);

}
