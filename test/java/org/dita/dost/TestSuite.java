package org.dita.dost;

import org.dita.dost.util.TestFileUtils;
import org.dita.dost.util.TestStringUtils;
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
