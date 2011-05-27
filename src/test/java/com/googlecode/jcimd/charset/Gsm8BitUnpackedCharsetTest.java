/*
 * Copyright 2010-2011 the original author or authors.
 *
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
package com.googlecode.jcimd.charset;


import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Gsm8BitUnpackedCharsetTest {

	private Charset charset;
	private CharsetDecoder decoder;
	private CharsetEncoder encoder;

	@Before
	public void setUp() throws Exception {
		charset = Charset.forName("GSM-8BIT");
		decoder = charset.newDecoder();
		encoder = charset.newEncoder();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void decodesAlphaNumericCharacters() throws Exception {
		ByteBuffer byteBuffer = ByteBuffer.wrap(
				"It is easy to send text messages. 123".getBytes());
		CharBuffer charBuffer = decoder.decode(byteBuffer);
		assertEquals("It is easy to send text messages. 123", charBuffer.toString());
	}

	@Test
	public void decodesNonAlphaNumericCharacters() throws Exception {
		byte[] bytes = new byte[] { 0x00, 0x02 };
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		CharBuffer charBuffer = decoder.decode(byteBuffer);
		assertEquals("@$", charBuffer.toString());
	}

	@Test
	public void decodesEscapedCharacters() throws Exception {
		byte[] bytes = new byte[] {
				(byte) 0x1B, (byte) 0x28, (byte) 0x1B, (byte) 0x29,
				(byte) 0x1B, (byte) 0x3C, (byte) 0x1B, (byte) 0x3E
			};
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		CharBuffer charBuffer = decoder.decode(byteBuffer);
		assertEquals("{}[]", charBuffer.toString());
	}

	@Test
	public void decodesInvalidEscapeSequence() throws Exception {
		// 0x1B 0x48 0x1B 0x4A
		// (esc) 'H' (esc) 'J'
		// Escape (0x1B) should be followed by a valid character.
		// If not, 0x1B is decoded as SPACE, and the following byte
		// is treated as a non-escaped sequence.
		byte[] bytes = new byte[] {
				(byte) 0x1B, (byte) 'H', (byte) 0x1B, (byte) 'J'
			};
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		CharBuffer charBuffer = decoder.decode(byteBuffer);
		assertEquals(" H J", charBuffer.toString());
	}

	@Test
	public void encodesAlphaNumericCharacters() throws Exception {
		String string = "It is easy to send text messages. 123";
		CharBuffer in = CharBuffer.wrap(string);
		byte expecteds[] = string.getBytes();
		byte actuals[] = encoder.encode(in).array();
		for (int i = 0; i < expecteds.length; i++) {
			assertEquals("at element " + i, expecteds[i], actuals[i]);
		}
	}

	@Test
	public void encodesNonAlphaNumericCharacters() throws Exception {
		String string = "@$";
		CharBuffer in = CharBuffer.wrap(string);
		byte expecteds[] = new byte[] {
				(byte) 0x00, (byte) 0x02
			};
		byte actuals[] = encoder.encode(in).array();
		for (int i = 0; i < expecteds.length; i++) {
			assertEquals("at element " + i, expecteds[i], actuals[i]);
		}
	}

	@Test
	public void encodesEscapedCharacters() throws Exception {
		String string = "{}[]";
		CharBuffer in = CharBuffer.wrap(string);
		byte expecteds[] = new byte[] {
				(byte) 0x1B, (byte) 0x28, (byte) 0x1B, (byte) 0x29,
				(byte) 0x1B, (byte) 0x3C, (byte) 0x1B, (byte) 0x3E
			};
		// As these are ALL escaped characters, we allocate our own byte buffer.
		// Otherwise, the average of 1.5 bytes per character is NOT enough.
		ByteBuffer out = ByteBuffer.allocate(string.length() * 2);
		encoder.encode(in, out, true);
		byte actuals[] = out.array();
		for (int i = 0; i < expecteds.length; i++) {
			assertEquals("at element " + i, expecteds[i], actuals[i]);
		}
	}

	@Test
	public void usesQuestionMarkToEncodeCharactersOutsideGsmRange() throws Exception {
		CharBuffer in = CharBuffer.wrap("\u604F\u7D59");
		ByteBuffer out = ByteBuffer.allocate(2);
		encoder.encode(in, out, true);
		assertEquals("??", new String(out.array()));
	}

	@Test
	public void encodeAndDecode() throws Exception {
		CharBuffer charBuffer1 = CharBuffer.wrap("abc123{}[]");
		CharBuffer charBuffer2 = decoder.decode(encoder.encode(charBuffer1));
		assertEquals("abc123{}[]", charBuffer2.toString());
	}

}
