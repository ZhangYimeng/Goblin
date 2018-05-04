/**
 * @author Yimeng Zhang
 * @filename DeviceStreamTool.java
 * @package mingmeng.device.imp
 * @description 
 * @date 2015年1月4日
 */
package mochi.tool.data.interconversion;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import mochi.tool.data.interconversion.exception.ExceptionNotDoubleBytes;
import mochi.tool.data.interconversion.exception.ExceptionNotFloatBytes;

public class DataInterconversionTool {

	private static final Charset charset = Charset.forName("UTF-8");
	
	public static byte[] intToBytes(int integer) {
		byte[] temp = new byte[4];
		for(int i = 0, j = 24;i < 4; i++,j -= 8) {
			temp[i] = (byte) ((integer >> j) & 0xff);
		}
		return temp;
	}

	public static int bytesToInt(byte[] bytes) {
		if(bytes.length == 4) {
			int temp = 0;
			int mask = 0xffffffff;
			for(int i = 0, j = 0; i < bytes.length; i++, j += 8) {
				temp += ((bytes[i] << ((bytes.length-1)*8-j)) & (mask >>> j));
			}
			return temp;
		} if(bytes.length < 4) {
			int temp = 0;
			int mask = 0xffffffff >>> ((4 - bytes.length) * 8);
			int negativeCheckMask = 1 << 7;
			for(int i = 0, j = 0; i < bytes.length; i++, j += 8) {
				temp += ((bytes[i] << ((bytes.length-1)*8-j)) & (mask >>> j));
			}
			if((bytes[0] & negativeCheckMask) == 0){
				return temp;
			} else {
				return -((1 << bytes.length * 8) - temp);
			}
		} else {
			byte[] rebuild = new byte[4];
			for(int i = bytes.length - 1, j = 3; i >= bytes.length - 4; i--, j--) {
				rebuild[j] = bytes[i];
			}
			int temp = 0;
			int mask = 0xffffffff;
			for(int i = 0, j = 0; i < rebuild.length; i++, j += 8) {
				temp += ((rebuild[i] << ((rebuild.length-1)*8-j)) & (mask >>> j));
			}
			return temp;
		}
	}

	public static byte[] longToBytes(long integer) {
		byte[] temp = new byte[8];
		for(int i = 0, j = 56;i < 8; i++, j -= 8) {
			temp[i] = (byte) ((integer >> j) & 0xff);
		}
		return temp;
	}

	public static long bytesToLong(byte[] bytes) {
		if(bytes.length == 8) {
			long temp = 0;
			long mask = 0xffffffffffffffffL;
			for(int i = 0, j = 0; i < bytes.length; i++, j += 8) {
				temp += (((long)bytes[i] << ((bytes.length-1)*8-j)) & (mask >>> j));
			}
			return temp;
		} else if(bytes.length < 8) {
			long temp = 0;
			long mask = 0xffffffffffffffffL >>> ((8 - bytes.length) * 8);
			int negativeCheckMask = 1 << 7;
			for(int i = 0, j = 0; i < bytes.length; i++, j += 8) {
				temp += (((long)bytes[i] << ((bytes.length-1)*8-j)) & (mask >>> j));
			}
			if((bytes[0] & negativeCheckMask) == 0){
				return temp;
			} else {
				return -((1L << bytes.length * 8) - temp);
			}
		} else {
			byte[] rebuild = new byte[8];
			for(int i = bytes.length - 1, j = 7; i >= bytes.length - 8; i--, j--) {
				rebuild[j] = bytes[i];
			}
			long temp = 0;
			long mask = 0xffffffffffffffffL;
			for(int i = 0, j = 0; i < rebuild.length; i++, j += 8) {
				temp += (((long)rebuild[i] << ((rebuild.length-1)*8-j)) & (mask >>> j));
			}
			return temp;
		}
	}
	
	public static byte[] stringToBytes(String string) {
		return string.getBytes(charset);
	}

	public static byte[] stringToBytes(String string, String charset) {
		try {
			return string.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new byte[] {9};
		}
	}
	
	public static byte[] stringToBytes(String string, Charset charset) {
		return string.getBytes(charset);
	}

	public static String bytesToString(byte[] stringbytes) {
		return new String(stringbytes, charset);
	}
	
	public static String bytesToString(byte[] stringbytes, String charset) {
		try {
			return new String(stringbytes,charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "Wrong CharSet";
		}
	}
	
	public static String bytesToString(byte[] stringbytes, Charset charset) {
		return new String(stringbytes,charset);
	}

	public static byte[] shortToBytes(short integer) {
		byte[] temp = new byte[2];
		for(int i = 0, j = 8;i < 2; i++, j -= 8) {
			temp[i] = (byte) ((integer >> j) & 0xff);
		}
		return temp;
	}

	public static short bytesToShort(byte[] bytes) {
		if(bytes.length == 2) {
			short temp = 0;
			int mask = 0xffff;
			for(int i = 0, j = 0; i < bytes.length; i++, j += 8) {
				temp += (((short)bytes[i]) << ((bytes.length-1)*8-j)) & (mask >>> j);
			}
			return temp;
		} else if(bytes.length < 2) {
			short temp = 0;
			int mask = 0xffff >>> ((2 - bytes.length) * 8);
			int negativeCheckMask = 1 << 7;
			for(int i = 0, j = 0; i < bytes.length; i++, j += 8) {
				temp += (((short)bytes[i] << ((bytes.length-1)*8-j)) & (mask >>> j));
			}
			if((bytes[0] & negativeCheckMask) == 0){
				return temp;
			} else {
				return (short) -((1 << bytes.length * 8) - temp);
			}
		} else {
			byte[] rebuild = new byte[2];
			for(int i = bytes.length - 1, j = 1; i >= bytes.length - 2; i--, j--) {
				rebuild[j] = bytes[i];
			}
			short temp = 0;
			int mask = 0xffff;
			for(int i = 0, j = 0; i < rebuild.length; i++, j += 8) {
				temp += (((short)rebuild[i] << ((rebuild.length-1)*8-j)) & (mask >>> j));
			}
			return temp;
		}
	}

	public static byte[] floatToBytes(float real) {
		return intToBytes(Float.floatToIntBits(real));
	}

	public static float bytesToFloat(byte[] bytes) {
		try {
			if(bytes.length != 4) {
				throw new ExceptionNotFloatBytes();
			} else {
				return Float.intBitsToFloat(bytesToInt(bytes));
			}
		} catch (ExceptionNotFloatBytes e) {
			e.printStackTrace();
			System.exit(0);
			return Float.NaN;
		}
	}

	public static byte[] doubleToBytes(double real) {
		return longToBytes(Double.doubleToLongBits(real));
	}

	public static double bytesToDouble(byte[] bytes) {
		try {
			if(bytes.length != 8) {
				throw new ExceptionNotDoubleBytes();
			} else {
				return Double.longBitsToDouble(bytesToLong(bytes));
			}
		} catch (ExceptionNotDoubleBytes e) {
			e.printStackTrace();
			System.exit(0);
			return Double.NaN;
		}
	}

	public static byte booleanToByte(boolean bool) {
		return (byte) (bool?1:0);
	}

	public static boolean byteToBoolean(byte singlebyte) {
		if(singlebyte != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public static byte bytesToOneByte(byte[] bytes) {
		if(bytes != null) {
			if(bytes.length > 0) {
				return bytes[0];
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	public static String unicodeToString(String unicodeString) {
		int length = unicodeString.length() / 6;
		StringBuilder sb = new StringBuilder(length);  
	    for (int i = 0, j = 2; i < length; i++, j += 6) {  
	        String code = unicodeString.substring(j, j + 4);  
	        char ch = (char) Integer.parseInt(code, 16);  
	        sb.append(ch);  
	    }
	    return sb.toString();
	}
	
}
