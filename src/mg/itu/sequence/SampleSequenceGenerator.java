package mg.itu.sequence;

import java.util.ArrayList;
import java.util.List;

public class SampleSequenceGenerator {
    
    private final int sequenceStep;
    
    public SampleSequenceGenerator(int sequenceStep) {
        this.sequenceStep = sequenceStep;
    }
    
    public List<Integer> generatePositionsFromSequence(int totalSamples, int requiredPositions) 
        throws Exception 
    {
        if (requiredPositions > totalSamples) {
            throw new Exception("Required positions exceed total samples");
        }
        
        List<Integer> positions = new ArrayList<>();
        int currentPosition = 0; // Starting position
        
        for (int i = 0; i < requiredPositions; i++) {
            positions.add(currentPosition);
            
            // Apply the sequence formula to get the next position
            currentPosition = applySequenceFormula(currentPosition, totalSamples);
        }
        
        return positions;
    }
    
    /**
     * Applies the mathematical sequence formula to get the next sample position
     * @param currentPosition The current sample position
     * @param totalSamples The total number of samples in the WAV file
     * @return The next sample position according to the sequence formula
     */
    protected int applySequenceFormula(int currentPosition, int totalSamples) {
        // Current implementation: add sequenceStep to position (n + step)
        return (currentPosition + sequenceStep) % totalSamples;
        
        // Alternative formulas could be used, e.g.:
        // return (currentPosition * currentPosition + 3) % totalSamples; // n² + 3
    }
}