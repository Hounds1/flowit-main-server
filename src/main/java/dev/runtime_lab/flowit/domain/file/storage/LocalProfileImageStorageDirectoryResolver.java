package dev.runtime_lab.flowit.domain.file.storage;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import org.springframework.util.StringUtils;

public final class LocalProfileImageStorageDirectoryResolver {

	private static final String APP_DIRECTORY_WINDOWS_AND_MAC = "Flowit";
	private static final String APP_DIRECTORY_UNIX = "flowit";
	private static final String SERVER_DIRECTORY = "main-server";
	private static final String PROFILE_IMAGE_DIRECTORY = "profile-images";

	private LocalProfileImageStorageDirectoryResolver() {
	}

	public static Path resolve(String configuredDirectory) {
		if (StringUtils.hasText(configuredDirectory)) {
			return Path.of(configuredDirectory.trim());
		}

		return resolveDefault(
			System.getProperty("os.name"),
			System.getProperty("user.home"),
			System.getenv()
		);
	}

	static Path resolveDefault(String osName, String userHome, Map<String, String> environment) {
		String normalizedOsName = osName == null ? "" : osName.toLowerCase(Locale.ROOT);
		Path homeDirectory = Path.of(requiredUserHome(userHome));

		if (normalizedOsName.contains("win")) {
			return windowsBaseDirectory(homeDirectory, environment)
				.resolve(APP_DIRECTORY_WINDOWS_AND_MAC)
				.resolve(SERVER_DIRECTORY)
				.resolve(PROFILE_IMAGE_DIRECTORY);
		}
		if (normalizedOsName.contains("mac") || normalizedOsName.contains("darwin")) {
			return homeDirectory
				.resolve("Library")
				.resolve("Application Support")
				.resolve(APP_DIRECTORY_WINDOWS_AND_MAC)
				.resolve(SERVER_DIRECTORY)
				.resolve(PROFILE_IMAGE_DIRECTORY);
		}

		return unixDataHome(homeDirectory, environment)
			.resolve(APP_DIRECTORY_UNIX)
			.resolve(SERVER_DIRECTORY)
			.resolve(PROFILE_IMAGE_DIRECTORY);
	}

	public static boolean isDedicatedProfileImageDirectory(Path directory) {
		Path normalizedDirectory = directory.toAbsolutePath().normalize();
		Path leaf = normalizedDirectory.getFileName();
		Path serverDirectory = normalizedDirectory.getParent();
		Path applicationDirectory = serverDirectory == null ? null : serverDirectory.getParent();

		return leaf != null
			&& PROFILE_IMAGE_DIRECTORY.equals(leaf.toString())
			&& serverDirectory != null
			&& SERVER_DIRECTORY.equals(serverDirectory.getFileName().toString())
			&& applicationDirectory != null
			&& isFlowitApplicationDirectory(applicationDirectory.getFileName().toString());
	}

	private static Path windowsBaseDirectory(Path homeDirectory, Map<String, String> environment) {
		String localAppData = environment.get("LOCALAPPDATA");
		if (StringUtils.hasText(localAppData)) {
			return Path.of(localAppData.trim());
		}
		return homeDirectory.resolve("AppData").resolve("Local");
	}

	private static Path unixDataHome(Path homeDirectory, Map<String, String> environment) {
		String xdgDataHome = environment.get("XDG_DATA_HOME");
		if (StringUtils.hasText(xdgDataHome)) {
			return Path.of(xdgDataHome.trim());
		}
		return homeDirectory.resolve(".local").resolve("share");
	}

	private static boolean isFlowitApplicationDirectory(String directoryName) {
		return APP_DIRECTORY_WINDOWS_AND_MAC.equals(directoryName)
			|| APP_DIRECTORY_UNIX.equals(directoryName);
	}

	private static String requiredUserHome(String userHome) {
		if (!StringUtils.hasText(userHome)) {
			throw new IllegalStateException("user.home must be set to resolve local profile image storage directory");
		}
		return userHome.trim();
	}
}
