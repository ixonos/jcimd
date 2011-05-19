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
 * Represents a <a href="http://en.wikipedia.org/wiki/CIMD">CIMD</a>
 * (Computer Interface to Message Distribution) command/operation
 * parameter. Used as part of a {@link Packet packet}.
 *
 * @author Lorenzo Dee
 *
 * @see Packet
 */
public class Parameter {
	private int number;
	private String value;

	// TODO: Consider overloading constructor to accept other Java-types (e.g. int, java.util.Date)
	/*
	 * The conversion would be as follows:
	 * int -> toString()
	 * Date -> yyMMddHHmmss (12 digits)
	 * boolean -> 0 - false; 1 - true
	 * byte[] -> as hex string (e.g. byte[] { '0', '1', '2' } --> "303132")
	 */
	public Parameter(int number, String value) {
		if (number < 0 || number > 999) {
			throw new IllegalArgumentException(
					"parameter number must be between 0 and 999");
		}
		this.number = number;
		this.value = value;
	}

	public int getNumber() {
		return number;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%03d", number));
		builder.append(":");
		// Do not show password parameter
		if (number == 11) {
			builder.append("<password-not-shown>");
		} else {
			builder.append(value);
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + number;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameter other = (Parameter) obj;
		if (number != other.number)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
