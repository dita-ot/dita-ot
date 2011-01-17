package org.dita.dost;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.dita.dost.util.TestDITAOTCopy;

public class TestUtils {

	/**
	 * Create temporary directory based on test class.
	 * 
	 * @param testClass test class
	 * @return temporary directory
	 * @throws IOException if creating directory failed
	 */
	public static File createTempDir(final Class testClass) throws IOException {
		final File tempDir = new File(System.getProperty("java.io.tmpdir"),
									 testClass.getName());
		if (!tempDir.exists() && !tempDir.mkdirs()) {
			throw new IOException("Unable to create temporary directory " + tempDir.getAbsolutePath());
		}
		return tempDir;
	}
	
	/**
	 * Read file contents into a string
	 * 
	 * @param file file to read
	 * @return contents of the file
	 * @throws IOException if reading file failed
	 */
	public static String readFileToString(final File file) throws IOException {
		return readFileToString(file, false);
	}
	
	/**
	 * Read file contents into a string
	 * 
	 * @param file file to read
	 * @param ignoreHead ignore first row
	 * @return contents of the file
	 * @throws IOException if reading file failed
	 */
	public static String readFileToString(final File file, final boolean ignoreHead) throws IOException {
		StringBuilder std = new StringBuilder();
		BufferedReader in = null;
		try {
            in = new BufferedReader(new FileReader(file));
            if (ignoreHead) {
            	in.readLine();
            }
            String str;
            while ((str = in.readLine()) != null) {
            	std.append(str);
            }
        } finally {
        	if (in != null) {
        		in.close();
        	}
        }
        return std.toString();
	}
	
	/**
	 * Deletes a file. If file is a directory, delete it and all sub-directories.
	 * 
	 * @param file file or directory to delete, must not be null 
	 * @throws IOException in case deletion is unsuccessful
	 */
	public static void forceDelete(final File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (final File c: file.listFiles()) {
					forceDelete(c);
				}
			}
			if (!file.delete()) {
				throw new IOException("Failed to delete " + file.getAbsolutePath());
			}
		}
	}
	
}
