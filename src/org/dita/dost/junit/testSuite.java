package org.dita.dost.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
//Test Suite.
@RunWith(Suite.class)
@SuiteClasses({testMergeUtils.class,testFileUtils.class,testStringUtils.class})
public class testSuite {

}
