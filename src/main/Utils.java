package main;

public class Utils {

	public static String format(float f, int n) {
		String s = "" + (float)((float)((int)(f*Math.pow(10, n))) / Math.pow(10, n));
		while(s.length() - s.indexOf('.') - 1 < n) {
			s += "0";
		}
		return s;
	}
}
