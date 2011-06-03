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

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link Connection} implementation that uses {@link Socket sockets}.
 *
 * @author Lorenzo Dee
 */
public class TcpNetConnection implements Connection, Runnable {

	private static final Log logger = LogFactory.getLog(TcpNetConnection.class);

	private Map<String, AsyncReply> pendingReplies = new ConcurrentHashMap<String, AsyncReply>();

	private final Socket socket;
	private final PacketSerializer serializer;

	private final String username;
	private final String password;

	private boolean loggedIn = false;
	private long replyTimeout = 10000;

	public TcpNetConnection(Socket socket, PacketSerializer serializer,
			String username, String password)
	throws Exception {
		if (socket == null) {
			throw new IllegalArgumentException("socket cannot be null");
		}
		if (serializer == null) {
			throw new IllegalArgumentException("serializer cannot be null");
		}
		this.socket = socket;
		int timeout = socket.getSoTimeout();
		if (timeout > 0) {
			this.replyTimeout = timeout;
		}
		this.serializer = serializer;
		this.username = username;
		this.password = password;
	}

	void login() throws Exception {
		Packet response = send(new Packet(Packet.OP_LOGIN,
				new Parameter(Parameter.USER_IDENTITY, this.username),
				new Parameter(Parameter.PASSWORD, this.password)));
		if (!response.isPositiveResponse()) {
			throw new IOException("Failed to login");
		} else {
			this.loggedIn = true;
		}
	}

	private void logout() throws Exception {
		Packet response = send(new Packet(Packet.OP_LOGOUT));
		if (!response.isPositiveResponse()) {
			throw new IOException("Failed to logout");
		} else {
			this.loggedIn = false;
		}
	}

	@Override
	public synchronized Packet send(Packet request) throws Exception {
		AsyncReply asyncReply = new AsyncReply();
		this.pendingReplies.put("", asyncReply);
		this.serializer.serialize(request, this.socket.getOutputStream());
		return asyncReply.getReply();
	}

	@Override
	public boolean isOpen() {
		return !isClosed();
	}

	@Override
	public boolean isClosed() {
		return this.socket.isClosed();
	}

	@Override
	public void close() {
		logger.debug("Closing connection by sending logout operation...");
		try {
			try {
				if (this.loggedIn) {
					logout();
				}
			} finally {
				closeSocket();
			}
		} catch (Exception e) {
			if (logger.isTraceEnabled()) {
				logger.trace("Ignoring error while closing connection: " + e.getMessage());
			}
		}
	}

	@Override
	public void run() {
		logger.debug("Ready for replies...");
		Packet reply;
		while (true) {
			try {
				reply = this.serializer.deserialize(this.socket.getInputStream());
			} catch (SocketException e) {
				break;
			} catch (Exception e) {
				if (logger.isDebugEnabled()) {
					logger.debug("Read exception " +
							 e.getClass().getName() + 
						     ": " + e.getCause() + ": " + e.getMessage());
				}
				// since it's a socket exception, let's close without sending a logout operation
				closeSocket();
				break; // get out of this while-loop
			}
			AsyncReply asyncReply = pendingReplies.get("");
			asyncReply.setReply(reply);
		}
	}

	private synchronized void closeSocket() {
		if (!this.socket.isClosed()) {
			try {
				logger.debug("Closing socket...");
				this.socket.close();
			} catch (IOException ioe) {
				if (logger.isTraceEnabled()) {
					logger.trace("Ignoring error while closing socket: " + ioe.getMessage());
				}
			}
		}
	}

	private class AsyncReply {
		private final CountDownLatch latch;

		private volatile Packet reply;

		public AsyncReply() {
			this.latch = new CountDownLatch(1);
		}

		/**
		 * Sender blocks here until the reply is received, or we time out
		 * @return The return message or null if we time out
		 * @throws Exception
		 */
		public Packet getReply() throws Exception {
			try {
				if (!this.latch.await(replyTimeout, TimeUnit.MILLISECONDS)) {
					return null;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			return this.reply;
		}

		public void setReply(Packet reply) {
			this.reply = reply;
			this.latch.countDown();
		}
	}

}