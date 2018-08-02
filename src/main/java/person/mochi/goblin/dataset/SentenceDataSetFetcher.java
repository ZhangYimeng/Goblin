package person.mochi.goblin.dataset;

import java.util.Arrays;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher;
import org.nd4j.linalg.factory.Nd4j;

import com.wonder.mongodb.api.MongoDBConfigWithAuth;
import com.wonder.mongodb.api.MongoDBPlayer;
import com.wonder.mongodb.api.Result;
import com.wonder.mongodb.api.Results;
import com.wonder.mongodb.api.exception.IllegalPortValueException;
import com.wonder.mongodb.api.exception.NoServerIPException;
import com.wonder.mongodb.api.exception.SelectedCollectionWithNoIndexesException;

import mochi.tool.data.interconversion.DataInterconversionTool;

public class SentenceDataSetFetcher extends BaseDataFetcher {

	private static final long serialVersionUID = 5105159674306458714L;
	private MongoDBPlayer playerSentences;
	private Results sentenceResults;

	public SentenceDataSetFetcher() {
		try {
			this.numOutcomes = 4;
			MongoDBConfigWithAuth confSentences = new MongoDBConfigWithAuth("127.0.0.1", 27017, "mochi",
					"gotohellmyevilex", null, null);
			playerSentences = new MongoDBPlayer(confSentences, "NGBPMFSD", "Blended");
			sentenceResults = playerSentences.getData(null, true);
			this.totalExamples = (int) (sentenceResults.size());
		} catch (SelectedCollectionWithNoIndexesException e) {
			e.printStackTrace();
		} catch (NoServerIPException e) {
			e.printStackTrace();
		} catch (IllegalPortValueException e) {
			e.printStackTrace();
		}
	}

	private void generateFeatureVec(float[] featureVec, Result result) {
		String sentenceContent = result.getString("sentence");
		int sentenceLength = sentenceContent.length();
		for (int i = 0; i < sentenceLength; i++) {
			featureVec[i] = sentenceContent.charAt(i);
		}
	}

	@Override
	public void fetch(int numExamples) {
		if (!hasMore()) {
			sentenceResults.close();
			throw new IllegalStateException("Unable to get more; there are no more images");
		}
		float[][] featureData = new float[numExamples][0];
		float[][] labelData = new float[numExamples][0];
		int actualExamples = 0;
		for (int i = 0; i < numExamples; i++, cursor++) {
			if (!hasMore()) {
				sentenceResults.close();
				break;
			}
			float[] featureVec = new float[200];
			featureData[actualExamples] = featureVec;
			labelData[actualExamples] = new float[numOutcomes];

			if (sentenceResults.hasNext()) {
				Result articlesResult = sentenceResults.next();
				int presetAnswer = articlesResult.getInteger("tag");
				switch (presetAnswer) {
				case 0:
					labelData[actualExamples][presetAnswer] = 1f;
					break;
				case 1:
					labelData[actualExamples][presetAnswer] = 1f;
					break;
				case 2:
					// do nothing
					break;
				case 3:
					labelData[actualExamples][presetAnswer - 1] = 1f;
					break;
				case 4:
					labelData[actualExamples][presetAnswer - 1] = 1f;
					break;
				}
				generateFeatureVec(featureVec, articlesResult);
			}

			// for (int j = 0; j < featureVec.length; j++) {
			// featureVec[j] = featureVec[j] / 255.0f;
			// }
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
		sentenceResults = playerSentences.getData(null, true);
	}

	public static void main(String[] args) {
		String[] contentsArray = "1,2,3,4,5,6".split(",");
		String[] integratedContentsArray = new String[13];
		for (int i = 0; i < integratedContentsArray.length; i += contentsArray.length) {
			if (i + contentsArray.length < integratedContentsArray.length) {
				System.arraycopy(contentsArray, 0, integratedContentsArray, i, contentsArray.length);
			} else {
				System.arraycopy(contentsArray, 0, integratedContentsArray, i, integratedContentsArray.length - i);
			}
		}
		for (String s : integratedContentsArray) {
			System.out.print(s + " ");
		}
		System.out.println();
		for (int i = 0; i < 1000; i++) {
			if (i % 100 == 0) {
				System.out.println("-");
				System.out.print(i + " ");
			} else {
				System.out.print(i + " ");
			}
		}
		System.out.println(DataInterconversionTool.doubleToBytes(0.9999999999).length);
		System.out.println(",1,2,3,4,".split(",").length);
		String s = "safsadf你好sdf， ,，,，,";
		int length = s.length();
		System.out.println(length);
		for (int i = 0; i < length; i++) {
			System.out.println((float) s.charAt(i));
		}
		float[] featureVec = new float[200];
		for(float f: featureVec) {
			System.out.print(f);
		}
	}

}