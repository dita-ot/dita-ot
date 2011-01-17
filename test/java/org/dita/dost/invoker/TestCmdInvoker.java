package org.dita.dost.invoker;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.invoker.CommandLineInvoker;
import org.junit.Test;

public class TestCmdInvoker {

	@Test(expected = DITAOTException.class)
	public void testProcessArguments() throws Exception {
		String input[]={"/i:abc.ditamap","/transtypexhtml"};
		CommandLineInvoker test = new CommandLineInvoker();
		test.processArguments(input);
	}
	
	@Test(expected = DITAOTException.class)
	public void testProcessArgsWrongParam() throws Exception {
		String input[]={"/i:abc.ditamap","/abc:def"};
		CommandLineInvoker test = new CommandLineInvoker();		
		test.processArguments(input);
	}
	
	@Test(expected = DITAOTException.class)
	public void testProcessArgsEmptyValue() throws Exception {
		String input[]={"/i:"};
		CommandLineInvoker test = new CommandLineInvoker();
		test.processArguments(input);
	}

}
