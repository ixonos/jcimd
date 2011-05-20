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

    public static final int USER_IDENTITY = 10;
    public static final int PASSWORD = 11;
    public static final int DESTINATION_ADDRESS = 21;
    public static final int ORIGINATING_ADDRESS = 23;
    public static final int ORIGINATING_IMSI = 26;
    public static final int ALPHANUMERIC_ORIGINATING_ADDRESS = 27;
    public static final int ORIGINATED_VISITED_MSC = 28;
    public static final int DATA_CODING_SCHEME = 30;
    public static final int USER_DATA_HEADER = 32;
    public static final int USER_DATA = 33;
    public static final int USER_DATA_BINARY = 34;
    public static final int MORE_MESSAGES_TO_SEND = 44;
    public static final int VALIDITY_PERIOD_RELATIVE = 50;
    public static final int VALIDITY_PERIOD_ABSOLUTE = 51;
    public static final int PROTOCOL_IDENTIFIER = 52;
    public static final int FIRST_DELIVERY_TIME_RELATIVE = 53;
    public static final int FIRST_DELIVERY_TIME_ABSOLUTE = 54;
    public static final int REPLY_PATH = 55;
    public static final int STATUS_REPORT_REQUEST = 56;
    public static final int CANCEL_ENABLED = 58;
    public static final int CANCEL_MODE = 59;
    public static final int MC_TIMESTAMP = 60;
    public static final int STATUS_CODE = 61;
    public static final int STATUS_ERROR_CODE = 62;
    public static final int DISCHARGE_TIME = 63;
    public static final int TARIFF_CLASS = 64;
    public static final int SERVICE_DESCRIPTION = 65;
    public static final int MESSAGE_COUNT = 66;
    public static final int PRIORITY = 67;
    public static final int DELIVERY_REQUEST_MODE = 68;
    public static final int SERVICE_CENTER_ADDRESS = 69;
    public static final int GET_PARAMETER = 500;
    public static final int MC_TIME = 501;
    public static final int ERROR_CODE = 900;
    public static final int ERROR_TEXT = 901;

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
