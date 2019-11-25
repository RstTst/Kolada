package dev.rsttst.kolada.internal;

import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraftforge.fml.Logging.LOADING;
import static net.minecraftforge.fml.Logging.SCAN;

public class KoladaModLanguageProvider implements IModLanguageProvider {

    private static final Type K_MOD = Type.getType("Ldev/rsttst/kolada/KMod;");
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return scanResult -> {
            LOGGER.debug(SCAN, "Starting scan for @KMod classes in KoladaModLanguageProvider!");
            final Map<String, KoladaModTarget> modTargetMap = scanResult.getAnnotations().stream()
                    .filter(ad -> ad.getAnnotationType().equals(K_MOD))
                    .peek(ad -> LOGGER.debug(SCAN, "Found @KMod class {} with id {}", ad.getClassType().getClassName(), ad.getAnnotationData().get("value")))
                    .map(ad -> new KoladaModTarget(ad.getClassType().getClassName(), (String) ad.getAnnotationData().get("value")))
                    .collect(Collectors.toMap(KoladaModTarget::getModId, mt -> mt, (a, b) -> a));
            LOGGER.debug(SCAN, "Found the following in KoladaModLanguageProvider: {}", modTargetMap);
            LOGGER.debug(SCAN, modTargetMap.toString());
            scanResult.addLanguageLoader(modTargetMap);
        };
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) { /*no-op*/ }

    @Override
    public String name() {
        return "kolada";
    }

    public static class KoladaModTarget implements IModLanguageProvider.IModLanguageLoader {

        private static final Logger LOGGER = KoladaModLanguageProvider.LOGGER;
        private static final String CONTAINER_CLASS_NAME = "dev.rsttst.kolada.internal.KoladaModContainer";

        private final String className;
        private final String modId;

        private KoladaModTarget(String className, String modId) {
            this.className = className;
            this.modId = modId;
        }

        public String getModId() {
            return modId;
        }

        @Override
        public <T> T loadMod(IModInfo info, ClassLoader modClassLoader, ModFileScanData modFileScanResults) {
            try {
                final Class<?> koladaContainerClass = Class.forName(CONTAINER_CLASS_NAME, true, Thread.currentThread().getContextClassLoader());
                LOGGER.debug(LOADING, "Loading {} from classloader {} - got {}", CONTAINER_CLASS_NAME, Thread.currentThread().getContextClassLoader(), koladaContainerClass.getClassLoader());
                final Constructor<?> constructor = koladaContainerClass.getConstructor(IModInfo.class, String.class, ClassLoader.class, ModFileScanData.class);
                @SuppressWarnings("unchecked") T containerInstance = (T) constructor.newInstance(info, className, modClassLoader, modFileScanResults);
                return containerInstance;
            } catch (NoSuchMethodException ex) {
                LOGGER.fatal(LOADING, "Could not find {} constructor!", CONTAINER_CLASS_NAME, ex);
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                LOGGER.fatal(LOADING, "Could not find class {}!", CONTAINER_CLASS_NAME, ex);
                throw new RuntimeException(ex);
            } catch (InstantiationException ex) {
                LOGGER.fatal(LOADING, "Could not instantiate {}!", CONTAINER_CLASS_NAME, ex);
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                LOGGER.fatal(LOADING, "Could not access {} constructor!", CONTAINER_CLASS_NAME, ex);
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                LOGGER.fatal(LOADING, "{} constructor threw an exception!", CONTAINER_CLASS_NAME, ex);
                throw new RuntimeException(ex.getCause());
            }
        }
    }

}
