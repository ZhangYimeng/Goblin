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

import person.mochi.goblin.dataset.OmniDataSetIterator;
import person.mochi.goblin.dataset.SentenceDataSetFetcher;

public class LogicDetectionModel {

	private static Logger log = LoggerFactory.getLogger(LogicDetectionModel.class);

	public static void main(String[] args) throws Exception {
		int outputNum = 4; // number of output classes
		int batchSize = 358; // batch size for each epoch
		int rngSeed = 123; // random number seed for reproducibility
		int numEpochs = 5000; // number of epochs to perform
		double rate = 0.01; // learning rate

		DataSetIterator mnistTrain = new OmniDataSetIterator(batchSize, new SentenceDataSetFetcher());
		log.info("Build model....");
		MultiLayerConfiguration conf1 = new NeuralNetConfiguration.Builder().seed(rngSeed)
				// include a random seed for reproducibility
				.activation(Activation.RELU)
				.weightInit(WeightInit.XAVIER)
				.updater(new Nesterovs(rate, 0.98))
				.l2(rate * 0.005) // regularize learning model
				.list()
				.layer(0, new DenseLayer.Builder() // create the first input layer.
						.activation(Activation.SIGMOID)
						.nIn(200)
						.nOut(2000)
						.build())
				.layer(1, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.SIGMOID)
						.nIn(2000)
						.nOut(4000)
						.build())
				.layer(2, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.SIGMOID)
						.nIn(4000)
						.nOut(2000)
						.build())
				.layer(3, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.SIGMOID)
						.nIn(2000)
						.nOut(1000)
						.build())
				.layer(4, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.SIGMOID)
						.nIn(1000)
						.nOut(500)
						.build())
				.layer(5, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.SIGMOID)
						.nIn(500)
						.nOut(100)
						.build())
				.layer(6, new DenseLayer.Builder() // create the second hidden layer
						.activation(Activation.SIGMOID)
						.nIn(100)
						.nOut(10)
						.build())
				.layer(7, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD) // create outputLayer layer
						.activation(Activation.SOFTMAX)
						.nIn(10)
						.nOut(4)
						.build())
				.pretrain(false).backprop(true) // use backpropagation to adjust weights
				.build();
		MultiLayerNetwork model = new MultiLayerNetwork(conf1);
		System.out.println(model);
		model.init();
		model.setListeners(new ScoreIterationListener(1)); // print the score with every iteration

		log.info("Train model....");
		for (int i = 0; i < numEpochs; i++) {
			log.info("Epoch " + i);
			model.fit(mnistTrain);
		}

		log.info("Evaluate model....");
		DataSetIterator mnistTest = new OmniDataSetIterator(batchSize, new SentenceDataSetFetcher());
		Evaluation eval = new Evaluation(outputNum); // create an evaluation object with 10 possible classes
		while (mnistTest.hasNext()) {
			DataSet next = mnistTest.next();
			INDArray output = model.output(next.getFeatureMatrix()); // get the networks prediction
			eval.eval(next.getLabels(), output); // check the prediction against the true class
		}

		log.info(eval.stats());
		log.info("****************Example finished********************");

	}

}
