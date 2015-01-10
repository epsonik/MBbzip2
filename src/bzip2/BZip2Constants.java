package bzip2;


/**
 * BZip2 constants shared between the compressor and decompressor
 */
interface BZip2Constants {

	/**
	 * First three bytes of the block header marker
	 */
	static final int BLOCK_HEADER_MARKER_1 = 0x314159;

	/**
	 * Last three bytes of the block header marker
	 */
	static final int BLOCK_HEADER_MARKER_2 = 0x265359;

	/**
	 * Number of symbols decoded after which a new Huffman table is selected
	 */
	static final int HUFFMAN_GROUP_RUN_LENGTH = 50;

	/**
	 * Maximum possible Huffman alphabet size
	 */
	static final int HUFFMAN_MAXIMUM_ALPHABET_SIZE = 258;

	/**
	 * The longest Huffman code length created by the encoder
	 */
	static final int HUFFMAN_ENCODE_MAXIMUM_CODE_LENGTH = 20;

	/**
	 * The longest Huffman code length accepted by the decoder
	 */
	static final int HUFFMAN_DECODE_MAXIMUM_CODE_LENGTH = 23;

	/**
	 * Minimum number of alternative Huffman tables
	 */
	static final int HUFFMAN_MINIMUM_TABLES = 2;

	/**
	 * Maximum number of alternative Huffman tables
	 */
	static final int HUFFMAN_MAXIMUM_TABLES = 6;

	/**
	 * Maximum possible number of Huffman table selectors
	 */
	static final int HUFFMAN_MAXIMUM_SELECTORS = (900000 / HUFFMAN_GROUP_RUN_LENGTH) + 1;

	/**
	 * Huffman symbol used for run-length encoding
	 */
	static final int HUFFMAN_SYMBOL_RUNA = 0;

	/**
	 * Huffman symbol used for run-length encoding
	 */
	static final int HUFFMAN_SYMBOL_RUNB = 1;

	/**
	 * First three bytes of the end of stream marker
	 */
	static final int STREAM_END_MARKER_1 = 0x177245;

	/**
	 * Last three bytes of the end of stream marker
	 */
	static final int STREAM_END_MARKER_2 = 0x385090;

	/**
	 * 'B' 'Z' that marks the start of a BZip2 stream
	 */
	static final int STREAM_START_MARKER_1 = 0x425a;

	/**
	 * 'h' that distinguishes BZip from BZip2
	 */
	static final int STREAM_START_MARKER_2 = 0x68;

}
