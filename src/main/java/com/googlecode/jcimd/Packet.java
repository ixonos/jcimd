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

import java.util.Arrays;

/**
 * Represents a <a href="http://en.wikipedia.org/wiki/CIMD">CIMD</a>
 * (Computer Interface to Message Distribution) message packet.
 * <p>
 * An example CIMD message packet looks like the following:
 * </p>
 * <pre>
 * &lt;STX&gt;03:007&lt;TAB&gt;021:12345678&lt;TAB&gt;033:hello&lt;TAB&gt;&lt;ETX&gt;
 * &lt;STX&gt;53:007&lt;TAB&gt;021:12345678&lt;TAB&gt;060:060:971107131212&lt;TAB&gt;&lt;ETX&gt;
 * </pre>
 *
 * @author Lorenzo Dee
 *
 * @see PacketSerializer
 */
public class Packet {

	/* Requests from client/application */
	/** Login operation code. */
	public static final int OP_LOGIN = 1;
	/** Logout operation code. */
	public static final int OP_LOGOUT = 2;
	/** Submit message operation code. */
	public static final int OP_SUBMIT_MESSAGE = 3;
	/** Enquire message status operation code. */
	public static final int OP_ENQUIRE_MESSAGE_STATUS = 4;
	/** Delivery request operation code. */
	public static final int OP_DELIVERY_REQUEST = 5;
	/** Cancel message operation code. */
	public static final int OP_CANCEL_MESSAGE = 6;
	/** Set (to change interface profile parameters) operation code. */
	public static final int OP_SET = 8;
	/** Get (to retrieve interface profile parameters) operation code. */
	public static final int OP_GET = 9;

	/* Requests from server/SMSC */
	/** Deliver message operation code. */
	public static final int OP_DELIVER_MESSAGE = 20;
	/** Deliver status report operation code. */
	public static final int OP_DELIVER_STATUS_REPORT = 23;

	/* Requests from either */
	/**
	 * Alive operation code.
	 * Used to check whether the link between the application and
	 * the SMS Center is still alive.
	 */
	public static final int OP_ALIVE = 40;

	/** General error response operation code. */
	public static final int OP_GENERAL_ERROR_RESPONSE = 98;
	/**
	 * Nack response operation code. The nack operation is used to
	 * reject an operation due to an incorrect checksum or an
	 * incorrect sequence number. The nack operation causes
	 * retransmission of the message. The packet sequence number
	 * in a nack message is always the expected sequence number.
	 */
	public static final int OP_NACK = 99;

	private int operationCode;
	private Integer sequenceNumber;
	private Parameter parameters[];

	/**
	 * Constructs a packet with the given operation code and parameters,
	 * and <em>no</em> sequence number. This indicates to the
	 * {@link PacketSerializer serializer} to set a packet sequence
	 * number when sending.
	 *
	 * @param operationCode the operation code (0 - 99)
	 * @param parameters the parameters
	 */
	public Packet(int operationCode, Parameter... parameters) {
		this(operationCode, null, parameters);
	}

	/**
	 * Constructs a packet with the given operation code, sequence number,
	 * and parameters.
	 * @param operationCode the operation code (0 - 99)
	 * @param sequenceNumber the sequence number (if <code>null</code>, it
	 * indicates that a generated sequence number be used when sending.
	 * @param parameters the parameters
	 */
	public Packet(int operationCode, Integer sequenceNumber, Parameter... parameters) {
		if (operationCode <= 0 || operationCode > 99) {
			throw new IllegalArgumentException("operationCode must be between 1 and 99");
		}
		this.operationCode = operationCode;
		this.sequenceNumber = sequenceNumber;
		// Due to repeating parameters, we do not store them in a java.util.Map
		this.parameters = parameters;
	}

	public int getOperationCode() {
		return operationCode;
	}

	/**
	 * Returns <code>true</code> if this packet is a response message.
	 * A response message can be one of the following:
	 * <ul>
	 * <li>Positive response message</li>
	 * <li>Negative response message</li>
	 * <li>Nack message</li>
	 * <li>General error response message</li>
	 * </ul>
	 * @return <code>true</code> if this packet is a response message.
	 *     Otherwise, <code>false</code> is returned.
	 */
	public boolean isResponse() {
		return (operationCode >= 50);
	}

	/**
	 * Returns <code>true</code> if this packet is a <em>positive</em>
	 * response message.
	 * @return <code>true</code> if this packet is a positive response
	 * message. Otherwise, <code>false</code> is returned.
	 */
	public boolean isPositiveResponse() {
		return (operationCode >= 50 && operationCode < 90
				&& !hasErrorParameter());
	}

	/**
	 * Returns <code>true</code> if this packet is a <em>negative</em>
	 * response message.
	 * @return <code>true</code> if this packet is a negative response
	 * message. Otherwise, <code>false</code> is returned.
	 * @return
	 */
	public boolean isNegativeResponse() {
		return (operationCode >= 50 && operationCode < 90
				&& hasErrorParameter());
	}

	/**
	 * Returns <code>true</code> if this packet contains an error parameter
	 * (i.e. parameter with type that starts with 9; 9xx). For example,
	 * error code is parameter type 900. And 901 is for the optional
	 * error text.
	 * @return <code>true</code> if this packet contains an error parameter
	 */
	public boolean hasErrorParameter() {
		for (Parameter parameter : this.parameters) {
			if (parameter.getNumber() >= 900) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if this packet is a general error response
	 * (i.e. operation code is 98).
	 * @return <code>true</code> if this packet is a general error response
	 */
	public boolean isGeneralErrorResponse() {
		return (operationCode == 98);
	}

	/**
	 * Returns <code>true</code> if this packet is a <em>nack</em>
	 * (i.e. operation code is 99).
	 * @return <code>true</code> if this packet is a <em>nack</em>
	 */
	public boolean isNack() {
		return (operationCode == 99);
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public Parameter[] getParameters() {
		return parameters.clone();
	}

	public Parameter getParameter(int number) {
		for (Parameter parameter : this.parameters) {
			if (parameter.getNumber() == number) {
				return parameter;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<STX>");
		builder.append(String.format("%02d", this.operationCode));
		builder.append(":");
		if (this.sequenceNumber != null) {
			builder.append(String.format("%03d", this.sequenceNumber));
		} else {
			builder.append("<sequence-number-to-be-generated>");
		}
		builder.append("<TAB>");
		for (Parameter parameter : this.parameters) {
			builder.append(parameter.toString());
			builder.append("<TAB>");
		}
		builder.append("<ETX>");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + operationCode;
		result = prime * result + Arrays.hashCode(parameters);
		result = prime * result
				+ ((sequenceNumber == null) ? 0 : sequenceNumber.hashCode());
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
		Packet other = (Packet) obj;
		if (operationCode != other.operationCode)
			return false;
		if (!Arrays.equals(parameters, other.parameters))
			return false;
		if (sequenceNumber == null) {
			if (other.sequenceNumber != null)
				return false;
		} else if (!sequenceNumber.equals(other.sequenceNumber))
			return false;
		return true;
	}

}
