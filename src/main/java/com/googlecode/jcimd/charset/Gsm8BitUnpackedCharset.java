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
public class Gsm8BitUnpackedCharset extends Charset {

	private final char[] BYTE_TO_CHAR;
	private final char[] BYTE_TO_ESCAPED_CHAR;
	private final int[] CHAR_TO_BYTE;

	protected Gsm8BitUnpackedCharset(
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
		return new Decoder8Bit(this);
	}

	@Override
	public CharsetEncoder newEncoder() {
		return new Encoder8Bit(this);
	}

	protected class Encoder8Bit extends CharsetEncoder {

		protected Encoder8Bit(Charset charset) {
			super(charset, 1.5f, 2f);
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
					out.put((byte) 0x3F); // question mark
				} else {
					byte highByte = (byte) ((b >> 8) & 0xFF);
					if (highByte > 0) {
						out.put(highByte);
					}
					out.put((byte) (b & 0xFF));
				}
				remaining--;
			}
			return CoderResult.UNDERFLOW;
		}
		
	}

	protected class Decoder8Bit extends CharsetDecoder {

		protected Decoder8Bit(Charset charset) {
			super(charset, 1.5f, 2f);
		}

		@Override
		protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
			int remaining = in.remaining();
			while (remaining > 0) {
				if (out.remaining() < 1) {
					return CoderResult.OVERFLOW;
				}
				byte bite = in.get();
				if (bite > 0x7F) {
					return CoderResult.malformedForLength(1);
				}
				if (bite == GsmCharsetProvider.ESCAPE) {
					if (remaining < 1) {
						return CoderResult.OVERFLOW;
					}
					bite = in.get();
					remaining--;
					// TODO: What if this is an invalid escape sequence?
					out.put(BYTE_TO_ESCAPED_CHAR[bite & 0x7F]);
				} else {
					out.put(BYTE_TO_CHAR[bite & 0x7F]);
				}
				remaining--;
			}
			return CoderResult.UNDERFLOW;
		}

	}

}
