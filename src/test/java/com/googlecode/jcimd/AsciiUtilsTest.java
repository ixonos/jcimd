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
package com.googlecode.jcimd;


import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AsciiUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void serializeAsAsciiBytes() throws Exception {
		final String data = "abc123";
		final Charset asciiCharset = Charset.forName("US-ASCII");
		final int loops = 1000;
		ByteArrayOutputStream out = new ByteArrayOutputStream(10);
		byte[] expecteds = new byte[] { 'a', 'b', 'c', '1', '2', '3' };
		long start = 0;

		start = System.nanoTime();
		for (int i = 0; i < loops; i++) {
			AsciiUtils.writeStringAsAsciiBytes(data, out);
			out.flush();
			assertArrayEquals(expecteds, out.toByteArray());
			out.reset();
		}
		System.out.println("AsciiUtils#writeStringAsAsciiBytes() elapsed time: "
				+ ((System.nanoTime() - start) / 1000000) + " ms");

		start = System.nanoTime();
		for (int i = 0; i < loops; i++) {
			out.write(data.getBytes(asciiCharset));
			out.flush();
			assertArrayEquals(expecteds, out.toByteArray());
			out.reset();
		}
		System.out.println("String#getBytes() elapsed time: "
				+ ((System.nanoTime() - start) / 1000000) + " ms");
	}

	@Test
	public void serializeAsHexAsciiBytes() throws Exception {
		final byte[] bytes = new byte[] { 
				(byte) 0x00, (byte) 0x01, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF
		};
		ByteArrayOutputStream out = new ByteArrayOutputStream(20);
		AsciiUtils.writeByteArrayAsHexAsciiBytes(bytes, out);
		final byte[] expecteds = new byte[] {
				'0', '0', '0', '1', 'a', 'b', 'c', 'd', 'e', 'f'
		};
		assertArrayEquals(expecteds, out.toByteArray());
	}

	@Test
	public void serializeIntAsAsciiBytesLeftPaddedWithTwoZeroes() throws Exception {
		final int in = 1;
		ByteArrayOutputStream out = new ByteArrayOutputStream(20);
		AsciiUtils.writeIntAsAsciiBytes(in, out, 2);
		final byte[] expecteds = new byte[] { '0', '1' };
		assertArrayEquals(expecteds, out.toByteArray());
	}

	@Test
	public void serializeIntAsAsciiBytesLeftPaddedWithThreeZeroes() throws Exception {
		final int in = 2;
		ByteArrayOutputStream out = new ByteArrayOutputStream(20);
		AsciiUtils.writeIntAsAsciiBytes(in, out, 3);
		final byte[] expecteds = new byte[] { '0', '0', '2' };
		assertArrayEquals(expecteds, out.toByteArray());
	}

	@Test
	public void convertByteArrayToHexString() throws Exception {
		final byte[] bytes = new byte[] { 
				(byte) 0x00, (byte) 0x01, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF
		};
		assertEquals("0001abcdef", AsciiUtils.byteArrayToHexString(bytes));
	}
}
