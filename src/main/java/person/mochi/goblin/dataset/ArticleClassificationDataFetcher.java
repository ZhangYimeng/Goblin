package person.mochi.goblin.dataset;

import java.util.Arrays;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher;
import org.nd4j.linalg.factory.Nd4j;

import com.wonder.mongodb.api.Duality;
import com.wonder.mongodb.api.MongoDBConfigWithAuth;
import com.wonder.mongodb.api.MongoDBPlayer;
import com.wonder.mongodb.api.Result;
import com.wonder.mongodb.api.Results;
import com.wonder.mongodb.api.exception.DBFindDistinctValueLackFiledException;
import com.wonder.mongodb.api.exception.IllegalPortValueException;
import com.wonder.mongodb.api.exception.NoServerIPException;
import com.wonder.mongodb.api.exception.SelectedCollectionWithNoIndexesException;

import mochi.tool.data.interconversion.DataInterconversionTool;

public class ArticleClassificationDataFetcher extends BaseDataFetcher {

	private static final long serialVersionUID = -8049279699258553880L;
	private MongoDBPlayer playerArticles;
	private Results articlesResults;
	
	public ArticleClassificationDataFetcher() {
		try {
			MongoDBConfigWithAuth confArticlesClassificationDatabase = new MongoDBConfigWithAuth("127.0.0.1", 27017, "mochi",
					"gotohellmyevilex", null, null);
			playerArticles = new MongoDBPlayer(confArticlesClassificationDatabase, "RimMindTrainDatabase", "CoreTrainData");
			articlesResults = playerArticles.getData(null, true);
			articlesResults.sort("tags", 1);
			this.totalExamples = (int) (articlesResults.size());
			numOutcomes = (int) playerArticles.advancedDistinct("area", null, String.class).size();
		} catch (SelectedCollectionWithNoIndexesException e) {
			e.printStackTrace();
		} catch (NoServerIPException e) {
			e.printStackTrace();
		} catch (IllegalPortValueException e) {
			e.printStackTrace();
		} catch (DBFindDistinctValueLackFiledException e) {
			e.printStackTrace();
		}
	}
	
	private void generateFeatureVec(float[] featureVec, Result result) {
		String articleContent = result.getString("body");
		String[] strArray = articleContent.split("");
		String[] integratedContentsArray = new String[featureVec.length];
		int featureLength = featureVec.length;
		for (int i = 0; i < integratedContentsArray.length;) {
			if (strArray.length < featureLength) {
				System.arraycopy(strArray, 0, integratedContentsArray, i, strArray.length);
				i += strArray.length;
				featureLength -= strArray.length;
			} else {
				System.arraycopy(strArray, 0, integratedContentsArray, i, featureLength);
				i += strArray.length;
			}
		}
		for (int i = 0; i < integratedContentsArray.length; i++) {
			featureVec[i] = DataInterconversionTool.bytesToInt(integratedContentsArray[i].getBytes());
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
			float[] featureVec = new float[16000];
			featureData[actualExamples] = featureVec;
			labelData[actualExamples] = new float[numOutcomes];
			if (articlesResults.hasNext()) {
				Result articlesResult = articlesResults.next();
				String presetAnswer = articlesResult.getString("area");
				switch(presetAnswer) {
				case "国内":
					labelData[actualExamples][0] = 1.0f;
					break;
				case "国际":
					labelData[actualExamples][1] = 1.0f;
					break;
				case "财经":
					labelData[actualExamples][2] = 1.0f;
					break;
				case "军事":
					labelData[actualExamples][3] = 1.0f;
					break;
				case "体育":
					labelData[actualExamples][4] = 1.0f;
					break;
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
		articlesResults = playerArticles.getData(dual, true);
		articlesResults.sort("tags", 1);
	}
	
	public static void main(String[] args) {
		ArticleClassificationDataFetcher a = new ArticleClassificationDataFetcher();
		a.fetch(1);
		System.out.println("====================");
		byte[] uu = "彦".getBytes();
		for(byte b: uu) {
			System.out.print(b + " ");
		}
		System.out.println();
		System.out.println(DataInterconversionTool.bytesToFloatForce(uu));
	}

}
