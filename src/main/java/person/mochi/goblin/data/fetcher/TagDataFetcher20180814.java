package person.mochi.goblin.data.fetcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

public class TagDataFetcher20180814 extends BaseDataFetcher {

	private static final long serialVersionUID = -8049279699258553880L;
	private MongoDBPlayer playerArticles;
	private Results tagDataResults;

	public TagDataFetcher20180814() {
		try {
			MongoDBConfigWithAuth confArticlesClassificationDatabase = new MongoDBConfigWithAuth("127.0.0.1", 27017,
					"mochi", "gotohellmyevilex", null, null);
			playerArticles = new MongoDBPlayer(confArticlesClassificationDatabase, "RimMindTrainDatabase",
					"TagTrainData");
			tagDataResults = playerArticles.getData(null, true);
			this.totalExamples = (int) (tagDataResults.size());
			numOutcomes = 2;
		} catch (SelectedCollectionWithNoIndexesException e) {
			e.printStackTrace();
		} catch (NoServerIPException e) {
			e.printStackTrace();
		} catch (IllegalPortValueException e) {
			e.printStackTrace();
		}
	}

	private void generateFeatureVec(float[] featureVec, Result result) {
		int threshold = result.getInteger("threshold");
		int proportion = result.getInteger("proportion");
		int pos = result.getInteger("pos");
		int classification = result.getInteger("classification");
		int formerPos = result.getInteger("formerPos");
		int rearPos = result.getInteger("rearPos");
		int count = result.getInteger("count");
		List<Integer> featureList = new ArrayList<Integer>();
		featureList.add(threshold);
		featureList.add(proportion);
		featureList.add(pos);
		featureList.add(classification);
		featureList.add(formerPos);
		featureList.add(rearPos);
		featureList.add(count);
		for (int i = 0; i < 7; i++) {
			featureVec[i] = featureList.get(i);
		}
	}

	@Override
	public void fetch(int numExamples) {
		if (!hasMore()) {
			tagDataResults.close();
			throw new IllegalStateException("Unable to get more; there are no more images");
		}
		float[][] featureData = new float[numExamples][0];
		float[][] labelData = new float[numExamples][0];
		int actualExamples = 0;
		for (int i = 0; i < numExamples; i++, cursor++) {
			if (!hasMore()) {
				tagDataResults.close();
				break;
			}
			float[] featureVec = new float[7];
			featureData[actualExamples] = featureVec;
			labelData[actualExamples] = new float[numOutcomes];
			if (tagDataResults.hasNext()) {
				Result articlesResult = tagDataResults.next();
				boolean presetAnswer = articlesResult.getBoolean("answer");
				if (presetAnswer) {
					labelData[actualExamples][0] = 1.0f;
				} else {
					labelData[actualExamples][1] = 1.0f;
				}
				generateFeatureVec(featureVec, articlesResult);
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
		tagDataResults = playerArticles.getData(dual, true);
	}

}
