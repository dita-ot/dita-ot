package org.dita.dost.resolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;

/**
 * @author Alan
 * 
 * Hold resolver that you actually used. If no URIResolver is specified,
 * an anonymous DITA-OT default resolver is used.
 * 
 * Usage: DitaURIResolverFactory.getURIResolver().resolve(href, base);
 * 
 */
public class DitaURIResolverFactory {
	private static URIResolver resolver;
	private static String path="";
	static {
		// DITA-OT default URIResolver
		/**
		 * The href parameter can be either absolute or relative path. If
		 * relative path is encountered, this function will tend to change it
		 * into an absolute path according to the basedir and tempdir defined in the
		 * program. Then it will use this absolute path to find the
		 * file. If no such file in the specific path is found or something goes
		 * wrong while trying to open the file, null is returned.
		 */
		resolver = new URIResolver() {
			public Source resolve(String href, String base) throws TransformerException {			
				File file = new File(href);
				if (!file.isAbsolute()) {
					String parentDir=null;
					if(base == null){
						parentDir=path;
					}
					else{
						parentDir=new File(parentDir).getAbsolutePath();
					}
					file = new File(parentDir, href);
				}
				try {
					return new SAXSource(new InputSource(new FileInputStream(file)));
				} catch (Exception e) {
					return null;
				}
			}
		};
	}

	public static URIResolver getURIResolver() {
		return resolver;
	}

	public static void setURIResolver(URIResolver resolver) {
		DitaURIResolverFactory.resolver = resolver;
	}

	public static void setPath(String path) {
		DitaURIResolverFactory.path = path;
	}
}
