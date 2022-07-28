package link.infra.jumploader.launch.serviceloading;

import link.infra.jumploader.launch.PreLaunchDispatcher;
import link.infra.jumploader.launch.ReflectionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.Map;

public class QuiltFilesystemURLHandlerAppender implements PreLaunchDispatcher.Handler {
	private final Logger LOGGER = LogManager.getLogger();

	@Override
	public void handlePreLaunch(ClassLoader loadingClassloader) {
		// Jimfs requires some funky hacks to load under a custom classloader, Java protocol handlers don't handle custom classloaders very well
		// See also FileSystemProviderAppender
		try {
			Class<?> jfsHandler = Class.forName("org.quiltmc.loader.impl.filesystem.quilt.jfs.Handler", true, loadingClassloader);
			Class<?> mfsHandler = Class.forName("org.quiltmc.loader.impl.filesystem.quilt.mfs.Handler", true, loadingClassloader);
			// Add the jimfs handler to the URL handlers field, because Class.forName by default uses the classloader that loaded the calling class (in this case the system classloader, so we have to do it manually)
			Map<String, URLStreamHandler> handlers = ReflectionUtil.reflectStaticField(URL.class, "handlers");
			handlers.putIfAbsent("quilt.jfs", (URLStreamHandler) jfsHandler.getDeclaredConstructor().newInstance());
			handlers.putIfAbsent("quilt.mfs", (URLStreamHandler) mfsHandler.getDeclaredConstructor().newInstance());
		} catch (ClassNotFoundException ignored) {
			// Ignore class not found - jimfs presumably isn't in the classpath
		} catch (ReflectiveOperationException | ClassCastException e) {
			LOGGER.warn("Failed to fix jimfs loading, jar-in-jar may not work", e);
		}

		try {
			ReflectionUtil.transformStaticField(URL.class, "factory", factory -> null);
		} catch (ReflectiveOperationException e) {
			LOGGER.warn("Failed to fix jimfs loading, jar-in-jar may not work", e);
		}
	}
}
