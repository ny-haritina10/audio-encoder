package mg.itu.decoder;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WavPositionBasedDecoder {
    
    public String decode(String wavPath, List<Integer> samplePositions) 
        throws Exception 
    {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(wavPath));
        byte[] audioBytes = audioInputStream.readAllBytes();
        audioInputStream.close();
        
        if (samplePositions.size() < 32) {
            throw new Exception("Position list too short to contain message length");
        }
        
        // extract bits from specified sample positions
        StringBuilder binary = new StringBuilder();
        for (int sampleIndex : samplePositions) {
            int byteIndex = sampleIndex * 2; // 16-bit samples = 2 bytes each
            if (byteIndex + 1 >= audioBytes.length) 
            { throw new Exception("Sample position out of bounds: " + sampleIndex); }
            
            // get the 16-bit sample (little-endian)
            byte[] sampleBytes = new byte[]{audioBytes[byteIndex], audioBytes[byteIndex + 1]};
            short sample = ByteBuffer.wrap(sampleBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
            
            // Extract LSB
            int bit = sample & 1;
            binary.append(bit);
        }
        
        long messageLength = getMessageLength(binary.toString());
        long requiredBits = 32 + messageLength * 8;
        if (binary.length() < requiredBits) {
            throw new Exception("Not enough bits extracted: " + binary.length() + " found, " + requiredBits + " needed");
        }
        
        return binaryToMessage(binary.toString());
    }
    
    private long getMessageLength(String binary) {
        String lengthBits = binary.substring(0, 32);
        return Long.parseLong(lengthBits, 2);
    }
    
    private String binaryToMessage(String binary) {
        long messageLength = getMessageLength(binary);
        StringBuilder message = new StringBuilder();
        
        for (int i = 32; i < 32 + (messageLength * 8); i += 8) {
            String charBits = binary.substring(i, i + 8);
            char c = (char) Integer.parseInt(charBits, 2);
            message.append(c);
        }
        return message.toString();
    }
}