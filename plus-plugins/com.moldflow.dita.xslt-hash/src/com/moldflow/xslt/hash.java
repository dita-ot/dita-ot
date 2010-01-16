package com.moldflow.xslt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class hash {

	public static String md5(String arg) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] bytes = md.digest(arg.getBytes());
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < md.getDigestLength(); i++) {
			result.append(String.format("%02x", bytes[i]));
		}
		return result.toString();
	}
}
