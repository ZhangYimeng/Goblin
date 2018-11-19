package person.mochi.goblin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import person.mochi.goblin.data.fetcher.TagDataFetcherForTest20180815;
import person.mochi.goblin.dataset.OmniDataSetIterator;

public class NetLoadTest {
	
	private static Logger log = LoggerFactory.getLogger(NetLoadTest.class);

	public static void main(String[] args) throws IOException {
		int outputNum = 2; // number of output classes
		int batchSize = 1024; // batch size for each epoch
		File locationToSave = new File("MyMultiLayerNetwork.zip");
		MultiLayerNetwork restored = ModelSerializer.restoreMultiLayerNetwork(locationToSave);
		log.info("Evaluate model....");
		DataSetIterator mnistTest = new OmniDataSetIterator(batchSize, new TagDataFetcherForTest20180815());
		Evaluation eval = new Evaluation(outputNum); // create an evaluation object with 10 possible classes
		while (mnistTest.hasNext()) {
			DataSet next = mnistTest.next();
			INDArray output = restored.output(next.getFeatureMatrix()); // get the networks prediction
			eval.eval(next.getLabels(), output); // check the prediction against the true class
			System.out.println(restored.predict(next));
		}
		
		log.info(eval.stats());
		log.info("****************Example finished********************");
		
		float[] featureVec = new float[] {1000, 50, 2, 4, 2, 3, 10};
        float[][] features = new float[1][];
        features[0] = featureVec;
        INDArray featuresArray = Nd4j.create(features);
        DataSet dataSet = new DataSet();
        dataSet.setFeatures(featuresArray);
        List<String> labelNames = new ArrayList<String>();
        labelNames.add("是关键词");
        labelNames.add("不是关键词");
        dataSet.setLabelNames(labelNames);
        System.out.println(restored.predict(dataSet));
	}

}
