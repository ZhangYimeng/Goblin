package person.mochi.goblin.dataset;

import java.util.Arrays;

import org.bson.types.Binary;
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

public class MongoDBDataSetFetcher extends BaseDataFetcher {

	private static final long serialVersionUID = 3554743576571534884L;
	private MongoDBPlayer db;
	private Results results;
	private int skip;
	private int limit;

	public MongoDBDataSetFetcher(String ip, int port, String username, String password, String database,
			String collection, Integer totalOutcomes, int skip, int limit)
			throws NoServerIPException, IllegalPortValueException, SelectedCollectionWithNoIndexesException {
		MongoDBConfigWithAuth conf = new MongoDBConfigWithAuth(ip, port, "mochi", "gotohellmyevilex",
				new String[] { "data", "label" }, null);
		db = new MongoDBPlayer(conf, database, collection);
		this.numOutcomes = totalOutcomes;
		results = db.getData(new Duality(), skip, limit);
		totalExamples = (int) results.size();
		this.skip = skip;
		this.limit = limit;
	}

	@Override
	public int cursor() {
		return cursor;
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
			Result result = results.next();
			byte[] dataBytes = ((Binary) result.get("data")).getData();
			int label = result.getInteger("label");

			float[] featureVec = new float[dataBytes.length];
			featureData[actualExamples] = featureVec;
			labelData[actualExamples] = new float[numOutcomes];
			labelData[actualExamples][label] = 1.0f;

			for (int j = 0; j < dataBytes.length; j++) {
				float v = ((int) dataBytes[j]) & 0xFF; // byte is loaded as signed -> convert to unsigned
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
	public boolean hasMore() {
		return cursor < totalExamples;
	}

	@Override
	public DataSet next() {
		DataSet next = super.next();
		return next;
	}

	@Override
	public void reset() {
		cursor = 0;
		curr = null;
		results = db.getData(new Duality(), skip, limit);
	}

	@Override
	public int totalExamples() {
		return totalExamples;
	}

	@Override
	public int totalOutcomes() {
		return numOutcomes;
	}

}
