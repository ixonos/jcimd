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
		UserData ud = TextMessageUserDataFactory.newInstance("abc123 {curly}[square] brackets \u00FC");
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
	public void createsUtf16BigEndianEncodedUserData() throws Exception {
		UserData ud = TextMessageUserDataFactory.newInstance("\u4F60\u597D");
		assertNotNull(ud);
		assertEquals(0x08, ud.getDataCodingScheme());
		assertNull(ud.getHeader());
		assertTrue(ud.isBodyBinary());
		assertArrayEquals(new byte[] { 0x4F, 0x60, 0x59, 0x7D }, ud.getBinaryBody());
	}

}
