package org.dyndns.bluefield.fxc.util;

import junit.framework.TestCase;

public class PriceUtilTest extends TestCase {
	public void test1() {
		assertEquals("700", PriceUtil.separateComma("700"));
	}

	public void test2() {
		assertEquals("7,010", PriceUtil.separateComma("7010"));
	}

	public void test3() {
		assertEquals("70,200", PriceUtil.separateComma("70200"));
	}

	public void test4() {
		assertEquals("700,003", PriceUtil.separateComma("700003"));
	}

}
