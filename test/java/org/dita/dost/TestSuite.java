/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
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
