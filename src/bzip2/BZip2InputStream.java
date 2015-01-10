package bzip2;

import java.io.IOException;
import java.io.InputStream;

public class BZip2InputStream extends InputStream {

    /**
     * The stream from which compressed BZip2 data is read and decoded
     */
    private InputStream inputStream;

    /**
     * An InputStream wrapper that provides bit-level reads
     */
    private BZip2BitInputStream bitInputStream;

    /**
     * If {@code true}, the caller is assumed to have read away the stream's
     * leading "BZ" identifier bytes
     */
    private final boolean headerless;

    /**
     * (@code true} if the end of the compressed stream has been reached,
     * otherwise {@code false}
     */
    private boolean streamComplete = false;

    /**
     * The declared block size of the stream (before final run-length decoding).
     * The final block will usually be smaller, but no block in the stream has
     * to be exactly this large, and an encoder could in theory choose to mix
     * blocks of any size up to this value. Its function is therefore as a hint
     * to the decompressor as to how much working space is sufficient to
     * decompress blocks in a given stream
     */
    private int streamBlockSize;

    /**
     * The merged CRC of all blocks decompressed so far
     */
    private int streamCRC = 0;

    /**
     * The decompressor for the current block
     */
    private BZip2BlockDecompressor blockDecompressor = null;


    /* (non-Javadoc)
     * @see java.io.InputStream#read()
     */
    @Override
    public int read() throws IOException {

        int nextByte = -1;
        if (this.blockDecompressor == null) {
            initialiseStream();
        } else {
            nextByte = this.blockDecompressor.read();
        }

        if (nextByte == -1) {
            if (initialiseNextBlock()) {
                nextByte = this.blockDecompressor.read();
            }
        }

        return nextByte;

    }


    /* (non-Javadoc)
     * @see java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(final byte[] destination, final int offset, final int length) throws IOException {

        int bytesRead = -1;
        if (this.blockDecompressor == null) {
            initialiseStream();
        } else {
            bytesRead = this.blockDecompressor.read(destination, offset, length);
        }

        if (bytesRead == -1) {
            if (initialiseNextBlock()) {
                bytesRead = this.blockDecompressor.read(destination, offset, length);
            }
        }

        return bytesRead;

    }


    /* (non-Javadoc)
     * @see java.io.InputStream#close()
     */
    @Override
    public void close() throws IOException {

        if (this.bitInputStream != null) {
            this.streamComplete = true;
            this.blockDecompressor = null;
            this.bitInputStream = null;

            try {
                this.inputStream.close();
            } finally {
                this.inputStream = null;
            }
        }

    }

    /**
     * Reads the stream header and checks that the data appears to be a valid
     * BZip2 stream
     *
     * @throws IOException if the stream header is not valid
     */
    private void initialiseStream() throws IOException {

        /* If the stream has been explicitly closed, throw an exception */
        if (this.bitInputStream == null) {
            throw new BZip2Exception("Stream closed");
        }

        /* If we're already at the end of the stream, do nothing */
        if (this.streamComplete) {
            return;
        }

        /* Read the stream header */
        try {
            int marker1 = this.headerless ? 0 : this.bitInputStream.readBits(16);
            int marker2 = this.bitInputStream.readBits(8);
            int blockSize = (this.bitInputStream.readBits(8) - '0');

            if ((!this.headerless && (marker1 != BZip2Constants.STREAM_START_MARKER_1))
                    || (marker2 != BZip2Constants.STREAM_START_MARKER_2)
                    || (blockSize < 1) || (blockSize > 9)) {
                throw new BZip2Exception("Invalid BZip2 header");
            }

            this.streamBlockSize = blockSize * 100000;
        } catch (IOException e) {
            // If the stream header was not valid, stop trying to read more data
            this.streamComplete = true;
            throw e;
        }

    }

    /**
     * Prepares a new block for decompression if any remain in the stream. If a
     * previous block has completed, its CRC is checked and merged into the
     * stream CRC. If the previous block was the final block in the stream, the
     * stream CRC is validated
     *
     * @return {@code true} if a block was successfully initialised, or
     * {@code false} if the end of file marker was encountered
     * @throws IOException if either the block or stream CRC check failed, if
     * the following data is not a valid block-header or end-of-file marker, or
     * if the following block could not be decoded
     */
    private boolean initialiseNextBlock() throws IOException {

        /* If we're already at the end of the stream, do nothing */
        if (this.streamComplete) {
            return false;
        }

        /* If a block is complete, check the block CRC and integrate it into the stream CRC */
        if (this.blockDecompressor != null) {
            int blockCRC = this.blockDecompressor.checkCRC();
            this.streamCRC = ((this.streamCRC << 1) | (this.streamCRC >>> 31)) ^ blockCRC;
        }

        /* Read block-header or end-of-stream marker */
        final int marker1 = this.bitInputStream.readBits(24);
        final int marker2 = this.bitInputStream.readBits(24);

        if (marker1 == BZip2Constants.BLOCK_HEADER_MARKER_1 && marker2 == BZip2Constants.BLOCK_HEADER_MARKER_2) {
            // Initialise a new block
            try {
                this.blockDecompressor = new BZip2BlockDecompressor(this.bitInputStream, this.streamBlockSize);
            } catch (IOException e) {
                // If the block could not be decoded, stop trying to read more data
                this.streamComplete = true;
                throw e;
            }
            return true;
        } else if (marker1 == BZip2Constants.STREAM_END_MARKER_1 && marker2 == BZip2Constants.STREAM_END_MARKER_2) {
            // Read and verify the end-of-stream CRC
            this.streamComplete = true;
            final int storedCombinedCRC = this.bitInputStream.readInteger();
            if (storedCombinedCRC != this.streamCRC) {
                throw new BZip2Exception("BZip2 stream CRC error");
            }
            return false;
        }

        /* If what was read is not a valid block-header or end-of-stream marker, the stream is broken */
        this.streamComplete = true;
        throw new BZip2Exception("BZip2 stream format error");

    }

    /**
     * @param inputStream The InputStream to wrap
     * @param headerless If {@code true}, the caller is assumed to have read
     * away the stream's leading "BZ" identifier bytes
     */
    public BZip2InputStream(final InputStream inputStream, final boolean headerless) {

        if (inputStream == null) {
            throw new IllegalArgumentException("Null input stream");
        }

        this.inputStream = inputStream;
        this.bitInputStream = new BZip2BitInputStream(inputStream);
        this.headerless = headerless;

    }

}
