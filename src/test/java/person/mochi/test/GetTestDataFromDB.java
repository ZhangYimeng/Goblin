package person.mochi.test;

import org.bson.types.Binary;

import com.wonder.mongodb.api.Duality;
import com.wonder.mongodb.api.MongoDBConfigWithAuth;
import com.wonder.mongodb.api.MongoDBPlayer;
import com.wonder.mongodb.api.Result;
import com.wonder.mongodb.api.Results;
import com.wonder.mongodb.api.exception.IllegalPortValueException;
import com.wonder.mongodb.api.exception.NoServerIPException;
import com.wonder.mongodb.api.exception.SelectedCollectionWithNoIndexesException;

public class GetTestDataFromDB {

	public static void main(String[] args)
			throws SelectedCollectionWithNoIndexesException, NoServerIPException, IllegalPortValueException {
		MongoDBConfigWithAuth conf = new MongoDBConfigWithAuth("127.0.0.1", 27017, "mochi", "gotohellmyevilex",
				new String[] { "data", "label" }, null);
		MongoDBPlayer db = new MongoDBPlayer(conf, "Yihan", "ImageDataForTest");
		Results results = db.getData(new Duality(), 10);
		while(results.hasNext()) {
			Result result = results.next();
			Binary bin = (Binary) result.get("data");
			for(byte b: bin.getData()) {
				System.out.print(b + " ");
			}
			System.out.println();
			System.out.println(result.get("label"));
		}
	}

}
