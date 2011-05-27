/*
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.googlecode.jcimd;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Provides static methods that write {@link String strings} and
 * {@link Number numbers} as <a href="http://en.wikipedia.org/wiki/ASCII">
 * ASCII</a> bytes to an {@link OutputStream output stream}.
 * <p>
 * This was provided since using {@link String#getBytes()} is slower
 * and requires a temporary byte array to be created, and
 * {@link Integer#toString()} followed by {@link String#getBytes()}
 * is slower. Also, using an {@link OutputStreamWriter output stream
 * writer} (with ASCII character set) is not any faster either. 
 *
 * @author Lorenzo Dee
 */
public final class AsciiUtils {

	public static final byte ZERO_ASCII_BYTE_VALUE = '0'; // 48

	/**
	 * Writes the given {@link String string} as ASCII bytes (0-127)
	 * to the given {@link OutputStream output stream}.
	 * <p>
	 * This method ignores the 24 high-order bits of the characters
	 * in the given string. It does not check if the characters in
	 * the string are indeed ASCII characters or not. It <em>assumes</em>
	 * that the given string only contains valid ASCII characters.
	 *
	 * @param in the given string
	 * @param out the output stream
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeStringAsAsciiBytes(
			String in, OutputStream out) throws IOException {
		final int length = in.length();
		for (int i = 0; i < length; i++) {
			// The byte to be written is the eight low-order bits
			// of charAt(i). The 24 high-order bits are ignored.
			out.write(in.charAt(i));
		}
	}

	private static final byte[] HEX_DIGITS = new byte[] {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};

	/**
	 * Writes the given byte array as hexadecimal ASCII bytes
	 * ('0'-'9', 'a'-'f') to the output stream.
	 *
	 * @param bytes the given byte array
	 * @param out the output stream
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeByteArrayAsHexAsciiBytes(
			byte[] bytes, OutputStream out) throws IOException {
		for (byte b : bytes) {
			out.write(HEX_DIGITS[(b & 0xf0) >> 4]);
			out.write(HEX_DIGITS[(b & 0x0f)]);
		}
	}

	public static String byteArrayToHexString(byte[] bytes) {
		StringBuilder s = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			s.append((char) HEX_DIGITS[(b & 0xf0) >> 4]);
			s.append((char) HEX_DIGITS[(b & 0x0f)]);
		}
		return s.toString();
	}

	private static final int[] SIZE_TABLE = new int[] {
		1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000 
	};

	/**
	 * Writes the given integer as ASCII characters ('0'-'9') to the
	 * given output stream. Left pads with '0' (zeroes) to achieve the
	 * given width. For example,
	 * <p>
	 * <pre>
	 * int x = 99;
	 * AsciiUtils.writeIntAsAsciiBytes(x, out, 3);
	 * // writes '0', '9', '9' to out 
	 * </pre>
	 * <p>
	 * If the given width is shorter than the given integer (e.g.
	 * integer is 1000, and width is 3), only the last n-digits
	 * will be written to the output stream (in this case, only
	 * zeroes are written).
	 *
	 * @param x the given integer
	 * @param out the output stream
	 * @param width the given width
	 * @throws IOException if an I/O error occurs
	 */
	public static void writeIntAsAsciiBytes(
			int x, OutputStream out, int width) throws IOException {
		int size;
		while (width > 0) {
			size = SIZE_TABLE[width - 1];
			out.write(ZERO_ASCII_BYTE_VALUE + (x / size));
			width--;
			if (width > 0) {
				x = x % size;
			}
		}
	}

}
