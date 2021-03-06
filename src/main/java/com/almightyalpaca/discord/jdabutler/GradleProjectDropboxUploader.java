package com.almightyalpaca.discord.jdabutler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class GradleProjectDropboxUploader {

	private static DbxClientV2			client;

	private static final AtomicBoolean	initialized				= new AtomicBoolean(false);

	public static final File			GRADLE_PROJECT_DIR		= new File("gradle project/");
	public static final File			GRADLE_BUILD_FILE		= new File(GradleProjectDropboxUploader.GRADLE_PROJECT_DIR, "build.gradle");
	public static final File			GRADLE_SETTINGS_FILE	= new File(GradleProjectDropboxUploader.GRADLE_PROJECT_DIR, "settings.gradle");
	public static final File			GRADLE_TEMP_DIR			= new File(GradleProjectDropboxUploader.GRADLE_PROJECT_DIR, ".gradle/");

	public static final File			SRC_MAIN_RESOURCES		= new File(GradleProjectDropboxUploader.GRADLE_PROJECT_DIR, "src/main/resources/");
	public static final File			SRC_MAIN_JAVA			= new File(GradleProjectDropboxUploader.GRADLE_PROJECT_DIR, "src/main/java/");

	public static final File			EXAMPLE_IMPL			= new File(GradleProjectDropboxUploader.SRC_MAIN_JAVA, "MessageListenerExample.java");

	public static final String			EXMAPLE_IMPL_URL		= "https://raw.githubusercontent.com/DV8FromTheWorld/JDA/master/src/examples/java/MessageListenerExample.java";

	public static final File			GRADLE_PROJECT_ZIP		= new File("exmaple gradle project for jda.zip");

	public static final String			DROPBOX_FILE_NAME		= "/JDA/jda gradle setup example.zip";

	public static void createZip() {
		Bot.LOG.info("Creating gradle example zip...");
		try {
			if (GradleProjectDropboxUploader.GRADLE_PROJECT_DIR.exists()) {
				FileUtils.cleanDirectory(GradleProjectDropboxUploader.GRADLE_PROJECT_DIR);
			} else {
				GradleProjectDropboxUploader.GRADLE_PROJECT_DIR.mkdirs();
			}

			if (GradleProjectDropboxUploader.GRADLE_PROJECT_ZIP.exists()) {
				GradleProjectDropboxUploader.GRADLE_PROJECT_ZIP.delete();
			}

			final ProcessBuilder builder = new ProcessBuilder(GradleDownloader.getGradlePath().getAbsolutePath(), "init");
			builder.directory(GradleProjectDropboxUploader.GRADLE_PROJECT_DIR);
			builder.inheritIO();

			final Process process = builder.start();

			process.waitFor(1L, TimeUnit.MINUTES);

			final String jdaVersion = Bot.config.getString("jda.version.name");

			final Collection<Pair<String, String>> repositories = Collections.singleton(new ImmutablePair<>("jcenter()", null));
			final Collection<Triple<String, String, String>> dependencies = Collections.singleton(new ImmutableTriple<>("net.dv8tion", "JDA", jdaVersion));

			FileUtils.write(GradleProjectDropboxUploader.GRADLE_BUILD_FILE, GradleUtil.getBuildFile(GradleUtil.DEFAULT_PLUGINS, "MessageListenerExample", "1.0", "1.8", dependencies, repositories,
					false), Charset.forName("UTF-8"));

			FileUtils.write(GradleProjectDropboxUploader.GRADLE_SETTINGS_FILE, "rootProject.name = 'Example gradle project for JDA'", Charset.forName("UTF-8"));

			FileUtils.deleteDirectory(GradleProjectDropboxUploader.GRADLE_TEMP_DIR);

			GradleProjectDropboxUploader.SRC_MAIN_JAVA.mkdirs();
			GradleProjectDropboxUploader.SRC_MAIN_RESOURCES.mkdirs();

			FileUtils.copyURLToFile(new URL(GradleProjectDropboxUploader.EXMAPLE_IMPL_URL), GradleProjectDropboxUploader.EXAMPLE_IMPL);

			final ZipFile zip = new ZipFile(GradleProjectDropboxUploader.GRADLE_PROJECT_ZIP);

			final ZipParameters parameters = new ZipParameters();
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_ULTRA);
			parameters.setDefaultFolderPath("exmaple gradle project for jda/");

			zip.addFolder(GradleProjectDropboxUploader.GRADLE_PROJECT_DIR, parameters);

			Bot.LOG.info("Zip creation finished!");

		} catch (final IOException | InterruptedException | ZipException e) {
			Bot.LOG.log(e);
		}
	}

	private static void init() {
		if (!GradleProjectDropboxUploader.initialized.getAndSet(true)) {
			final String ACCESS_TOKEN = Bot.config.getString("dropbox.access_token");

			final DbxRequestConfig config = DbxRequestConfig.newBuilder("JDA-Butler").build();

			GradleProjectDropboxUploader.client = new DbxClientV2(config, ACCESS_TOKEN);
		}
	}

	public static void uploadProject() {

		Bot.LOG.info("Uploading gradle example zip...");

		GradleProjectDropboxUploader.init();

		GradleProjectDropboxUploader.createZip();

		if (Bot.config.getBoolean("testing", true)) {
			Bot.LOG.debug("Skipping upload!");
			return;
		}

		try (InputStream in = new FileInputStream(GradleProjectDropboxUploader.GRADLE_PROJECT_ZIP)) {

			GradleProjectDropboxUploader.client.files().uploadBuilder(GradleProjectDropboxUploader.DROPBOX_FILE_NAME).withMute(true).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);

			Bot.LOG.info("Zip uploading finished!");

		} catch (DbxException | IOException e) {
			Bot.LOG.log(e);
		}

	}

}
