package org.vitrivr.cineast.core.data.raw.images;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;

import net.coobird.thumbnailator.Thumbnails;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;


public class InMemoryMultiImage implements MultiImage {

	/** Thumbnail image. This reference will remain in memory as long as {@link InMemoryMultiImage} does. */
	private SoftReference<BufferedImage> thumb;

	/** Reference to the colors array of the image. */
	private int[] colors;

	/** The width of the cached {@link MultiImage}. */
	private final int width;

	/** The height of the cached {@link MultiImage}. */
	private final int height;

	/** The height of the cached {@link MultiImage}. */
	private final int type;

	/**
	 * Constructor for {@link InMemoryMultiImage}.
	 *
	 * @param img {@link BufferedImage} to create a {@link InMemoryMultiImage} from.
	 */
	public InMemoryMultiImage(BufferedImage img){
		this(img, null);
	}

	/**
	 * Constructor for {@link InMemoryMultiImage}.
	 *
	 * @param img {@link BufferedImage} to create a {@link InMemoryMultiImage} from.
	 * @param thumb {@link BufferedImage} holding the thumbnail image.
	 */
	public InMemoryMultiImage(BufferedImage img, BufferedImage thumb) {
		this.colors = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
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
	 * Getter for the {@link BufferedImage} held by this {@link InMemoryMultiImage}. The image is reconstructed from the the
	 * color array. See {@link InMemoryMultiImage#getColors()}
	 *
	 * @return The image held by this  {@link InMemoryMultiImage}
	 */
	@Override
	public BufferedImage getBufferedImage() {
		final BufferedImage image = new BufferedImage(this.width, this.height, this.type);
		image.setRGB(0, 0, this.width, this.height, this.colors, 0, this.width);
		return image;
	}

	/**
	 * Getter for the thumbnail image of this {@link InMemoryMultiImage}. If the thumbnail image reference does not
	 * exist anymore, a new thumbnail image will be created from the original image.
	 *
	 * @return The thumbnail image for this {@link InMemoryMultiImage}
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
	 * Getter for the colors array representing this {@link InMemoryMultiImage}.
	 *
	 * @return The thumbnail image for this {@link InMemoryMultiImage}
	 */
	@Override
	public int[] getColors() {
		return this.colors;
	}

	/**
	 * Getter for the colors array representing the thumbnail of this {@link InMemoryMultiImage}.
	 *
	 * @return Color array of the thumbnail image.
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
	 * @return Width of the {@link MultiImage}
	 */
	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public void clear() {}
}
