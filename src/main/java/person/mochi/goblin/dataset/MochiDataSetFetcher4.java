package person.mochi.goblin.dataset;

import java.util.Arrays;

import org.nd4j.linalg.api.memory.MemoryWorkspace;
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

public class MochiDataSetFetcher4 extends BaseDataFetcher {

	private static final long serialVersionUID = 5105159674306458714L;
	private MongoDBPlayer playerArticles;
	private Results articlesResults;

	public MochiDataSetFetcher4() {
		try {
			this.numOutcomes = 600;
			MongoDBConfigWithAuth confArticles = new MongoDBConfigWithAuth("127.0.0.1", 27017, "mochi",
					"gotohellmyevilex", new String[] { "articleName" }, null);
			playerArticles = new MongoDBPlayer(confArticles, "NGBPMF", "Articles");
			articlesResults = playerArticles.getData(null);
			this.totalExamples = (int) (articlesResults.size());
		} catch (SelectedCollectionWithNoIndexesException e) {
			e.printStackTrace();
		} catch (NoServerIPException e) {
			e.printStackTrace();
		} catch (IllegalPortValueException e) {
			e.printStackTrace();
		}
	}

	private void generateFeatureVec(float[] featureVec, Result result) {
		String articleContent = result.getString("articleContent");
		double criticalVal = result.getDouble("criticalVal");
		String[] temp = articleContent.split(",");
		String[] contentsArray = new String[temp.length - 1];
		System.arraycopy(articleContent.split(","), 1, contentsArray, 0, articleContent.split(",").length - 1);
		
		String[] integratedContentsArray = new String[16000];
		for (int i = 0; i < integratedContentsArray.length; i += contentsArray.length) {
			if (i + contentsArray.length < integratedContentsArray.length) {
				System.arraycopy(contentsArray, 0, integratedContentsArray, i, contentsArray.length);
			} else {
				System.arraycopy(contentsArray, 0, integratedContentsArray, i, integratedContentsArray.length - i);
			}
		}
		int featureVecIndex = 0;
		for (int i = 0; i < integratedContentsArray.length; i++) {
			if (i % 100 == 0) {
				featureVec[featureVecIndex] = (float) criticalVal;
				featureVecIndex++;
				featureVec[featureVecIndex] = Float.parseFloat(integratedContentsArray[i]);
				featureVecIndex++;
			} else {
				featureVec[featureVecIndex] = Float.parseFloat(integratedContentsArray[i]);
				featureVecIndex++;
			}
		}
	}

	@Override
	public void fetch(int numExamples) {
		if (!hasMore()) {
			articlesResults.close();
			throw new IllegalStateException("Unable to get more; there are no more images");
		}
		float[][] featureData = new float[numExamples][0];
		float[][] labelData = new float[numExamples][0];
		int actualExamples = 0;
		for (int i = 0; i < numExamples; i++, cursor++) {
			if (!hasMore()) {
				articlesResults.close();
				break;
			}
			float[] featureVec = new float[16160];
			featureData[actualExamples] = featureVec;
			labelData[actualExamples] = new float[numOutcomes];

			if (articlesResults.hasNext()) {
				Result articlesResult = articlesResults.next();
				int presetAnswer = articlesResult.getInteger("presetAnswer");
				if (presetAnswer == -1) {
					labelData[actualExamples][599] = 1.0f;
				} else {
					labelData[actualExamples][presetAnswer] = 1.0f;
				}
				generateFeatureVec(featureVec, articlesResult);
			}

//			for (int j = 0; j < bytesFeatureVec.length; j++) {
//				float v = ((int) bytesFeatureVec[j]) & 0xFF; // byte is loaded as signed -> convert to unsigned
//				featureVec[j] = v / 255.0f;
//			}
			actualExamples++;
		}

		if (actualExamples < numExamples) {
			featureData = Arrays.copyOfRange(featureData, 0, actualExamples);
			labelData = Arrays.copyOfRange(labelData, 0, actualExamples);
		}
		try (MemoryWorkspace scopedOut = Nd4j.getWorkspaceManager().scopeOutOfWorkspaces()) {
			INDArray features = Nd4j.create(featureData).detach();
			INDArray labels = Nd4j.create(labelData).detach();
			curr = new DataSet(features, labels);
		}

	}

	@Override
	public void reset() {
		cursor = 0;
		curr = null;
		articlesResults = playerArticles.getData(null);
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
	}

}
