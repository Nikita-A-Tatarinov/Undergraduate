package hse.fcs.se.visualization;

import org.pmml4s.model.Model;

import hse.fcs.se.useful.UsefulMethods;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class VisualizationModel {

    static class InDTrees {
        static final Model CAR = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "inD-dataset-v1.0" + File.separator + "car.pmml");

        static final Model TRUCK_BUS = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "inD-dataset-v1.0" + File.separator + "truck_bus.pmml");

        static final Model PEDESTRIAN = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "inD-dataset-v1.0" + File.separator + "pedestrian.pmml");

        static final Model BICYCLE = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "inD-dataset-v1.0" + File.separator + "bicycle.pmml");
    }

    static class RounDTrees {
        static final Model CAR = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "rounD-dataset-v1.0" + File.separator + "car.pmml");

        static final Model TRUCK = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "rounD-dataset-v1.0" + File.separator + "truck.pmml");

        static final Model VAN = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "rounD-dataset-v1.0" + File.separator + "van.pmml");

        static final Model BUS = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "rounD-dataset-v1.0" + File.separator + "bus.pmml");

        static final Model TRAILER = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "rounD-dataset-v1.0" + File.separator + "trailer.pmml");

        static final Model PEDESTRIAN = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "rounD-dataset-v1.0" + File.separator + "pedestrian.pmml");

        static final Model BICYCLE = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "rounD-dataset-v1.0" + File.separator + "bicycle.pmml");

        static final Model MOTORCYCLE = Model.fromFile("ML" + File.separator + "trees" + File.separator +
                "rounD-dataset-v1.0" + File.separator + "motorcycle.pmml");
    }

    String datasetName;
    String datasetFullName;
    int minFileNumber;
    int maxFileNumber;
    int fileNumber;
    String backgroundFileName;

    //List of tracks by frames.
    //tracks[i] - pieces of tracks in frame i.
    //tracks[i][j] - piece of track j in frame i.
    //tracks[i][j][k] - attribute k in piece of track j in frame i.
    ArrayList<ArrayList<ArrayList<String>>> tracksByFrames;
    //classes[trackId] = class
    HashMap<String, String> classes;
    ArrayList<ArrayList<Pair<String, String>>> predictions;

    int indexOfTrackId;
    int indexOfFrame;
    int indexOfX;
    int indexOfY;
    int indexOfXVelocity;
    int indexOfYVelocity;
    int indexOfXAcceleration;
    int indexOfYAcceleration;
    int indexOfHeading;
    int indexOfLength;
    int indexOfWidth;

    int indexOfTrackLifetime;
    int indexOfLonVelocity;
    int indexOfLatVelocity;
    int indexOfLonAcceleration;
    int indexOfLatAcceleration;

    int minFrame;
    int maxFrame;

    Double visualizationCoefficient;

    void refresh(String newDatasetName, int newFileNumber) throws IOException {
        if ("inD".equals(newDatasetName)) {
            if (newFileNumber < 0 || newFileNumber > 32) {
                throw new IllegalArgumentException("File number for inD dataset must be [0; 32] ");
            }
        } else if ("rounD".equals(newDatasetName)) {
            if (newFileNumber < 0 || newFileNumber > 23) {
                throw new IllegalArgumentException("File number for rounD dataset must be [0; 23] ");
            }
        } else {
            throw new IllegalArgumentException("Incorrect dataset name ");
        }
        initializeComponents(newDatasetName);
        refresh(newFileNumber);
    }

    void refresh(int newFileNumber) throws IOException {
        if (newFileNumber < minFileNumber || newFileNumber > maxFileNumber) {
            throw new IllegalArgumentException("File number for " + datasetName + " dataset must be [" +
                    minFileNumber + "; " + maxFileNumber + "] ");
        }
        fileNumber = newFileNumber;
        try (BufferedReader br = new BufferedReader(new FileReader("datasets" + File.separator +
                datasetFullName + File.separator + "data" + File.separator + fileNumber / 10 + "" +
                fileNumber % 10 + "_tracks.csv"))) {
            String line;
            ArrayList<String> splitLine;
            tracksByFrames = new ArrayList<>();
            br.readLine();
            while ((line = br.readLine()) != null) {
                splitLine = UsefulMethods.split(line, ",");
                if (Integer.parseInt(splitLine.get(indexOfFrame)) - minFrame >= tracksByFrames.size()) {
                    int size = tracksByFrames.size();
                    for (int i = size; i < Integer.parseInt(splitLine.get(indexOfFrame)) - minFrame + 1; i++) {
                        tracksByFrames.add(new ArrayList<>());
                    }
                }
                tracksByFrames.get(Integer.parseInt(splitLine.get(indexOfFrame)) - minFrame).add(splitLine);
            }
        }
        maxFrame = minFrame + tracksByFrames.size() - 1;

        try (BufferedReader br = new BufferedReader(new FileReader("datasets" + File.separator +
                datasetFullName + File.separator + "data" + File.separator + fileNumber / 10 + "" +
                fileNumber % 10 + "_tracksMeta.csv"))) {
            String line;
            ArrayList<String> splitLine;
            classes = new HashMap<>();
            br.readLine();
            while ((line = br.readLine()) != null) {
                splitLine = UsefulMethods.split(line, ",");
                classes.put(splitLine.get(1), splitLine.get(7));
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader("datasets" + File.separator +
                datasetFullName + File.separator + "data" + File.separator + fileNumber / 10 + "" +
                fileNumber % 10 + "_recordingMeta.csv"))) {
            br.readLine();
            ArrayList<String> splitLine = UsefulMethods.split(br.readLine(), ",");
            visualizationCoefficient = Double.parseDouble(splitLine.get(14));
        }

        try (BufferedReader br = new BufferedReader(new FileReader("ML" + File.separator +
                "0_tracks_prediction" + File.separator + datasetFullName + File.separator +
                "predictions" + File.separator + fileNumber / 10 + "" + fileNumber % 10 + "_tracks_predictions.csv"))) {
            String line;
            ArrayList<String> splitLine;
            predictions = new ArrayList<>();
            br.readLine();
            while ((line = br.readLine()) != null) {
                splitLine = UsefulMethods.split(line, ",");
                if (Integer.parseInt(splitLine.get(0)) - minFrame >= predictions.size()) {
                    int size = predictions.size();
                    for (int i = size; i < Integer.parseInt(splitLine.get(0)) - minFrame + 1; i++) {
                        predictions.add(new ArrayList<>());
                    }
                }
                predictions.get(Integer.parseInt(splitLine.get(0)) - minFrame).add(new Pair<>(splitLine.get(1), splitLine.get(2)));
            }
        }
    }

    private void initializeComponents(String newDatasetName) {
        if ("inD".equals(newDatasetName)) {
            datasetName = "inD";
            datasetFullName = "inD-dataset-v1.0";
            maxFileNumber = 32;
        } else {
            datasetName = "rounD";
            datasetFullName = "rounD-dataset-v1.0";
            maxFileNumber = 23;
        }
        minFileNumber = 0;
        backgroundFileName = "_background.png";

        indexOfTrackId = 1;
        indexOfFrame = 2;
        indexOfX = 4;
        indexOfY = 5;
        indexOfXVelocity = 9;
        indexOfYVelocity = 10;
        indexOfXAcceleration = 11;
        indexOfYAcceleration = 12;
        indexOfHeading = 6;
        indexOfLength = 8;
        indexOfWidth = 7;

        indexOfTrackLifetime = 3;
        indexOfLonVelocity = 13;
        indexOfLatVelocity = 14;
        indexOfLonAcceleration = 15;
        indexOfLatAcceleration = 16;

        minFrame = 0;
    }

    VisualizationModel() {
        predictedManoeuvre = new ArrayList<>();
        predictedManoeuvre.add("constant-speed");
        predictedManoeuvre.add("easy-turn-left");
        predictedManoeuvre.add("easy-turn-right");
        predictedManoeuvre.add("faster");
        predictedManoeuvre.add("slower");
        predictedManoeuvre.add("still");
        predictedManoeuvre.add("turn-left");
        predictedManoeuvre.add("turn-right");
    }

    ArrayList<String> predictedManoeuvre;

    String getPrediction(ArrayList<String> nearestNeighbours, String classOfVehicle) {
        Object[] neighbours = new Object[nearestNeighbours.size()];
        for (int i = 0; i < nearestNeighbours.size(); i++) {
            neighbours[i] = Double.parseDouble(nearestNeighbours.get(i));
        }
        Object[] result;
        if ("inD".equals(datasetName)) {
            if ("car".equals(classOfVehicle)) {
                result = InDTrees.CAR.predict(neighbours);
            } else if ("truck_bus".equals(classOfVehicle)) {
                result = InDTrees.TRUCK_BUS.predict(neighbours);
            } else if ("pedestrian".equals(classOfVehicle)) {
                result = InDTrees.PEDESTRIAN.predict(neighbours);
            } else {
                result = InDTrees.BICYCLE.predict(neighbours);
            }
        } else {
            if ("car".equals(classOfVehicle)) {
                result = RounDTrees.CAR.predict(neighbours);
            } else if ("truck".equals(classOfVehicle)) {
                result = RounDTrees.TRUCK.predict(neighbours);
            } else if ("van".equals(classOfVehicle)) {
                result = RounDTrees.VAN.predict(neighbours);
            } else if ("bus".equals(classOfVehicle)) {
                result = RounDTrees.BUS.predict(neighbours);
            } else if ("trailer".equals(classOfVehicle)) {
                result = RounDTrees.TRAILER.predict(neighbours);
            } else if ("pedestrian".equals(classOfVehicle)) {
                result = RounDTrees.PEDESTRIAN.predict(neighbours);
            } else if ("bicycle".equals(classOfVehicle)) {
                result = RounDTrees.BICYCLE.predict(neighbours);
            } else {
                result = RounDTrees.MOTORCYCLE.predict(neighbours);
            }
        }
        int indexOfMaxProbability = 0;
        for (int i = 1; i < result.length; i++) {
            if ((Double) result[i] > (Double) result[indexOfMaxProbability]) {
                indexOfMaxProbability = i;
            }
        }
        return predictedManoeuvre.get(indexOfMaxProbability);
    }

    ArrayList<String> getArrayOfNearestNeighbours(ArrayList<ArrayList<String>> tracksInFrame) {
        if (tracksInFrame == null || tracksInFrame.size() == 0) {
            throw new IllegalArgumentException("ArrayList of tracks must not be empty");
        }
        ArrayList<ArrayList<Pair<Integer, Double>>> distances = getDistances(tracksInFrame);
        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < tracksInFrame.size(); i++) {
            StringBuilder stringBuilder = new StringBuilder(tracksInFrame.get(i).get(indexOfX));
            stringBuilder.append(",").append(tracksInFrame.get(i).get(indexOfY)).append(",").append(tracksInFrame.get(i).get(indexOfHeading))
                    .append(",").append(tracksInFrame.get(i).get(indexOfWidth)).append(",").append(tracksInFrame.get(i).get(indexOfLength))
                    .append(",").append(tracksInFrame.get(i).get(indexOfXVelocity)).append(",").append(tracksInFrame.get(i).get(indexOfYVelocity))
                    .append(",").append(tracksInFrame.get(i).get(indexOfXAcceleration)).append(",").append(tracksInFrame.get(i).get(indexOfYAcceleration))
                    .append(",").append(tracksInFrame.get(i).get(indexOfLonVelocity)).append(",").append(tracksInFrame.get(i).get(indexOfLatVelocity))
                    .append(",").append(tracksInFrame.get(i).get(indexOfLonAcceleration)).append(",").append(tracksInFrame.get(i).get(indexOfLatAcceleration));
            for (int j = 0; j < Math.min(3, tracksInFrame.size() - 1); j++) {
                stringBuilder.append(",").append(distances.get(i).get(j).getValue())
                        .append(",").append(tracksInFrame.get(distances.get(i).get(j).getKey()).get(indexOfYVelocity))
                        .append(",").append(tracksInFrame.get(distances.get(i).get(j).getKey()).get(indexOfXVelocity))
                        .append(",").append(tracksInFrame.get(distances.get(i).get(j).getKey()).get(indexOfYAcceleration))
                        .append(",").append(tracksInFrame.get(distances.get(i).get(j).getKey()).get(indexOfXAcceleration))
                        .append(",").append(tracksInFrame.get(distances.get(i).get(j).getKey()).get(indexOfX))
                        .append(",").append(tracksInFrame.get(distances.get(i).get(j).getKey()).get(indexOfY));
            }
            stringBuilder.append(",999999".repeat(7).repeat(Math.max(0, 4 - tracksInFrame.size())));
            result.add(stringBuilder.toString());
        }
        return result;
    }

    private ArrayList<ArrayList<Pair<Integer, Double>>> getDistances(ArrayList<ArrayList<String>> tracksInFrame) {
        ArrayList<ArrayList<Pair<Integer, Double>>> distances = new ArrayList<>();
        for (int i = 0; i < tracksInFrame.size(); i++) {
            distances.add(new ArrayList<>());
            for (int j = 0; j < tracksInFrame.size(); j++) {
                if (i != j) {
                    distances.get(i).add(new Pair<>(j, distance(
                            Double.parseDouble(tracksInFrame.get(i).get(indexOfX)),
                            Double.parseDouble(tracksInFrame.get(i).get(indexOfY)),
                            Double.parseDouble(tracksInFrame.get(j).get(indexOfX)),
                            Double.parseDouble(tracksInFrame.get(j).get(indexOfY))
                    )));
                }
            }
            distances.get(i).sort(Comparator.comparing(Pair::getValue));
        }
        return distances;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
}
