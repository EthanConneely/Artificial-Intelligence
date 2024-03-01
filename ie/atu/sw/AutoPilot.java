package ie.atu.sw;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;

import jhealy.aicme4j.NetworkBuilderFactory;
import jhealy.aicme4j.net.Activation;
import jhealy.aicme4j.net.Loss;
import jhealy.aicme4j.net.NeuralNetwork;
import jhealy.aicme4j.net.Output;

public class AutoPilot {

    public static final int DATA = 12;
    public static final int INPUTS = DATA + 1;

    public static final boolean MANUAL = false;

    static NeuralNetwork network;

    public static void Train() throws Exception {
        if (MANUAL) {
            return;
        }

        var trainingData = loadData();

        // Normalise or standardise the data

        network = NetworkBuilderFactory.getInstance()
                .newNetworkBuilder()
                .inputLayer("Input", INPUTS)
                .hiddenLayer("Hidden1", Activation.TANH, 6)
                .hiddenLayer("Hidden1", Activation.TANH, 8)
                .outputLayer("Output", Activation.TANH, 1)
                .train(trainingData.data(), trainingData.expected(), 0.001, 0.94, 10000, 0.0000001, Loss.MSE)
                // .save("./resources/autopilot.bin") // Saves the trained network
                .build(); // Builds, trains, saves and returns the network

        var lines = network.toString().split("\n");

        for (int i = 0; i < trainingData.data().length; i++) {
            var dir = Math.round(network.process(trainingData.data()[i], Output.NUMERIC) * 100.0) / 100.0;
            System.out.println(trainingData.expected()[i][0] + " -> " + dir);
        }

        System.out.println(lines[lines.length - 4]);
        System.out.println(lines[lines.length - 3]);
        System.out.println(lines[lines.length - 2]);
        System.out.println(lines[lines.length - 1]);

        // System.exit(0);
    }

    public static double Process(double[] inputs, int playerRow) throws Exception {
        var dir = network.process(inputs, Output.NUMERIC);
        // System.out.println(Math.round(dir * 100.0) / 100.0);
        return (dir - (playerRow / (double) GameView.MODEL_HEIGHT)) * 20;
    }

    public static void PreProcessModel(double[] output, int playerRow, LinkedList<byte[]> model) throws Exception {
        var index = 0;

        for (int i = GameView.PLAYER_COLUMN; i < GameView.PLAYER_COLUMN + AutoPilot.DATA; i++) {
            var col = model.get(i);
            int top = 0, bot = GameView.MODEL_HEIGHT;
            int y = 0;
            int prev = 0;
            // run length encoding the column
            for (byte b : col) {
                if (prev == 1 && b == 0) {
                    top = y - 1;
                }
                if (prev == 0 && b == 1) {
                    bot = y;
                }
                y++;
                prev = b;
            }
            output[index++] = Math.round((((top + bot) / 2 / (double) GameView.MODEL_HEIGHT)) * 1000.0) / 1000.0;
        }

        output[index++] = playerRow / (double) GameView.MODEL_HEIGHT;
    }

    static TrainingData loadData() throws Exception {
        var lines = Files.readAllLines(Path.of("./resources/training.csv"));

        var data = new double[lines.size()][INPUTS];
        var expected = new double[lines.size()][1];

        int index = 0;
        for (var line : lines) {
            var values = line.split(",");

            var inputs = Arrays.stream(values).mapToDouble(Double::parseDouble).toArray();

            for (int i = 0; i < inputs.length - 1; i++) {
                data[index][i] = inputs[i];
            }

            expected[index][0] = inputs[inputs.length - 1];

            index++;
        }

        return new TrainingData(data, expected);
    }
}

record TrainingData(double[][] data, double[][] expected) {
}
