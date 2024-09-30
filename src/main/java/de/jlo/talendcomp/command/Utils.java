package de.jlo.talendcomp.command;

import org.apache.commons.exec.OS;

public class Utils {
	
	public static String getNativeFilePath(String path) {
		if (path != null) {
			path = path.trim();
			if (OS.isFamilyUnix()) {
				if (path.toLowerCase().startsWith("c:") || path.toLowerCase().startsWith("d:")) {
					path = path.substring(2);
				}
				path = path.replace("\\", "/");
				return path;
			} else {
				path = path.replace("/", "\\");
				if (path.startsWith("\\")) {
					path = "c:" + path;
				}
				return path;
			}
		} else {
			return null;
		}
	}

}
