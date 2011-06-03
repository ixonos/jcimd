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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TextMessageUserDataFactoryTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void createsGsm7BitEncodedUserData() throws Exception {
		UserData[] uds = TextMessageUserDataFactory.newInstance("abc123 {curly}[square] brackets \u00FC");
		assertNotNull(uds);
		assertEquals(1, uds.length);
		UserData ud = uds[0];
		assertNotNull(ud);
		assertEquals(0x00, ud.getDataCodingScheme());
		assertNull(ud.getHeader());
		assertTrue(ud.isBodyBinary());
		assertArrayEquals(new byte[] {
			(byte) 0x61,
			(byte) 0xf1,
			(byte) 0x38,
			(byte) 0x26,
			(byte) 0x9b,
			(byte) 0x81,
			(byte) 0x36,
			(byte) 0xa8,
			(byte) 0x71,
			(byte) 0x5d,
			(byte) 0xce,
			(byte) 0xce,
			(byte) 0x6f,
			(byte) 0x52,
			(byte) 0x1b,
			(byte) 0xde,
			(byte) 0x3c,
			(byte) 0x5e,
			(byte) 0x0f,
			(byte) 0xcb,
			(byte) 0xcb,
			(byte) 0x1b,
			(byte) 0x1f,
			(byte) 0x48,
			(byte) 0x2c,
			(byte) 0x0f,
			(byte) 0x8f,
			(byte) 0xd7,
			(byte) 0x65,
			(byte) 0xfa,
			(byte) 0x1c,
			(byte) 0xe4,
			(byte) 0x07
		}, ud.getBinaryBody());
	}

	@Test
	public void createsUtf16EncodedUserDataWhenTextMessageContainsNonGsmCharacters()
	throws Exception {
		UserData[] uds = TextMessageUserDataFactory.newInstance("\u4F60\u597D");
		assertNotNull(uds);
		assertEquals(1, uds.length);
		UserData ud = uds[0];
		assertNotNull(ud);
		assertEquals(0x08, ud.getDataCodingScheme());
		assertNull(ud.getHeader());
		assertTrue(ud.isBodyBinary());
		assertArrayEquals(new byte[] { 0x4F, 0x60, 0x59, 0x7D }, ud.getBinaryBody());
	}

	@Test
	public void createsConcatenatedMessageUsingUserDataHeader() throws Exception {
		UserData[] uds = TextMessageUserDataFactory.newInstance("first part" + ", 2nd part", 15);
		byte[] udh = null;
		assertNotNull(uds);
		assertEquals(2, uds.length);
		assertNotNull(uds[0].getHeader());
		udh = uds[0].getHeader();
		assertEquals(6, udh.length);
		assertEquals(0x05, udh[0]);
		assertEquals(0x00, udh[1]);
		assertEquals(0x03, udh[2]);
		//assertEquals(0x00, udh[3]); // reference number
		assertEquals(0x02, udh[4]); // total number of parts
		assertEquals(0x01, udh[5]); // part's number in the sequence
		// TODO: Assert body contents
		assertTrue(uds[0].isBodyBinary());
		assertTrue(uds[0].getBinaryBody().length <= 9);
		// assertArrayEquals(new byte[] {}, uds[0].getBinaryBody());

		assertNotNull(uds[1].getHeader());
		udh = uds[1].getHeader();
		assertEquals(6, udh.length);
		assertEquals(0x05, udh[0]);
		assertEquals(0x00, udh[1]);
		assertEquals(0x03, udh[2]);
		//assertEquals(0x00, udh[3]); // reference number
		assertEquals(0x02, udh[4]); // total number of parts
		assertEquals(0x02, udh[5]); // part's number in the sequence
		// TODO: Assert body contents
		assertTrue(uds[1].isBodyBinary());
		assertTrue(uds[1].getBinaryBody().length <= 9);
		// assertArrayEquals(new byte[] {}, uds[1].getBinaryBody());
	}

	@Test
	public void createsConcatenatedUnicodeMessage() throws Exception {
		final String textMessage = "\u5B6B\u5B50\u5175\u6CD5 \u8A08\u7BC7\u7B2C\u4E00 \u5B6B\u5B50\u66F0\uFF1A\u5175\u8005\uFF0C\u570B\u4E4B\u5927\u4E8B\uFF0C\u6B7B\u751F\u4E4B\u5730\uFF0C\u5B58\u4EA1\u4E4B\u9053\uFF0C\u4E0D\u53EF\u4E0D\u5BDF\u4E5F\u3002\u6545\u7D93\u4E4B\u4EE5\u4E94\uFF0C\u6821\u4E4B\u4EE5\u8A08\uFF0C\u800C\u7D22\u5176\u60C5\uFF1A\u4E00\u66F0\u9053\uFF0C\u4E8C\u66F0\u5929\uFF0C\u4E09\u66F0\u5730\uFF0C\u56DB\u66F0 \u5C07\uFF0C\u4E94\u66F0\u6CD5\u3002\u9053\u8005\uFF0C\u4EE4\u6C11\u4E8E\u4E0A\u540C\u610F\u8005\u4E5F\uFF0C\u53EF\u8207\u4E4B\u6B7B\uFF0C\u53EF\u8207\u4E4B\u751F\uFF0C\u6C11\u4E0D \u8A6D\u4E5F\u3002\u5929\u8005\uFF0C\u9670\u967D\u3001\u5BD2\u6691\u3001\u6642\u5236\u4E5F\u3002\u5730\u8005\uFF0C\u9AD8\u4E0B\u3001\u9060\u8FD1\u3001\u96AA\u6613\u3001\u5EE3\u72F9 \u3001\u6B7B\u751F\u4E5F\u3002\u5C07\u8005\uFF0C\u667A\u3001\u4FE1\u3001\u4EC1\u3001\u52C7\u3001\u56B4\u4E5F\u3002\u6CD5\u8005\uFF0C\u66F2\u5236\u3001\u5B98\u9053\u3001\u4E3B\u7528 \u4E5F\u3002\u51E1\u6B64\u4E94\u8005\uFF0C\u5C07\u83AB\u4E0D\u805E\uFF0C\u77E5\u4E4B\u8005\u52DD\uFF0C\u4E0D\u77E5\u4E4B\u8005\u4E0D\u52DD\u3002\u6545\u6821\u4E4B\u4EE5\u8A08\uFF0C \u800C\u7D22\u5176\u60C5\u3002\u66F0\uFF1A\u4E3B\u5B70\u6709\u9053\uFF1F\u5C07\u5B70\u6709\u80FD\uFF1F\u5929\u5730\u5B70\u5F97\uFF1F\u6CD5\u4EE4\u5B70\u884C\uFF1F\u5175\u773E\u5B70 \u5F37\uFF1F\u58EB\u5352\u5B70\u7DF4\uFF1F\u8CDE\u7F70\u5B70\u660E\uFF1F\u543E\u4EE5\u6B64\u77E5\u52DD\u8CA0\u77E3\u3002";
		UserData[] uds = TextMessageUserDataFactory.newInstance(textMessage, 140);
		assertEquals(4, uds.length);
	}

	@Test
	public void countUtf16Bytes() throws Exception {
		BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("utf8-samples.txt"), "UTF-8"));
		try {
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = r.readLine()) != null) {
				sb.append(line);
			}
			System.out.println(sb.toString().length());
			final String textMessage = sb.toString();
			System.out.println(textMessage.length());
			System.out.println(Character.codePointCount(textMessage, 0, textMessage.length()) * 2);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Writer w = new OutputStreamWriter(out, "UTF-16BE");
			try {
				w.write(textMessage);
			} finally {
				w.close();
			}
			System.out.println(out.toByteArray().length);
		} finally {
			r.close();
		}
	}
}
