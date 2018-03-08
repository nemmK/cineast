package org.vitrivr.cineast.core.util.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.raw.bytes.ByteData;
import org.vitrivr.cineast.core.data.raw.bytes.CachedByteData;
import org.vitrivr.cineast.core.data.raw.bytes.InMemoryByteData;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * This factory class generates {@link ByteData} objects based on a heuristic involving the size of the allocated data chunks.
 */
public class ByteDataFactory {
    /** Logger instance used to log errors. */
    private static final Logger LOGGER = LogManager.getLogger();

    /* Sets up the cache location (if specified). */
    static  {
        Path cacheLocation = Config.sharedConfig().getImagecache().getCacheLocation();
        try {
            Files.createDirectories(cacheLocation);
        } catch (IOException e) {
            LOGGER.fatal("Could not create the cache location under {}", cacheLocation.toAbsolutePath().toString());
            LOGGER.fatal(e);
        }
    }

    /**
     * Private constructor as this is a factory class.
     */
    private ByteDataFactory() {}

    /**
     * Wraps the provided byte array in a {@link ByteData} object and returns it. This method determines whether to use a
     * {@link InMemoryByteData} or a {@link CachedByteData} object based on the size of the data object and global
     * application settings.
     *
     * @param data The data that should be wrapped.
     * @return {@link ByteData} object.
     */
    public static ByteData newData(byte[] data) {
        if (Config.sharedConfig().getImagecache().keepInMemory(data.length)) {
            return newInMemoryData(data);
        } else {
            return newCachedData(data, "default");
        }
    }

    /**
     * Wraps the provided byte array in a {@link ByteData} object and returns it. This method determines whether to use a
     * {@link InMemoryByteData} or a {@link CachedByteData} object based on the size of the data object and global
     * application settings.
     *
     * @param data Size of the data object.
     * @param prefix The string prefix used to denote the cache file.
     * @return {@link ByteData} object.
     */
    public static ByteData newData(byte[] data, String prefix) {
        if (Config.sharedConfig().getImagecache().keepInMemory(data.length)) {
            return newInMemoryData(data);
        } else {
            return newCachedData(data, prefix);
        }
    }

    /**
     * Wraps the provided byte array in a {@link InMemoryByteData} object and returns it.
     *
     * @param data The data that should be wrapped.
     * @return {@link ByteData} object.
     */
    public static ByteData newInMemoryData(byte[] data) {
        return new InMemoryByteData(data);
    }

    /**
     * Wraps the provided byte array in a {@link CachedByteData} object and returns it. If for some reason, allocation
     * of the {@link CachedByteData} fails, an {@link InMemoryByteData} will be returned instead.
     *
     * @param data The data that should be wrapped.
     * @return {@link ByteData} object.
     * @throws UncheckedIOException If the allocation of the {@link CachedByteData} fails and FORCE_DISK_CACHE cache policy is used.
     */
    public static ByteData newCachedData(byte[] data, String prefix) {
        final CacheConfig.Policy cachePolicy = Config.sharedConfig().getImagecache().getCachingPolicy();
        final Path cacheLocation = Config.sharedConfig().getImagecache().getCacheLocation();
        try {
            return new CachedByteData(data, Files.createTempFile(cacheLocation, prefix, ".tmp"));
        } catch (IOException e) {
            if (cachePolicy == CacheConfig.Policy.FORCE_DISK_CACHE) {
                LOGGER.error("Failed to instantiate an object of type CachedByteData. No data object is created due to policy restrictions (FORCE_DISK_CACHE).");
                throw new UncheckedIOException(e);
            } else {
                LOGGER.warn("Failed to instantiate an object of type CachedByteDate. Fallback to InMemoryByteData instead.");
                return new InMemoryByteData(data);
            }
        }
    }
}
