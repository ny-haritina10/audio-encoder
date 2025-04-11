package mg.itu.main;

import java.io.File;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import mg.itu.decoder.WavPositionBasedDecoder;
import mg.itu.encoder.WavPositionBasedEncoder;
import mg.itu.sequence.SampleSequenceGenerator;

public class Main {

    public static void main(String[] args) {
        try {
            // WAV Steganography Test
            String inputWavPath = "D:\\Studies\\ITU\\S6\\INF-310_Codage\\audio-encoder\\wav\\input_distortion.wav";
            String message = "Hidden audio message!";
            String outputWavPath = "D:\\Studies\\ITU\\S6\\INF-310_Codage\\audio-encoder\\wav\\output.wav";
            
            // Calculate required positions (32 bits for length + 8 bits per character)
            int requiredPositions = 32 + message.length() * 8;
            
            // Get audio data and format
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(inputWavPath));
            byte[] audioBytes = audioInputStream.readAllBytes();
            javax.sound.sampled.AudioFormat format = audioInputStream.getFormat();
            int sampleSizeInBytes = format.getSampleSizeInBits() / 8; // 16-bit = 2 bytes
            int totalSamples = audioBytes.length / sampleSizeInBytes;
            audioInputStream.close();
            
            // Generate sequence-based sample positions
            int sequenceStep = 4; // Every 4th sample
            SampleSequenceGenerator sequenceGen = new SampleSequenceGenerator(sequenceStep);
            List<Integer> samplePositions = sequenceGen.generatePositionsFromSequence(totalSamples, requiredPositions);
            
            // Encode with sequence-based positions
            WavPositionBasedEncoder encoder = new WavPositionBasedEncoder();
            String binaryMessage = WavPositionBasedEncoder.messageToBinary(message); 
            encoder.encodeWithPositions(audioBytes, binaryMessage, samplePositions, format, outputWavPath);

            System.out.println("Message encoded successfully into WAV with " + samplePositions.size() + " sequence-based positions!");
            
            WavPositionBasedDecoder decoder = new WavPositionBasedDecoder();
            String extractedMessage = decoder.decode(outputWavPath, samplePositions);
            System.out.println("Extracted message from WAV: " + extractedMessage);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}