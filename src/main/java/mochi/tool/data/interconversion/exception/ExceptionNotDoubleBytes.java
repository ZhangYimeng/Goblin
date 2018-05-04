package mochi.tool.data.interconversion.exception;

public class ExceptionNotDoubleBytes extends Exception{

	private static final long serialVersionUID = 5200362460855917385L;

	public ExceptionNotDoubleBytes(){
		super();
		System.out.println("双精度的浮点数字节数组应为8个字节，请检查！");
	}
	
	public ExceptionNotDoubleBytes(String s){
		super(s);
		System.out.println(s);
	}
	
}
