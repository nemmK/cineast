package org.vitrivr.cineast.core.data.raw.bytes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The {@link InMemoryByteData} object is an immutable {@link ByteData} object that holds all its data in-memory. The memory
 * will be occupied until the {@link InMemoryByteData} is garbage collected.
 */
public class InMemoryByteData implements ByteData {
    /** ByteBuffer holding the raw data. */
    private final ByteBuffer data;

    /**
     * Constructor for {@link InMemoryByteData} object.
     *
     * @param data The byte data with which to initialize the {@link InMemoryByteData} object
     */
    public InMemoryByteData(byte[] data) {
        this.data = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Returns the size in bytes of this {@link InMemoryByteData} packet.
     *
     * @return Size in bytes.
     */
    @Override
    public synchronized int size() {
        return this.data.capacity();
    }

    /**
     * Returns the {@link ByteBuffer} that backs this {@link InMemoryByteData} object.
     *
     * @return {@link ByteBuffer} object.
     */
    @Override
    public synchronized ByteBuffer buffer() {
        final ByteBuffer returnBuffer = this.data.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        returnBuffer.position(0);
        return returnBuffer;
    }

    /**
     * Returns the data in this {@link InMemoryByteData} object as byte array. Directly accesses the
     * underlying byte buffer to do so.
     *
     * @return ByteData of this {@link InMemoryByteData} object.
     */
    @Override
    public synchronized byte[] array() {
        return this.data.array();
    }
}
