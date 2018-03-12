package org.vitrivr.cineast.core.data.raw.images;

import org.vitrivr.cineast.core.data.raw.bytes.ByteData;
import org.vitrivr.cineast.core.data.raw.bytes.CachedByteData;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;

public class CachedMultiImage extends CachedByteData implements MultiImage {

    /** The width of the cached {@link MultiImage}. */
    private final int width;

    /** The height of the cached {@link MultiImage}. */
    private final int height;

    /** The height of the cached {@link MultiImage}. */
    private final int type;

    /** Soft reference to the thumbnail image. May be garbage collected under memory pressure. */
    private SoftReference<BufferedImage> thumb;

    /**
     * Constructor for {@link CachedMultiImage}.
     *
     * @param img {@link BufferedImage} to create a {@link CachedMultiImage} from.
     * @param cacheFile The cache file in which to store {@link CachedMultiImage}.
     * @throws IOException If creation of the cache file failed.
     */
    public CachedMultiImage(BufferedImage img, Path cacheFile) throws IOException {
        this(img, null, cacheFile);
    }

    /**
     * Constructor for {@link CachedMultiImage}.
     *
     * @param img {@link BufferedImage} to create a {@link CachedMultiImage} from.
     * @param thumb {@link BufferedImage} holding the thumbnail image.
     * @param cacheFile The cache file in which to store {@link CachedMultiImage}.
     * @throws IOException If creation of the cache file failed.
     */
    public CachedMultiImage(BufferedImage img, BufferedImage thumb, Path cacheFile) throws IOException {
        super(toBytes(img), cacheFile);
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.type = img.getType();
        if (thumb != null) {
            this.thumb = new SoftReference<>(thumb);
        } else {
            this.thumb = new SoftReference<>(MultiImage.generateThumb(img));
        }
    }

    /**
     * Getter for the thumbnail image of this {@link CachedMultiImage}. If the thumbnail image reference does not
     * exist anymore, a new thumbnail image will be created from the raw data.
     *
     * Calling this method will cause the soft reference {@link CachedMultiImage#thumb} to be refreshed! However, there is
     * no guarantee that the reference will still be around when invoking this or any other accessor the next time.
     *
     * @return The thumbnail image for this {@link CachedMultiImage}
     */
    @Override
    public BufferedImage getThumbnailImage() {
        BufferedImage thumbnail = this.thumb.get();
        if (thumbnail == null) {
            thumbnail = MultiImage.generateThumb(this.getBufferedImage());
        }
        this.thumb = new SoftReference<>(thumbnail);
        return thumbnail;
    }

    /**
     * Getter for the colors array representing this {@link CachedMultiImage}. If the reference to that array does not
     * exist anymore, the array will be loaded from cache.
     *
     * Calling this method will cause the soft reference {@link CachedMultiImage#data} to be refreshed! However, there is
     * no guarantee that the reference will still be around when invoking this or any other accessor the next time.
     *
     * @return The thumbnail image for this {@link CachedMultiImage}
     */
    @Override
    public int[] getColors() {
        final ByteBuffer buffer = this.buffer();
        int[] colors = new int[this.width * this.height];
        for (int i=0; i<colors.length; i++) {
            colors[i] = buffer.getInt();
        }
        return colors;
    }

    /**
     * Getter for the {@link BufferedImage} held by this {@link CachedMultiImage}. The image is reconstructed from the the
     * color array. See {@link CachedMultiImage#getColors()}
     *
     * Calling this method will cause the soft reference {@link CachedMultiImage#data} to be refreshed! However, there is
     * no guarantee that the reference will still be around when invoking this or any other accessor the next time.
     *
     * @return The image held by this  {@link CachedMultiImage}
     */
    @Override
    public BufferedImage getBufferedImage() {
        int[] colors = getColors();
        final BufferedImage image = new BufferedImage(this.width, this.height, this.type);
        image.setRGB(0, 0, this.width, this.height, colors, 0, this.width);
        return image;
    }

    /**
     * Getter for the colors array representing the thumbnail of this {@link CachedMultiImage}.
     *
     * Calling this method will cause the soft reference {@link CachedMultiImage#data} to be refreshed! However, there is
     * no guarantee that the reference will still be around when invoking this or any other accessor the next time.
     *
     * @return Color array
     */
    @Override
    public int[] getThumbnailColors() {
        final BufferedImage thumb = this.getThumbnailImage();
        return this.getThumbnailImage().getRGB(0, 0, thumb.getWidth(), thumb.getHeight(), null, 0, thumb.getWidth());
    }

    /**
     * Getter for width value.
     *
     * @return Width of the {@link MultiImage}
     */
    @Override
    public int getWidth() {
        return this.width;
    }

    /**
     * Getter for height value.
     *
     * @return Height of the {@link MultiImage}
     */
    @Override
    public int getHeight() {
        return this.height;
    }

    /**
     * Force clears all the soft reference associated with this {@link ByteData} object, that can be re-calculated on demand
     */
    @Override
    public void clear() {
        this.thumb.clear();
    }

    /**
     * Converts the {@link BufferedImage} to a byte array representation.
     *
     * @param img The {@link BufferedImage} that should be converted.
     * @return The byte array representing the {@link BufferedImage}
     */
    private static byte[] toBytes(BufferedImage img) {
        int[] colors = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        return toBytes(colors, img.getWidth(),img.getHeight());
    }

    /**
     * Converts the int array holding the colors of a {@link BufferedImage} to a byte array representation.
     *
     * @param colors The int array holding the color values.
     * @param width Width of the image.
     * @param height Height of the image.
     * @return The byte array representing the {@link BufferedImage}
     */
    private static byte[] toBytes(int[] colors, int width, int height) {
        final ByteBuffer data = ByteBuffer.allocate(width * height * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int c : colors) {
            data.putInt(c);
        }
        return data.array();
    }
}