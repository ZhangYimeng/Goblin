package person.mochi.test;

public class Test {

	public static void main(String[] args) {
		String[] paths = System.getProperty("java.library.path").split(";");
		for(String path: paths) {
			System.out.println(path);
		}
	}

}
