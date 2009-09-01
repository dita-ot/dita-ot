package org.dita.dost.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
//Test Suite.
@RunWith(Suite.class)
@SuiteClasses({
	//TestMergeUtils.class,
	TestFileUtils.class,TestStringUtils.class})
public class TestSuite {

}
