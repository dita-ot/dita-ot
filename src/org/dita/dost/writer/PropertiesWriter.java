/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.dita.dost.module.Content;

/**
 * This class extends AbstractWriter, used to output content to properites file.
 * 
 * @version 1.0 2005-05-11
 * 
 * @author Wu, Zhi Qiang
 */
public class PropertiesWriter extends AbstractWriter {
	/** Properties used to output */
	private Properties prop = null;

	/**
	 * Default Constructor
	 */
	public PropertiesWriter() {
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractWriter#setContent(org.dita.dost.module.Content)
	 */
	public void setContent(Content content) {
		prop = (Properties) content.getValue();
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractWriter#write(java.lang.String)
	 */
	public void write(String filename) {
		FileOutputStream fileOutputStream = null;

		try {
			fileOutputStream = new FileOutputStream(filename);
			prop.store(fileOutputStream, null);
			fileOutputStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (Exception e) {
				}
			}
		}
	}

	public void writeToFile(File file) {
		try {
			prop.store(new FileOutputStream(file), null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
