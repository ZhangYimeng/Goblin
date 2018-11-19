package person.mochi.goblin;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import person.mochi.goblin.data.fetcher.TagDataFetcher20180814;
import person.mochi.goblin.data.fetcher.TagDataFetcherForTest20180815;
import person.mochi.goblin.dataset.OmniDataSetIterator;

public class NewFormTrainingModel20180814 {
	private static Logger log = LoggerFactory.getLogger(ArticleClassification.class);

	public static void main(String[] args) throws Exception {
		int outputNum = 2; // number of output classes
		int batchSize = 93633; // batch size for each epoch
		int rngSeed = 123; // random number seed for reproducibility
		int numEpochs = 6000; // number of epochs to perform
		double rate = 0.001; // learning rate
		
		DataSetIterator mnistTrain = new OmniDataSetIterator(batchSize, new TagDataFetcher20180814());
		log.info("Build model....");
		MultiLayerConfiguration conf1 = new NeuralNetConfiguration.Builder().seed(rngSeed)
				// include a random seed for reproducibility
				.activation(Activation.LEAKYRELU)
				.weightInit(WeightInit.XAVIER)
				.updater(new Nesterovs(rate, 0.98))
				.l2(rate * 0.005) // regularize learning model
				.list()
				.layer(0, new DenseLayer.Builder() // create the first input layer.
						.activation(Activation.LEAKYRELU)
						.nIn(7)
						.nOut(12)
						.build())
				.layer(1, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.LEAKYRELU)
						.nIn(12)
						.nOut(24)
						.build())
				.layer(2, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.LEAKYRELU)
						.nIn(24)
						.nOut(48)
						.build())
				.layer(3, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.LEAKYRELU)
						.nIn(48)
						.nOut(48)
						.build())
				.layer(4, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.LEAKYRELU)
						.nIn(48)
						.nOut(24)
						.build())
				.layer(5, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.LEAKYRELU)
						.nIn(24)
						.nOut(24)
						.build())
				.layer(6, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.LEAKYRELU)
						.nIn(24)
						.nOut(12)
						.build())
				.layer(7, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.LEAKYRELU)
						.nIn(12)
						.nOut(6)
						.build())
				.layer(8, new OutputLayer.Builder(LossFunction.MEAN_SQUARED_LOGARITHMIC_ERROR) // create outputLayer layer
						.activation(Activation.SOFTMAX)
						.nIn(6)
						.nOut(2)
						.build())
				.pretrain(false).backprop(true) // use backpropagation to adjust weights
				.build();
		MultiLayerNetwork model = new MultiLayerNetwork(conf1);
		model.init();
		model.setListeners(new ScoreIterationListener(1)); // print the score with every iteration

		log.info("Train model....");
		for (int i = 0; i < numEpochs; i++) {
			log.info("Epoch " + i);
			model.fit(mnistTrain);
		}

//		File locationToSave = new File("MyMultiLayerNetwork.zip");      //Where to save the network. Note: the file is in .zip format - can be opened externally
//      boolean saveUpdater = true;                                             //Updater: i.e., the state for Momentum, RMSProp, Adagrad etc. Save this if you want to train your network more in the future
//      ModelSerializer.writeModel(model, locationToSave, saveUpdater);
		log.info("Evaluate model....");
		DataSetIterator mnistTest = new OmniDataSetIterator(batchSize, new TagDataFetcherForTest20180815());
		
		Evaluation eval = new Evaluation(outputNum); // create an evaluation object with 10 possible classes
		while (mnistTest.hasNext()) {
			DataSet next = mnistTest.next();
			INDArray output = model.output(next.getFeatureMatrix()); // get the networks prediction
			eval.eval(next.getLabels(), output); // check the prediction against the true class
		}

		log.info(eval.stats());
		System.out.println(eval.f1());
		log.info("****************Example finished********************");

	}
}
