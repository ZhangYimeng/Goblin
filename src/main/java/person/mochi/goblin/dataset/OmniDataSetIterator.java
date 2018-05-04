package person.mochi.goblin.dataset;

import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator;
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher;

public class OmniDataSetIterator extends BaseDatasetIterator {

	private static final long serialVersionUID = 8418417729331070089L;

	public OmniDataSetIterator(int batch, int numExamples, DataSetFetcher fetcher) {
		super(batch, numExamples, fetcher);
	}
	
	public OmniDataSetIterator(int batch, DataSetFetcher fetcher) {
		super(batch, -1, fetcher);
	}

}
