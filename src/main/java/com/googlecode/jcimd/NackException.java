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

/**
 * @author Lorenzo Dee
 *
 */
public class NackException extends SessionException {

	private static final long serialVersionUID = -1129027467046769620L;

	private final int expectedPacketSequenceNumber;

	public NackException(int expectedPacketSequenceNumber) {
		this(expectedPacketSequenceNumber, null, null);
	}

	public NackException(int expectedPacketSequenceNumber, String message, Throwable cause) {
		super(message, cause);
		this.expectedPacketSequenceNumber = expectedPacketSequenceNumber;
	}

	public NackException(int expectedPacketSequenceNumber, String message) {
		this(expectedPacketSequenceNumber, message, null);
	}

	public NackException(int expectedPacketSequenceNumber, Throwable cause) {
		this(expectedPacketSequenceNumber, null, cause);
	}

	public int getExpectedPacketSequenceNumber() {
		return expectedPacketSequenceNumber;
	}

}
