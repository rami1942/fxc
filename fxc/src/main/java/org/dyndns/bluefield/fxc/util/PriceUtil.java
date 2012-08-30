package org.dyndns.bluefield.fxc.util;

public class PriceUtil {

	public static String separateComma(String orig) {
		StringBuilder buf = new StringBuilder();
		
		boolean isFirst = true;
		int fp = orig.length() % 3;
		if (fp > 0) {
			buf.append(orig.substring(0, fp));
			isFirst = false;
		}
		int p = fp;
		while (p < orig.length()) {
			if (isFirst) {
				isFirst = false;
			} else {
				buf.append(',');
			}
			buf.append(orig.substring(p, p+3));
			p += 3;
		}
		
		return buf.toString();
	}
}
