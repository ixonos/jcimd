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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jcimd.charset.GsmCharsetProvider;

public class DetectGsmCharactersTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void detectsGsmCharacters() throws Exception {
		final String s = "{}[]\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u03A3\u0398\u039E";
		assertTrue(GsmCharsetProvider.noNonGsmCharacters(s));
	}

	@Test
	public void detectsNonGsmCharacters() throws Exception {
		assertFalse(GsmCharsetProvider.noNonGsmCharacters("" + (char) 0x7f));
		assertFalse(GsmCharsetProvider.noNonGsmCharacters("" + (char) 0x1a));
		assertFalse(GsmCharsetProvider.noNonGsmCharacters("" + (char) 0x1b));
		assertFalse(GsmCharsetProvider.noNonGsmCharacters("" + (char) 0x1b));
		assertFalse(GsmCharsetProvider.noNonGsmCharacters("\u604F\u7D59"));
	}

}
