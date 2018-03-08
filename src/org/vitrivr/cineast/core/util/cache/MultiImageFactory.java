package org.vitrivr.cineast.core.util.cache;

import java.awt.image.BufferedImage;

import java.io.IOException;

import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.data.raw.images.CachedMultiImage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.raw.images.InMemoryMultiImage;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;

public class MultiImageFactory {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * The prefix used for multi image cache files.
     */
    private static final String PREFIX = "image";

    /**
     * Private constructor as this is a factory class.
     */
    private MultiImageFactory() {}

    /**
     *
     * @param bimg
     * @return
     */
    public static MultiImage newMultiImage(BufferedImage bimg) {
        return newMultiImage(bimg, null);
    }

    /**
     *
     * @param bimg
     * @param thumb
     * @return
     */
    public static MultiImage newMultiImage(BufferedImage bimg, BufferedImage thumb) {
        if (Config.sharedConfig().getCache().keepInMemory(bimg.getWidth() * bimg.getHeight() * 3 * 8)) {
            return new InMemoryMultiImage(bimg, thumb);
        } else {
            return newCachedMultiImage(bimg, thumb, PREFIX);
        }
    }

    /**
     *
     * @param width
     * @param height
     * @param colors
     * @return
     */
    public static MultiImage newMultiImage(int width, int height, int[] colors) {
        height = MultiImageFactory.checkHeight(width, height, colors);
        final BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bimg.setRGB(0, 0, width, height, colors, 0, width);
        if (Config.sharedConfig().getCache().keepInMemory(colors.length * 8)) {
            return newInMemoryMultiImage(bimg);
        } else {
            return newCachedMultiImage(bimg, PREFIX);
        }
    }


    /**
     * @param width
     * @param height
     * @param colors
     * @return
     */
    public static MultiImage newInMemoryMultiImage(int width, int height, int[] colors) {
        height = MultiImageFactory.checkHeight(width, height, colors);
        final BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        bimg.setRGB(0, 0, width, height, colors, 0, width);
        return newInMemoryMultiImage(bimg);
    }

    /**
     * Wraps the provided  {@link BufferedImage} in a {@link InMemoryMultiImage} object and returns it.
     *
     * @param bimg The {@link BufferedImage} that should be wrapped.
     * @return {@link InMemoryMultiImage}.
     */
    public static MultiImage newInMemoryMultiImage(BufferedImage bimg) {
        return new InMemoryMultiImage(bimg);
    }

    /**
     * Creates and returns a new {@link CachedMultiImage} from the provided image using the provided cache prefix. If the
     * instantiation of the {@link CachedMultiImage} fails, the method is allowed to fallback to a {@link InMemoryMultiImage}
     *
     * @param image The image from which to create a {@link CachedMultiImage}.
     * @param prefix The cache prefix used to name the files.
     * @return {@link CachedMultiImage} or  {@link InMemoryMultiImage}, if former could not be created.
     * @throws UncheckedIOException If creation of {@link CachedMultiImage} and cache policy equals {@link CacheConfig.Policy#FORCE_DISK_CACHE}.
     */
    public static MultiImage newCachedMultiImage(BufferedImage image, String prefix) {
        final CacheConfig.Policy cachePolicy = Config.sharedConfig().getCache().getCachingPolicy();
        final Path cacheLocation = Config.sharedConfig().getCache().getCacheLocation();
        try {
            return new CachedMultiImage(image, Files.createTempFile(cacheLocation, PREFIX, ".tmp"));
        } catch (IOException e) {
            if (cachePolicy == CacheConfig.Policy.FORCE_DISK_CACHE) {
                LOGGER.error("Failed to instantiate an object of type CachedMultiImage. No data object is created due to policy restrictions (FORCE_DISK_CACHE).");
                throw new UncheckedIOException(e);
            } else {
                LOGGER.warn("Failed to instantiate an object of type CachedMultiImage. Fallback to InMemoryMultiImage instead.");
                return new InMemoryMultiImage(image);
            }
        }
    }

    /**
     * Creates and returns a new {@link CachedMultiImage} from the provided image using the provided cache prefix. If the
     * instantiation of the {@link CachedMultiImage} fails, the method is allowed to fallback to a {@link InMemoryMultiImage}
     *
     * @param image The image from which to create a {@link CachedMultiImage}.
     * @param thumb Pre-computed thumbnail that should be used.
     * @param prefix The cache prefix used to name the files.
     * @return {@link CachedMultiImage} or  {@link InMemoryMultiImage}, if former could not be created.
     * @throws UncheckedIOException If creation of {@link CachedMultiImage} and cache policy equals {@link CacheConfig.Policy#FORCE_DISK_CACHE}.
     */
    public static MultiImage newCachedMultiImage(BufferedImage image, BufferedImage thumb, String prefix) {
        final CacheConfig.Policy cachePolicy = Config.sharedConfig().getCache().getCachingPolicy();
        final Path cacheLocation = Config.sharedConfig().getCache().getCacheLocation();
        try {
            return new CachedMultiImage(image, thumb, Files.createTempFile(cacheLocation, prefix, ".tmp"));
        } catch (IOException e) {
            if (cachePolicy == CacheConfig.Policy.FORCE_DISK_CACHE) {
                LOGGER.error("Failed to instantiate an object of type CachedMultiImage. No data object is created due to policy restrictions (FORCE_DISK_CACHE).");
                throw new UncheckedIOException(e);
            } else {
                LOGGER.warn("Failed to instantiate an object of type CachedMultiImage. Fallback to InMemoryMultiImage instead.");
                return new InMemoryMultiImage(image, thumb);
            }
        }
    }

    private static int checkHeight(int width, int height, int[] colors) {
        if (colors.length / width != height) {
            LOGGER.debug("Dimension missmatch in MultiImage, setting height from {} to {}", height, (height = colors.length / width));
        }
        return height;
    }
}
