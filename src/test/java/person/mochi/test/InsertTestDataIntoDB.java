package person.mochi.test;

import com.wonder.mongodb.api.Duality;
import com.wonder.mongodb.api.MongoDBConfigWithAuth;
import com.wonder.mongodb.api.MongoDBPlayer;
import com.wonder.mongodb.api.exception.IllegalPortValueException;
import com.wonder.mongodb.api.exception.NoServerIPException;
import com.wonder.mongodb.api.exception.SelectedCollectionWithNoIndexesException;

public class InsertTestDataIntoDB {

	public static void main(String[] args)
			throws SelectedCollectionWithNoIndexesException, NoServerIPException, IllegalPortValueException {
		MongoDBConfigWithAuth conf = new MongoDBConfigWithAuth("127.0.0.1", 27017, "mochi", "gotohellmyevilex",
				new String[] { "data", "label" }, null);
		MongoDBPlayer db = new MongoDBPlayer(conf, "Yihan", "ImageDataForTest");
		Duality dual = new Duality("data", new byte[] { 3, 12 });
		dual.append("label", 3);
		db.insertData(dual);
	}

}
