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
 * Strategy interface to generate packet sequence numbers. Implementations
 * should follow the CIMD specification rules:
 * <ul>
 * <li>
 * Operations <strong>from the application to the SMS Center</strong> are
 * assigned an <em>odd</em> packet number, starting from <em>one</em>.
 * Subsequent packet numbers are incremented by <em>two</em>. After reaching
 * 255, the number wraps back to one again. Using a three-character
 * <code>NNN</code> field, the message packet numbering for a list of
 * application-originated packets is as follows: 001, 003, 005, ... 253,
 * 255, 001, 003, ...
 * </li>
 * <li>
 * Operations <strong>from the SMS Center to the application</strong> are
 * assigned an <em>even</em> packet number starting from <em>zero</em>.
 * Subsequent packet numbers are incremented by <em>two</em>. After reaching
 * 254, the number wraps back to zero again. This means that the message packet
 * numbering for SMS Center-originated packets is as follows: 000, 002, 004,
 * ... 252, 254, 000, 002, 004, ...
 * </li>
 * </ul>
 *
 * @author Lorenzo Dee
 *
 */
public interface PacketSequenceNumberGenerator {

	/**
	 * Returns the next packet sequence number.
	 * @return the next packet sequence number
	 */
	int nextSequence();

}
