package mg.itu.main;

import java.util.List;

import mg.itu.decoder.WavPositionBasedDecoder;
import mg.itu.encoder.WavPositionBasedEncoder;

public class Main {

    public static void main(String[] args) {
        try {
            // WAV Steganography Test
            String inputWavPath = "D:\\Studies\\ITU\\S6\\INF-310_Codage\\audio-encoder\\wav\\input_distortion.wav";
            String message = "Hidden audio message!";
            String outputWavPath = "D:\\Studies\\ITU\\S6\\INF-310_Codage\\audio-encoder\\wav\\output.wav";
            
            // Encode with random positions
            WavPositionBasedEncoder encoder = new WavPositionBasedEncoder();
            List<Integer> samplePositions = encoder.encode(inputWavPath, message, outputWavPath);
            System.out.println("Message encoded successfully into WAV with " + samplePositions.size() + " random positions!");
            
            // Decode using the same positions
            WavPositionBasedDecoder decoder = new WavPositionBasedDecoder();
            String extractedMessage = decoder.decode(outputWavPath, samplePositions);
            System.out.println("Extracted message from WAV: " + extractedMessage);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}