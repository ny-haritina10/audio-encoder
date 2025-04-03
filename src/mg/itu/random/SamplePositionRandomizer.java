package mg.itu.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SamplePositionRandomizer {
    private final Random random;

    public SamplePositionRandomizer() {
        this.random = new Random();
    }

    public List<Integer> generateRandomPositions(int totalSamples, int requiredPositions) 
        throws Exception 
    {
        if (requiredPositions > totalSamples) {
            throw new Exception("Required positions (" + requiredPositions + ") exceed total samples (" + totalSamples + ")");
        }

        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < totalSamples; i++) 
        { positions.add(i); }

        shuffle(positions);

        // take the first requiredPosition' elements
        return positions.subList(0, requiredPositions);
    }

    // Fisher-Yates shuffle
    private void shuffle(List<Integer> list) {
        int n = list.size();
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = list.get(i);
            
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }
}