package com.googlecode.jcimd.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * 
 * @author Lorenzo Dee
 */
public class Gsm7BitPackedCharset extends Charset {

	private final char[] BYTE_TO_CHAR;
	private final char[] BYTE_TO_ESCAPED_CHAR;
	private final int[] CHAR_TO_BYTE;

	protected Gsm7BitPackedCharset(
			String canonicalName, String[] aliases,
			char[] byteToChar, int[] charToByte, char[] byteToEscapedChar) {
		super(canonicalName, aliases);
		this.BYTE_TO_CHAR = byteToChar;
		this.CHAR_TO_BYTE = charToByte;
		this.BYTE_TO_ESCAPED_CHAR = byteToEscapedChar;
	}

	@Override
	public boolean contains(Charset cs) {
		return this.getClass().isInstance(cs);
	}

	@Override
	public CharsetDecoder newDecoder() {
		return new Decoder7Bit(this);
	}

	@Override
	public CharsetEncoder newEncoder() {
		return new Encoder7Bit(this);
	}

	protected class Encoder7Bit extends CharsetEncoder {

		private int data = 0;
		private int nBits = 0;

		protected Encoder7Bit(Charset charset) {
			// 7 bits for unescaped characters
			// 14 bits for escaped characters
			// average bits per character is 10.5
			// that's about 1.3 bytes per character
			super(charset, (7+14/2f)/8f, 2f);
		}

		@Override
		protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
			int remaining = in.remaining();
			while (remaining > 0) {
				if (out.remaining() < 1) {
					return CoderResult.OVERFLOW;
				}
				char ch = in.get();
				int b = CHAR_TO_BYTE[ch];
				if (b == GsmCharsetProvider.NO_GSM_BYTE) {
					// If ch does not map to a GSM character, replace with a '?'
					b = '?';
				}
				byte highByte = (byte) ((b >> 8) & 0xFF);
				if (highByte > 0) {
					data |= (highByte << nBits);
					nBits += 7;
				}
				data |= ((b & 0xFF) << nBits);
				nBits += 7;
				while (nBits >= 8) {
					out.put((byte) (data & 0xFF));
					data >>>= 8;
					nBits -= 8;
				}
				remaining--;
			}
			if (nBits > 0) {
				if (out.remaining() < 1) {
					return CoderResult.OVERFLOW;
				}
				out.put((byte) (data & 0xFF));
			}
			return CoderResult.UNDERFLOW;
		}
		
	}

	protected class Decoder7Bit extends CharsetDecoder {

		private int data = 0;
		private int nBits = 0;

		protected Decoder7Bit(Charset charset) {
			super(charset, 8/7f, 2f);
		}

		@Override
		protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
			boolean escaped = false;
			int remaining = in.remaining();
			while (remaining > 0) {
				if (out.remaining() < 1) {
					return CoderResult.OVERFLOW;
				}
				byte bite = in.get();
				data |= (bite & 0xFF) << nBits;
				nBits += 8;
				while (nBits >= 7) {
					int i = data & 0x7F;
					if (i != GsmCharsetProvider.ESCAPE) {
						if (escaped) {
							char escapedChar = BYTE_TO_ESCAPED_CHAR[i];
							if (escapedChar != GsmCharsetProvider.NO_GSM_BYTE) {
								out.put(escapedChar);
							} else {
								// If invalid escape sequence use SPACE
								out.put(' ');
								out.put(BYTE_TO_CHAR[i]);
							}
							escaped = false;
						} else {
							out.put(BYTE_TO_CHAR[i]);
						}
					} else {
						escaped = true;
					}
					data >>>= 7;
					nBits -= 7;
				}
				remaining--;
			}
			return CoderResult.UNDERFLOW;
		}

	}

}
