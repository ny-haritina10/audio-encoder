package mg.itu.encoder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import mg.itu.random.SamplePositionRandomizer;

public class WavPositionBasedEncoder {
    
    public List<Integer> encode(String inputWavPath, String message, String outputWavPath) throws Exception {
        // Read the input WAV file
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(inputWavPath));
        AudioFormat format = audioInputStream.getFormat();
        
        // Validate format (must be PCM, 16-bit samples)
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() != 16) {
            throw new Exception("Only 16-bit PCM WAV files are supported");
        }
        
        // Read all samples into a byte array
        byte[] audioBytes = audioInputStream.readAllBytes();
        audioInputStream.close();
        
        // Convert message to binary
        String binaryMessage = messageToBinary(message);
        int requiredBits = binaryMessage.length();
        
        // Calculate total number of samples
        int numSamples = audioBytes.length / (format.getSampleSizeInBits() / 8);
        
        // Generate random sample positions
        SamplePositionRandomizer randomizer = new SamplePositionRandomizer();
        List<Integer> samplePositions = randomizer.generateRandomPositions(numSamples, requiredBits);
        
        // Modify samples at specified positions
        for (int i = 0; i < binaryMessage.length(); i++) {
            int sampleIndex = samplePositions.get(i);
            int byteIndex = sampleIndex * 2; // 16-bit samples = 2 bytes each
            
            // Get the 16-bit sample (little-endian)
            byte[] sampleBytes = new byte[]{audioBytes[byteIndex], audioBytes[byteIndex + 1]};
            short sample = ByteBuffer.wrap(sampleBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
            
            // Hide bit in LSB
            sample = (short) ((sample & 0xFFFE) | (binaryMessage.charAt(i) - '0'));
            
            // Write back the modified sample
            ByteBuffer.wrap(audioBytes, byteIndex, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(sample);
        }
        
        // Write the modified audio to a new WAV file
        writeWavFile(audioBytes, outputWavPath, format);
        
        // Return the sample positions for decoding
        return samplePositions;
    }
    
    private String messageToBinary(String message) {
        StringBuilder binary = new StringBuilder();
        String lengthBinary = String.format("%32s", Long.toBinaryString(message.length())).replace(' ', '0');
        binary.append(lengthBinary);
        
        for (char c : message.toCharArray()) {
            String charBinary = String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0');
            binary.append(charBinary);
        }
        return binary.toString();
    }
    
    private void writeWavFile(byte[] audioBytes, String outputPath, AudioFormat format) throws Exception {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(audioBytes);
        AudioInputStream outputStream = new AudioInputStream(byteStream, format, audioBytes.length / format.getFrameSize());
        AudioSystem.write(outputStream, AudioFileFormat.Type.WAVE, new File(outputPath));
        outputStream.close();
    }
}
