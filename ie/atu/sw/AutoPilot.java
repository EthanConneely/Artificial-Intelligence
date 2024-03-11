package ie.atu.sw;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;

import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.EncogDirectoryPersistence;

public class AutoPilot {
    private static final int MaxEpochs = 1000;
    public static final int INPUTS = 2;
    public static final int OUTPUTS = 1;
    public static final boolean MANUAL = false;

    public static BasicNetwork network = new BasicNetwork();

    public static void Train() {
        if (MANUAL) {
            return;
        }

        // Create the neural network
        network.addLayer(new BasicLayer(null, true, INPUTS));
        network.addLayer(new BasicLayer(new ActivationTANH(), true, 4));
        network.addLayer(new BasicLayer(new ActivationTANH(), false, OUTPUTS));
        network.getStructure().finalizeStructure();
        network.reset();

        // Load training data
        MLDataSet trainingSet = new BasicMLDataSet();
        try {
            trainingSet = loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        long startTime = System.nanoTime();

        // Train the neural network
        ResilientPropagation train = new ResilientPropagation(network, trainingSet);
        double minError = 0.001;
        int epoch = 0;
        do {
            train.iteration();
            System.out.println("Epoch: " + epoch + " Error: " + train.getError());
            epoch++;
        } while (train.getError() > minError && epoch < MaxEpochs);
        train.finishTraining();

        long duration = (System.nanoTime() - startTime);

        System.out.println("Training took " + (duration / 1_000_000_000.0) + " milliseconds");

        EncogDirectoryPersistence.saveObject(new File("./resources/autopilot.model"), network);
    }

    public static double Process(double[] inputs) throws Exception {
        var output = new double[1];
        network.compute(inputs, output);
        return output[0];
    }

    public static double[] PreProcessInputs(int playerRow, LinkedList<byte[]> model) {
        var output = new double[INPUTS];
        var index = 0;

        // Get the top and bottom of where the cavern is infront of the player
        var col = model.get(GameView.PLAYER_COLUMN + 1);
        int top = 0, bot = GameView.MODEL_HEIGHT;
        int y = 0;
        int prev = 0;
        for (byte b : col) {
            if (prev == 1 && b == 0) {
                top = y;
            }
            if (prev == 0 && b == 1) {
                bot = y - 1;
            }
            y++;
            prev = b;
        }

        output[index++] = normalize(playerRow - top);
        output[index++] = normalize(bot - playerRow);

        // System.out.println(output[0] + " " + output[1]);

        return output;
    }

    static double normalize(double value) {
        return (value / 14);
    }

    static MLDataSet loadData() throws Exception {
        var lines = Files.readAllLines(Path.of("./resources/training.csv"));

        var data = new double[lines.size()][INPUTS];
        var expected = new double[lines.size()][1];

        int index = 0;
        for (var line : lines) {
            var values = line.split(",");

            var inputs = Arrays.stream(values).mapToDouble(Double::parseDouble).toArray();

            for (int i = 0; i < inputs.length - 1; i++) {
                data[index][i] = normalize(inputs[i]);
            }

            expected[index][0] = inputs[inputs.length - 1];

            index++;
        }

        return new BasicMLDataSet(data, expected);
    }
}
