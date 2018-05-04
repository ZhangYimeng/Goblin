package person.mochi.goblin.dataset;

import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator;

import com.wonder.mongodb.api.exception.IllegalPortValueException;
import com.wonder.mongodb.api.exception.NoServerIPException;
import com.wonder.mongodb.api.exception.SelectedCollectionWithNoIndexesException;

public class MongoDBDataSetIterator extends BaseDatasetIterator {

	private static final long serialVersionUID = 537807878432619820L;
	
	public MongoDBDataSetIterator(int batch, int numExamples, String ip, int port, String username, String password, String database, String collection, int totalOutcomes, int skip, int limit) throws NoServerIPException, IllegalPortValueException, SelectedCollectionWithNoIndexesException {
		super(batch, numExamples, new MongoDBDataSetFetcher(ip, port, username, password, database, collection, totalOutcomes, skip, limit));
	}

}
