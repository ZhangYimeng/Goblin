package person.mochi.goblin.data.fetcher;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import person.mochi.goblin.data.set.type.DataSetType;

public class OverwatchPicDataFetcher {

	private static final String HOME_DIR = System.getProperty("user.home");
	private static final String SEPARATOR = System.getProperty("file.separator");
	private static final String ORG = "MochiAI";
	private static final String TRAINPATH = "train";
	private static final String TESTPATH = "test";
	private File dataSetFileParent;
	private Collection<File> dataSet;
	
	// public final static File ROOT_CACHE_DIR = new
	// File(System.getProperty("user.home"), DL4J_DIR);

	public OverwatchPicDataFetcher(DataSetType setType) throws IOException {
		switch (setType) {
		case TRAIN:
			dataSetFileParent = new File(HOME_DIR + SEPARATOR + ORG + SEPARATOR + TRAINPATH);
			if (!dataSetFileParent.exists()) {
				dataSetFileParent.mkdirs();
			}
			break;
		case TEST:
			dataSetFileParent = new File(HOME_DIR + SEPARATOR + ORG + SEPARATOR + TESTPATH);
			if (!dataSetFileParent.exists()) {
				dataSetFileParent.mkdirs();
			}
			break;
		}
		setDataSet(FileUtils.listFiles(dataSetFileParent, new String[] { "png", "txt" }, false));
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		System.out.println(HOME_DIR);
		System.out.println(SEPARATOR);
		System.out.println(DataSetType.TRAIN);
		System.out.println(DataSetType.TEST);
		System.out.println(HOME_DIR + SEPARATOR + ORG + SEPARATOR + TRAINPATH);
		System.out.println(HOME_DIR + SEPARATOR + ORG + SEPARATOR + TESTPATH);
		OverwatchPicDataFetcher op = new OverwatchPicDataFetcher(DataSetType.TRAIN);
	}

	public Collection<File> getDataSet() {
		return dataSet;
	}

	public void setDataSet(Collection<File> dataSet) {
		this.dataSet = dataSet;
	}

}
