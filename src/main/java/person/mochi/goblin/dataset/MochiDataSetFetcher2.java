package person.mochi.goblin.dataset;

import java.util.Arrays;
import java.util.Random;

import org.bson.types.ObjectId;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher;
import org.nd4j.linalg.factory.Nd4j;

import com.wonder.mongodb.api.Duality;
import com.wonder.mongodb.api.MongoDBConfigWithAuth;
import com.wonder.mongodb.api.MongoDBPlayer;
import com.wonder.mongodb.api.Result;
import com.wonder.mongodb.api.Results;
import com.wonder.mongodb.api.exception.IllegalPortValueException;
import com.wonder.mongodb.api.exception.NoServerIPException;
import com.wonder.mongodb.api.exception.SelectedCollectionWithNoIndexesException;

import mochi.tool.data.interconversion.DataInterconversionTool;

public class MochiDataSetFetcher2 extends BaseDataFetcher {

	private static final long serialVersionUID = 5105159674306458714L;
	private MongoDBPlayer playerArticles;
	private MongoDBPlayer playerPositives;
	private MongoDBPlayer playerNegatives;
	private Results positiveResults;
	private Results negativeResults;
	private int negativeResultsNumber;
	private int positiveResultsNumber;
	private Random random;
	private boolean switchFlag;

	public MochiDataSetFetcher2() {
		try {
			random = new Random();
			switchFlag = false;
			this.numOutcomes = 2;
			MongoDBConfigWithAuth confArticles = new MongoDBConfigWithAuth("127.0.0.1", 27017, "mochi",
					"gotohellmyevilex", new String[] { "article" }, null);
			MongoDBConfigWithAuth confPositives = new MongoDBConfigWithAuth("127.0.0.1", 27017, "mochi",
					"gotohellmyevilex", new String[] { "data", "articleId" }, null);
			MongoDBConfigWithAuth confNegatives = new MongoDBConfigWithAuth("127.0.0.1", 27017, "mochi",
					"gotohellmyevilex", new String[] { "data", "articleId" }, null);
			playerArticles = new MongoDBPlayer(confArticles, "Yihan", "Articles");
			playerPositives = new MongoDBPlayer(confPositives, "Yihan", "Positives");
			playerNegatives = new MongoDBPlayer(confNegatives, "Yihan", "Negatives");
			Duality dual = new Duality();
			positiveResults = playerPositives.getData(dual);
			negativeResults = playerNegatives.getData(dual);
			negativeResultsNumber = (int) negativeResults.size();
			positiveResultsNumber = (int) positiveResults.size();
			this.totalExamples = negativeResultsNumber + positiveResultsNumber;
			positiveResults.close();
			negativeResults.close();
			positiveResults = playerPositives.getData(dual);
			negativeResults = playerNegatives.getData(dual);
		} catch (SelectedCollectionWithNoIndexesException e) {
			e.printStackTrace();
		} catch (NoServerIPException e) {
			e.printStackTrace();
		} catch (IllegalPortValueException e) {
			e.printStackTrace();
		}
	}

	private void generateFeatureVec(byte[] bytesFeatureVec, Result result) {
		String articleId = result.getString("articleId");
		String keyWord = result.getString("data");
		Duality dual = new Duality();
		dual.append("_id", new ObjectId(articleId));
		Results articleResults = playerArticles.getData(dual);
		String article = articleResults.next().getString("article");
		byte[] keyWordBytes = DataInterconversionTool.stringToBytes(keyWord);
		System.arraycopy(keyWordBytes, 0, bytesFeatureVec, 0, keyWordBytes.length);
		byte[] articleBytes = DataInterconversionTool.stringToBytes(article);
		System.arraycopy(articleBytes, 0, bytesFeatureVec, 75, articleBytes.length);
		articleResults.close();
	}

	@Override
	public void fetch(int numExamples) {
		if (!hasMore()) {
			throw new IllegalStateException("Unable to get more; there are no more images");
		}
		float[][] featureData = new float[numExamples][0];
		float[][] labelData = new float[numExamples][0];
		int actualExamples = 0;
		for (int i = 0; i < numExamples; i++, cursor++) {
			if (!hasMore()) {
				break;
			}
			byte[] bytesFeatureVec = new byte[51275];
			float[] featureVec = new float[51275];
			featureData[actualExamples] = featureVec;
			labelData[actualExamples] = new float[numOutcomes];

			if (switchFlag) {
				Result negativeResult = playerNegatives.getData(new Duality(), random.nextInt(negativeResultsNumber), 1)
						.next();
//				System.out.println(negativeResult.get("data").toString());
				labelData[actualExamples][0] = 1.0f;
				generateFeatureVec(bytesFeatureVec, negativeResult);
				switchFlag = !switchFlag;
			} else {
				Result positiveResult = playerPositives.getData(new Duality(), random.nextInt(positiveResultsNumber), 1)
						.next();
//				System.out.println(positiveResult.get("data").toString());
				labelData[actualExamples][1] = 1.0f;
				generateFeatureVec(bytesFeatureVec, positiveResult);
				switchFlag = !switchFlag;
			}

			// if (negativeExamplesIndex < negativeExamples && negativeResults.hasNext()) {
			// Result negativeResult = negativeResults.next();
			// labelData[actualExamples][0] = 1.0f;
			// generateFeatureVec(bytesFeatureVec, negativeResult);
			// negativeExamplesIndex++;
			// } else if (positiveExamplesIndex < positiveExamples &&
			// positiveResults.hasNext()) {
			// Result positiveResult = positiveResults.next();
			// labelData[actualExamples][1] = 1.0f;
			// generateFeatureVec(bytesFeatureVec, positiveResult);
			// positiveExamplesIndex++;
			// } else if (negativeResults.hasNext()) {
			// Result negativeResult = negativeResults.next();
			// labelData[actualExamples][0] = 1.0f;
			// generateFeatureVec(bytesFeatureVec, negativeResult);
			// } else if (positiveResults.hasNext()) {
			// Result positiveResult = positiveResults.next();
			// labelData[actualExamples][1] = 1.0f;
			// generateFeatureVec(bytesFeatureVec, positiveResult);
			// }

			for (int j = 0; j < bytesFeatureVec.length; j++) {
				float v = ((int) bytesFeatureVec[j]) & 0xFF; // byte is loaded as signed -> convert to unsigned
				featureVec[j] = v / 255.0f;
			}

			actualExamples++;
		}

		if (actualExamples < numExamples) {
			featureData = Arrays.copyOfRange(featureData, 0, actualExamples);
			labelData = Arrays.copyOfRange(labelData, 0, actualExamples);
		}

		INDArray features = Nd4j.create(featureData);
		INDArray labels = Nd4j.create(labelData);
		curr = new DataSet(features, labels);

	}

	@Override
	public void reset() {
		cursor = 0;
		curr = null;
		Duality dual = new Duality();
		positiveResults = playerPositives.getData(dual);
		negativeResults = playerNegatives.getData(dual);
	}

	public static void main(String[] args)
			throws NoServerIPException, IllegalPortValueException, SelectedCollectionWithNoIndexesException {
		// int numExamples = 65;
		// double ratio = 0.00001;
		// System.out.println(numExamples * ratio);
		// int positiveExamples = (int) (numExamples * ratio > 1? numExamples * ratio:
		// 1);
		// System.out.println(positiveExamples);
		// MongoDBConfigWithAuth confArticles = new MongoDBConfigWithAuth("127.0.0.1",
		// 27017, "mochi",
		// "gotohellmyevilex", new String[] { "article" }, null);
		// MongoDBConfigWithAuth confPositives = new MongoDBConfigWithAuth("127.0.0.1",
		// 27017, "mochi",
		// "gotohellmyevilex", new String[] { "data", "articleId" }, null);
		// MongoDBConfigWithAuth confNegatives = new MongoDBConfigWithAuth("127.0.0.1",
		// 27017, "mochi",
		// "gotohellmyevilex", new String[] { "data", "articleId" }, null);
		// MongoDBPlayer playerArticles = new MongoDBPlayer(confArticles, "Yihan",
		// "Articles");
		// MongoDBPlayer playerPositives = new MongoDBPlayer(confPositives, "Yihan",
		// "Positives");
		// MongoDBPlayer playerNegatives = new MongoDBPlayer(confNegatives, "Yihan",
		// "Negatives");
		// Duality dual = new Duality();
		// dual.append("_id", new ObjectId("5adf440daf8d573c5e31bcb2"));
		// Results results = playerArticles.getData(dual);
		// System.out.println(results.next());

		byte[] keyWordBytes = DataInterconversionTool.stringToBytes("张伊萌");
		byte[] keyWordBytesPart = new byte[75];
		System.arraycopy(keyWordBytes, 0, keyWordBytesPart, 0, keyWordBytes.length);
		for (byte b : keyWordBytesPart) {
			System.out.print(b + " ");
		}
		System.out.println();
		byte[] bytes = { -27, -68, -96, -28, -68, -118, -24, -112, -116 };
		for (int j = 0; j < bytes.length; j++) {
			float v = ((int) bytes[j]) & 0xFF; // byte is loaded as signed -> convert to unsigned
			System.out.print(v + " ");
		}
	}

}
