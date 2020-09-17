package com.vinopedia.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.vinopedia.test");
		//$JUnit-BEGIN$
		suite.addTest(new BatchTest("testSearchWines"));
		//$JUnit-END$
		return suite;
	}

}
