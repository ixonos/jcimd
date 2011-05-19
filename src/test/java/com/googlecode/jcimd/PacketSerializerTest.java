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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.googlecode.jcimd.ApplicationPacketSequenceNumberGenerator;
import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.PacketSerializer;
import com.googlecode.jcimd.Parameter;

public class PacketSerializerTest {

	public static final byte STX = 0x02;
	public static final byte TAB = '\t'; // ASCII value is 9
	public static final byte COLON = ':'; // ASCII value is 58
	public static final byte ETX = 0x03;
	
	private PacketSerializer serializer;

	@Before
	public void setUp() throws Exception {
		serializer = new PacketSerializer();
		serializer.setSequenceNumberGenerator(
				new ApplicationPacketSequenceNumberGenerator());
	}

	@After
	public void tearDown() throws Exception {
	}

	protected int calculateCheckSum(byte[] bytes) {
		int sum = 0;
		for (byte b : bytes) {
			sum += b;
			sum &= 0xFF;
		}
		return sum;
	}

	@Test
	public void serializeLoginCommand() throws Exception {
		Packet command = new Packet(1,
				new Parameter(10, "username"),
				new Parameter(11, "password"));
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte expected[] = new byte[] {
				STX, '0', '1', COLON, '0', '0', '1', TAB,
				'0', '1', '0', COLON, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e', TAB,
				'0', '1', '1', COLON, 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', TAB,
				0, 0, /* two-bytes for checksum */
				ETX };
		int checksum = calculateCheckSum(Arrays.copyOfRange(expected, 0, expected.length - 3));
		String checkSumHexString = Integer.toHexString(checksum).toUpperCase();
		expected[expected.length - 3] = (byte) checkSumHexString.charAt(0);
		expected[expected.length - 2] = (byte) checkSumHexString.charAt(1);
		serializer.serialize(command, outputStream);
		assertArrayEquals(expected, outputStream.toByteArray());
	}

	@Test
	public void deserializeLoginCommand() throws Exception {
		byte[] bytes = new byte[] {
				STX, '0', '1', COLON, '0', '0', '1', TAB,
				'0', '1', '0', COLON, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e', TAB,
				'0', '1', '1', COLON, 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', TAB,
				0, 0, /* two-bytes for checksum */
				ETX };
		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
		String checkSumHexString = Integer.toHexString(checksum).toUpperCase();
		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		Packet command = new Packet(1, 1,
				new Parameter(10, "username"),
				new Parameter(11, "password"));
		Packet actual = serializer.deserialize(inputStream);
		assertEquals(command, actual);
		System.out.println(actual);
	}

	@Test
	public void deserializeLoginCommandWithExtraDataBetweenPackets() throws Exception {
		byte[] bytes = new byte[] {
				'n', 'o', 'i', 's', 'e',
				STX, '0', '1', COLON, '0', '0', '1', TAB,
				'0', '1', '0', COLON, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e', TAB,
				'0', '1', '1', COLON, 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', TAB,
				0, 0, /* two-bytes for checksum */
				ETX, 'n', 'o', 'i', 's', 'e' };
		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 5, bytes.length - 8));
		String checkSumHexString = Integer.toHexString(checksum).toUpperCase();
		bytes[bytes.length - 8] = (byte) checkSumHexString.charAt(0);
		bytes[bytes.length - 7] = (byte) checkSumHexString.charAt(1);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		Packet command = new Packet(1, 1,
				new Parameter(10, "username"),
				new Parameter(11, "password"));
		Packet actual = serializer.deserialize(inputStream);
		assertEquals(command, actual);
		System.out.println(actual);
	}

	@Test(expected=IOException.class)
	public void deserializeCommandWithMissingColon() throws Exception {
		byte[] bytes = new byte[] {
				STX, '0', '1', TAB, '0', '0', '1', TAB,
				'0', '1', '0', COLON, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e', TAB,
				'0', '1', '1', COLON, 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', TAB,
				0, 0, /* two-bytes for checksum */
				ETX };
		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
		String checkSumHexString = Integer.toHexString(checksum).toUpperCase();
		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		try {
			serializer.deserialize(inputStream);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	@Test(expected=IOException.class)
	public void deserializeCommandWithFourByteParameterType() throws Exception {
		byte[] bytes = new byte[] {
				STX, '0', '1', COLON, '0', '0', '1', TAB,
				'0', '1', '0', '0', COLON, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e',
				'0', '1', '1', COLON, 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', TAB,
				0, 0, /* two-bytes for checksum */
				ETX };
		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
		String checkSumHexString = Integer.toHexString(checksum).toUpperCase();
		bytes[bytes.length - 3] = (byte) checkSumHexString.charAt(0);
		bytes[bytes.length - 2] = (byte) checkSumHexString.charAt(1);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		try {
			serializer.deserialize(inputStream);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	@Test(expected=IOException.class)
	public void deserializeCommandWithNonHexCheckSum() throws Exception {
		byte[] bytes = new byte[] {
				STX, '0', '1', COLON, '0', '0', '1', TAB,
				'0', '1', '0', '0', COLON, 'u', 's', 'e', 'r', 'n', 'a', 'm', 'e',
				'0', '1', '1', COLON, 'p', 'a', 's', 's', 'w', 'o', 'r', 'd', TAB,
				0, 0, /* two-bytes for checksum */
				ETX };
		int checksum = calculateCheckSum(Arrays.copyOfRange(bytes, 0, bytes.length - 3));
		bytes[bytes.length - 3] = (byte) (checksum & 0xFF00 >> 8);
		bytes[bytes.length - 2] = (byte) (checksum & 0x00FF);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		try {
			serializer.deserialize(inputStream);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

}
