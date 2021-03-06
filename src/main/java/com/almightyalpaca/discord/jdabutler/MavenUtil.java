package com.almightyalpaca.discord.jdabutler;

import org.apache.commons.lang3.tuple.Triple;

public class MavenUtil {
	public static String getDependencyString(final String group, final String name, final String version, String indentation) {
		if (indentation == null) {
			indentation = "";
		}

		return indentation + "<dependency>\n" + indentation + "    <groupId>" + group + "</groupId>\n" + indentation + "    <artifactId>" + name + "</artifactId>\n" + indentation + "    <version>"
				+ version + "</version>\n" + indentation + "</dependency>\n";
	}

	public static String getDependencyString(final Triple<String, String, String> dependency, final String indentation) {
		return MavenUtil.getDependencyString(dependency.getLeft(), dependency.getMiddle(), dependency.getRight(), indentation);
	}

	public static String getRepositoryString(final String id, final String name, final String url, String indentation) {
		if (indentation == null) {
			indentation = "";
		}
		return indentation + "<repository>\n" + indentation + "    <id>" + id + "</id>\n" + indentation + "    <name>" + name + "</name>\n" + indentation + "    <url>" + url + "</url>\n" + indentation
				+ "</repository>\n";
	}

}
