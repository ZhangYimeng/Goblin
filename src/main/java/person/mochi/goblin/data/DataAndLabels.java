package person.mochi.goblin.data;

import java.io.File;
import java.util.List;

public class DataAndLabels {
	
	private File trainData;
	private List<File> positionData;
	
	public DataAndLabels(File trainData, List<File> positionData) {
		this.setTrainData(trainData);
		this.setPositionData(positionData);
	}

	public List<File> getPositionData() {
		return positionData;
	}

	public void setPositionData(List<File> positionData) {
		this.positionData = positionData;
	}

	public File getTrainData() {
		return trainData;
	}

	public void setTrainData(File trainData) {
		this.trainData = trainData;
	}

}
