package org.vitrivr.cineast.core.data.raw.bytes;

import java.nio.ByteBuffer;


/**
 * This interface represents an object that holds arbitrary, immutable byte data that can be stored in a ByteBuffer.
 * The kind of data is implementation specific as are the patterns used to access the data.
 */
public interface ByteData {
    /**
     * Returns the size in bytes of this {@link ByteData} object.
     *
     * @return Size in bytes.
     */
    int size();
    
    /**
     * Returns the {@link ByteBuffer} that backs this {@link ByteData} object. The {@link ByteBuffer} is supposed to be
     * read-only and it's position is supposed to be 0.
     *
     * @return Read-only {@link ByteBuffer}.
     */
    ByteBuffer buffer();

    /**
     * Returns the data in this {@link ByteData} object as byte array.
     *
     * @return ByteData of this {@link ByteData} object.
     */
    byte[] array();
}


