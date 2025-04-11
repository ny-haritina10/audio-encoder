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

import mg.itu.sequence.SampleSequenceGenerator;

public class WavPositionBasedEncoder {
    
    public List<Integer> encode(String inputWavPath, String message, String outputWavPath) 
        throws Exception 
    {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(inputWavPath));
        AudioFormat format = audioInputStream.getFormat();
        
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() != 16) {
            throw new Exception("Only 16-bit PCM WAV files are supported");
        }
        
        // Read samples into a byte array
        byte[] audioBytes = audioInputStream.readAllBytes();
        audioInputStream.close();
        
        String binaryMessage = messageToBinary(message);
        int requiredBits = binaryMessage.length();
        
        // Total number of samples
        int numSamples = audioBytes.length / (format.getSampleSizeInBits() / 8);
        
        // Use SampleSequenceGenerator instead of SamplePositionRandomizer
        SampleSequenceGenerator sequenceGen = new SampleSequenceGenerator(4); // Default step, can be parameterized
        List<Integer> samplePositions = sequenceGen.generatePositionsFromSequence(numSamples, requiredBits);
        
        // Encode using the sequence-based positions
        encodeWithPositions(audioBytes, binaryMessage, samplePositions, format, outputWavPath);
        
        return samplePositions;
    }
    
    public void encodeWithPositions(byte[] audioBytes, String binaryMessage, List<Integer> samplePositions, 
                                   AudioFormat format, String outputWavPath) 
        throws Exception 
    {
        if (samplePositions.size() < binaryMessage.length()) {
            throw new Exception("Not enough sample positions provided");
        }
        
        // Modify samples at specified positions
        for (int i = 0; i < binaryMessage.length(); i++) {
            int sampleIndex = samplePositions.get(i);
            int byteIndex = sampleIndex * 2; 
            
            if (byteIndex + 1 >= audioBytes.length) {
                throw new Exception("Sample position out of bounds: " + sampleIndex);
            }
            
            // Get the 16-bit sample (little-endian)
            byte[] sampleBytes = new byte[]{audioBytes[byteIndex], audioBytes[byteIndex + 1]};
            short sample = ByteBuffer.wrap(sampleBytes).order(ByteOrder.LITTLE_ENDIAN).getShort();
            
            // Hide bit in LSB
            sample = (short) ((sample & 0xFFFE) | (binaryMessage.charAt(i) - '0'));
            
            // Write back the modified sample
            ByteBuffer.wrap(audioBytes, byteIndex, 2).order(ByteOrder.LITTLE_ENDIAN).putShort(sample);
        }
        
        writeWavFile(audioBytes, outputWavPath, format);
    }
    
    public static String messageToBinary(String message) {
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