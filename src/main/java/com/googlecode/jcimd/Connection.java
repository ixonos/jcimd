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
 * Strategy interface for a CIMD connection
 * (connection from client to SMSC).
 *
 * @author Lorenzo Dee
 */
public interface Connection {

	/**
	 * Sends a packet (containing an CIMD operation).
	 * @param request the request packet
	 * @return the response/reply packet
	 * @throws Exception when an error occurs while
	 * 		sending the packet
	 */
	Packet send(Packet request) throws Exception;

	/**
	 * Returns <code>true</code> if this connection is open
	 * for sending packets. Otherwise, <code>false</code> is
	 * returned.
	 * @return <code>true</code> if this connection is open
	 */
	boolean isOpen();

	/**
	 * Returns <code>true</code> if this connection is closed.
	 * Otherwise, <code>false</code> is returned.
	 * @return <code>true</code> if this connection is closed
	 */
	boolean isClosed();

	/**
	 * Closes this connection. This is usually done by sending
	 * a logout command.
	 */
	void close();

}