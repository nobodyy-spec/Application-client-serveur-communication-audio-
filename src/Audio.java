import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class Audio {
    
    // ==================== CONFIGURATION ====================
    private static final float SAMPLE_RATE = 44100.0f;
    private static final int SAMPLE_SIZE = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    
    private AudioFormat audioFormat;
    private AtomicBoolean isRecording = new AtomicBoolean(false);
    private TargetDataLine microphone;
    private SourceDataLine speaker;
    private ByteArrayOutputStream audioBuffer;
    
    // ==================== CONSTRUCTEUR ====================
    public Audio() {
        audioFormat = new AudioFormat(
            SAMPLE_RATE,
            SAMPLE_SIZE,
            CHANNELS,
            SIGNED,
            BIG_ENDIAN
        );
    }
    
    // ==================== ENREGISTREMENT START/STOP ====================
         
    public boolean startRecording() {
        if (isRecording.get()) {
            return false; 
        }
        
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(audioFormat);
            microphone.start();
            
            audioBuffer = new ByteArrayOutputStream();
            isRecording.set(true);
            
            // Thread pour capturer l'audio en continu
            new Thread(this::captureAudioLoop).start();
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
     //Arrêter l'enregistrement (quand bouton relâché)
     // Retourne les données audio enregistrées
     
    public byte[] stopRecording() {
        if (!isRecording.get()) {
            return null;
        }
        
        isRecording.set(false);
        
        try {
            if (microphone != null) {
                microphone.stop();
                microphone.close();
                microphone = null;
            }
            
            if (audioBuffer != null) {
                byte[] rawAudio = audioBuffer.toByteArray();
                audioBuffer.close();
                audioBuffer = null;
                
                // Normaliser l'audio
                return normalizeAudio(rawAudio);
            }
            
        } catch (Exception e) {
           
        }
        
        return null;
    }
    
     // Boucle de capture audio (tourne en arrière-plan)
     
    private void captureAudioLoop() {
        byte[] buffer = new byte[4096]; // Buffer de 4KB
        
        while (isRecording.get() && microphone != null) {
            try {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0 && audioBuffer != null) {
                    audioBuffer.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                break;
            }
        }
    }

     // Jouer un message audio reçu
     
    public void playAudio(byte[] audioData) {
        if (audioData == null || audioData.length == 0) {
            return;
        }
        
        new Thread(() -> {
            try {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                speaker = (SourceDataLine) AudioSystem.getLine(info);
                speaker.open(audioFormat);
                speaker.start();
                
                speaker.write(audioData, 0, audioData.length);
                speaker.drain();
                speaker.stop();
                speaker.close();
                speaker = null;
                
            } catch (Exception e) {
        
            }
        }).start();
    }
    
     // Pour le streaming en temps réel (appels vocaux)
    public void receiveAudioChunk(byte[] chunk) {
        if (chunk == null || chunk.length == 0) return;
        
        try {
            if (speaker == null || !speaker.isOpen()) {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
                speaker = (SourceDataLine) AudioSystem.getLine(info);
                speaker.open(audioFormat);
                speaker.start();
            }
            
            speaker.write(chunk, 0, chunk.length);
            
        } catch (Exception e) {
            // Silence
        }
    }
    
     // Arrêter la lecture (pour les appels)
     
    public void stopPlayback() {
        if (speaker != null && speaker.isOpen()) {
            speaker.stop();
            speaker.close();
            speaker = null;
        }
    }

     // Interface pour streaming (envoi en temps réel)
    public interface StreamCallback {
        void onAudioData(byte[] data);
    }
    
    private StreamCallback streamCallback;
    
            // Démarrer le streaming (pour appels)
    public boolean startStreaming(StreamCallback callback) {
        if (isRecording.get()) {
            return false;
        }
        
        this.streamCallback = callback;
        
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(audioFormat);
            microphone.start();
            
            isRecording.set(true);
            
            new Thread(this::streamAudioLoop).start();
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
            // Arrêter le streaming
     
    public void stopStreaming() {
        isRecording.set(false);
        streamCallback = null;
        
        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }
    }
    
    private void streamAudioLoop() {
        byte[] buffer = new byte[2048]; // Buffer plus petit pour moins de latence
        
        while (isRecording.get() && microphone != null && streamCallback != null) {
            try {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    byte[] normalized = normalizeAudio(buffer, bytesRead);
                    streamCallback.onAudioData(normalized);
                }
            } catch (Exception e) {
                break;
            }
        }
    }
                // Normalisation du volume
    
    private byte[] normalizeAudio(byte[] audioData) {
        return normalizeAudio(audioData, audioData.length);
    }
    
    private byte[] normalizeAudio(byte[] audioData, int length) {
        if (length <= 0) return new byte[0];
        
        byte[] normalized = new byte[length];
        int maxAmplitude = 0;
        
        for (int i = 0; i < length; i++) {
            int amplitude = Math.abs(audioData[i]);
            if (amplitude > maxAmplitude) {
                maxAmplitude = amplitude;
            }
        }
        
        if (maxAmplitude > 20 && maxAmplitude < 120) {
            float factor = 120.0f / maxAmplitude;
            for (int i = 0; i < length; i++) {
                int value = (int)(audioData[i] * factor);
                normalized[i] = (byte) Math.max(-128, Math.min(127, value));
            }
        } else {
            System.arraycopy(audioData, 0, normalized, 0, length);
        }
        
        return normalized;
    }
    
    public byte[] compressAudio(byte[] audioData, float ratio) {
        if (ratio >= 1.0f || audioData == null) return audioData;
        
        int newLength = (int)(audioData.length * ratio);
        byte[] compressed = new byte[newLength];
        
        for (int i = 0; i < newLength; i++) {
            int sourceIndex = (int)(i / ratio);
            if (sourceIndex < audioData.length) {
                compressed[i] = audioData[sourceIndex];
            }
        }
        
        return compressed;
    }
    
    public boolean isRecording() {
        return isRecording.get();
    }
   
    public static void main(String[] args) {
    
    }
}