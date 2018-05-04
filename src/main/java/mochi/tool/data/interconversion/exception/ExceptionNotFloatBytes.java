package mochi.tool.data.interconversion.exception;

public class ExceptionNotFloatBytes extends Exception {

	private static final long serialVersionUID = 5462706286053312196L;

	public ExceptionNotFloatBytes(){
		super();
		System.out.println("单精度的浮点数字节数组应为4个字节，请检查！");
	}
	
	public ExceptionNotFloatBytes(String s){
		super(s);
		System.out.println(s);
	}
	
}
