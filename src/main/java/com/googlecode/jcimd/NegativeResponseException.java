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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lorenzo Dee
 *
 */
public class NegativeResponseException extends SessionException {

	private static final long serialVersionUID = -3968988289576317472L;

	private static final Map<Integer, String> errorCodeTexts = new HashMap<Integer, String>();

	static {
		errorCodeTexts.put(  0, "No error");
		errorCodeTexts.put(  1, "Unexpected operation");
		errorCodeTexts.put(  2, "Syntax error");
		errorCodeTexts.put(  3, "Unsupported parameter error");
		errorCodeTexts.put(  4, "Connection to SMS Center lost");
		errorCodeTexts.put(  5, "No response from SMS Center");
		errorCodeTexts.put(  6, "General system error");
		errorCodeTexts.put(  7, "Cannot find information");
		errorCodeTexts.put(  8, "Parameter formatting error");
		errorCodeTexts.put(  9, "Requested operation failed");
		errorCodeTexts.put( 10, "Temporary congestion error");
	}

	private final int errorCode;

	public NegativeResponseException(int errorCode) {
		this(errorCode, errorCodeTexts.get(errorCode), null);
	}

	public NegativeResponseException(int errorCode, String message) {
		this(errorCode, message, null);
	}

	public NegativeResponseException(int errorCode, Throwable cause) {
		this(errorCode, errorCodeTexts.get(errorCode), cause);
	}

	public NegativeResponseException(int errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}
