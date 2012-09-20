package org.dyndns.bluefield.fxc.util;

import java.util.List;

public class PriceUtil {

	public static String separateComma(String orig) {
		if (orig == null || orig.equals("")) return "";

		StringBuilder buf = new StringBuilder();
		String s;
		if (orig.charAt(0) == '-') {
			s = orig.substring(1);
			buf.append('-');
		} else {
			s = orig;
		}

		boolean isFirst = true;
		int fp = s.length() % 3;
		if (fp > 0) {
			buf.append(s.substring(0, fp));
			isFirst = false;
		}
		int p = fp;
		while (p < s.length()) {
			if (isFirst) {
				isFirst = false;
			} else {
				buf.append(',');
			}
			buf.append(s.substring(p, p+3));
			p += 3;
		}

		return buf.toString();
	}

	public static Integer size(List<?> list) {
		return list.size();
	}

	public static String roundCommaSep(Double d) {
		int n = (int)Math.round(d);
		return separateComma(Integer.toString(n));
	}
}
