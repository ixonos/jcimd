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


import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jcimd.ApplicationPacketSequenceNumberGenerator;
import com.googlecode.jcimd.PacketSequenceNumberGenerator;
import com.googlecode.jcimd.SmsCenterPacketSequenceNumberGenerator;


public class PacketSequenceNumberGeneratorTest {

	private PacketSequenceNumberGenerator appGenerator;
	private PacketSequenceNumberGenerator smscGenerator;

	@Before
	public void setUp() throws Exception {
		this.appGenerator = new ApplicationPacketSequenceNumberGenerator();
		this.smscGenerator = new SmsCenterPacketSequenceNumberGenerator();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void generatesOddNumbersAndRollsBackToOne() throws Exception {
		for (int i = 1; i <= 255; i += 2) {
			assertEquals(i, this.appGenerator.nextSequence());
		}
		assertEquals(1, this.appGenerator.nextSequence());
	}

	@Test
	public void generatesEvenNumbersAndRollsBackToZero() throws Exception {
		for (int i = 0; i <= 255; i += 2) {
			assertEquals(i, this.smscGenerator.nextSequence());
		}
		assertEquals(0, this.smscGenerator.nextSequence());
	}

}
