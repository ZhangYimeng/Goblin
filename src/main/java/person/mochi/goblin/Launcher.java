package person.mochi.goblin;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

public class Launcher {

	public static void main(String[] args) {
		INDArray nd = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, new int[] { 3, 3 });
		System.out.println(nd);
		nd = Nd4j.rand(new int[] { 3, 3, 3 });
		System.out.println(nd);
		nd = nd.add(1);
		System.out.println(nd);
		nd = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, new int[] { 3, 3 });
		nd = nd.add(1);
		System.out.println(nd);
		nd = nd.mul(5);
		System.out.println(nd);
		nd = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, new int[] { 3, 3 });
		INDArray nd1 = Nd4j.create(new float[] { 1, 2, 3 }, new int[] { 3, 1 });
		System.out.println(nd.mmul(nd1));
		System.out.println(nd.transpose());
		INDArray nd2 = Nd4j.create(new float[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }, new int[] { 2, 6 });
		System.out.println(nd2);
		System.out.println(nd2.reshape(3, 4));
		System.out.println("==============");
		INDArray nd3 = nd2.linearView();
		System.out.println(nd3);
		System.out.println(nd.linearView());
		System.out.println(nd.isVector());
		System.out.println(nd.isScalar());
		System.out.println(nd.length());
		System.out.println(nd.size(0));
		nd2 = Nd4j.create(new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
		System.out.println(nd2);
		nd3 = nd2.broadcast(new int[]{3,12});
		System.out.println(nd3);
		INDArray ndv = Transforms.sigmoid(nd2);
		System.out.println(ndv);
		ndv = Transforms.tanh(nd2);
		System.out.println(ndv);
		System.out.println(1e-4);
	}

}
