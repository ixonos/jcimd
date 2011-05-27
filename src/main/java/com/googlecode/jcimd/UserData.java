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

/**
 * Strategy interface for user data.
 * <p>
 * If {@link #isBodyBinary()} returns <code>true</code>,
 * {@link #getBinaryBody()} shall be used to retrieve user data
 * and placed under parameter 34.
 * Otherwise, {@link #getBody()} shall be used to retrieve user data
 * and placed under parameter 33.
 * <p>
 * The header (as returned by {@link #getHeader()} (if not
 * <code>null</code>) is placed under user data header (parameter
 * number 32).
 *
 * @author Lorenzo Dee
 */
public interface UserData {

	/**
	 * The returned byte array (if not <code>null</code>) is added
	 * as parameter 32 (treated as hexadecimal parameter type which is
	 * converted to hexadecimal string).
	 * @return the user data header
	 */
	byte[] getHeader();

	// TODO: Always return bytes. This interface should be responsible for doing so.
	// 0000 0000
	//   ^  ^
	//   |  |
	//   |  +-- Bits 3 and 2:
	//   |        00 = Default alphabet (GSM)
	//   |        01 = 8-bit data
	//   |        10 = UCS2 (16-bit)
	//   |        11 = Reserved
	//   |
	//   +-- Bit 5: 0 = not compressed; 1 = compressed (160 septets packed into 140 octets)

	// #getDataCodingScheme() can return 0x20 for 160 septets packed into 140 octets
	
	/*
	 * class StringUserData
	 * + automatically encodes to compressed GSM (default alphabet)
	 * class UncompressedStringUserData
	 * + automatically encodes to uncompressed GSM (default alphabet)
	 */

	int getDataCodingScheme();

	/**
	 * Returns <code>true</code> if user data contains
	 * binary data.
	 * @return <code>true</code> if user data contains
	 * binary data.
	 */
	boolean isBodyBinary();

	/**
	 * Returns the string user data. Called only if
	 * {@link #isBodyBinary()} returns <code>false</code>.
	 * <p>
	 * The returned string is added as parameter 33.
	 * @return the string user data
	 */
	String getBody();

	/**
	 * Returns the binary user data. Called only if
	 * {@link #isBodyBinary()} returns <code>true</code>.
	 * <p>
	 * The returned byte array is added as parameter 34
	 * (treated as hexadecimal parameter type which is
	 * converted to hexadecimal string).
	 * @return the binary user data
	 */
	byte[] getBinaryBody();

}
