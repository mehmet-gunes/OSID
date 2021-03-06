import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.rules.*;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.FastVector;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

@SuppressWarnings("deprecation")
public class ClassifyML {
    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }

    public static Evaluation classify(Classifier model, Instances trainingSet, Instances testingSet) throws Exception {
        Evaluation evaluation = new Evaluation(trainingSet);

        model.buildClassifier(trainingSet);
        evaluation.evaluateModel(model, testingSet);

        return evaluation;
    }

    @SuppressWarnings("rawtypes")
    public static double calculateAccuracy(FastVector predictions) {
        double correct = 0;

        for (int i = 0; i < predictions.size(); i++) {
            NominalPrediction np = (NominalPrediction) predictions.elementAt(i);
            if (np.predicted() == np.actual())
                correct++;
        }

        return 100 * correct / predictions.size();
    }

    public static Instances removeFeatures(Instances inst, String indices) {
        Instances newData = null;
        try {
            Remove remove = new Remove();                         // new instance of filter
            remove.setAttributeIndices(indices);                           // set options
            remove.setInputFormat(inst);                          // inform filter about dataset **AFTER** setting options
            newData = weka.filters.Filter.useFilter(inst, remove);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newData;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public double train_test(String trainfile_link, String testfile_link, String indices, int classifier, boolean verbose) throws Exception {
        Classifier[] models = {
                new J48(), // a decision tree
                new PART(),
                new DecisionTable(), // decision table majority classifier
                new DecisionStump(), // one-level decision tree
                new ZeroR(),
                new OneR(), // one-rule classifier
                new MultilayerPerceptron(), // neural network
                new RandomForest(),
                new SMO(),
                new JRip(),
                new Logistic(),
                new LinearRegression(),
                new BayesNet()
        };

        BufferedReader trainfile = readDataFile(trainfile_link);
        BufferedReader testfile = readDataFile(testfile_link);

        Instances traindata = new Instances(trainfile);
        Instances testdata = new Instances(testfile);

        if (!indices.equals("")) {
            traindata = removeFeatures(traindata, indices);
            testdata = removeFeatures(testdata, indices);
        }

        if ((traindata == null) || (testdata == null)) {
            System.out.println("ErRoR!");
            System.exit(0);
        }

        traindata.setClassIndex(traindata.numAttributes() - 1);
        testdata.setClassIndex(testdata.numAttributes() - 1);

        // Collect every group of predictions for current model in a FastVector
        FastVector predictions = new FastVector();

        // For each training-testing split pair, train and test the classifier
        Evaluation validation = classify(models[classifier], traindata, testdata);
        predictions.appendElements(validation.predictions());

        // Uncomment to see the summary for each training-testing pair.
        if (verbose) {
            System.out.println(models[classifier].toString());
            System.out.println(validation.toSummaryString());
            System.out.println(validation.toMatrixString());
            System.out.println("fmeasure: " + (validation.weightedFMeasure() * 100));
            System.out.println();

            for (int i = 0; i < traindata.numClasses(); i++) {
                System.out.print("class: " + i);
                System.out.print(" tp: " + validation.numTruePositives(i));
                System.out.print(" tn: " + validation.numTrueNegatives(i));
                System.out.print(" fp: " + validation.numFalsePositives(i));
                System.out.print(" fn: " + validation.numFalseNegatives(i));
                System.out.println();
            }
        }

        // Return overall accuracy of current classifier on all splits
        return calculateAccuracy(predictions);
//        return (validation.weightedFMeasure() * 100);
    }
}
