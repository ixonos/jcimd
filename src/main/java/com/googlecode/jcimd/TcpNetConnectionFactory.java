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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Factory for {@link TcpNetConnection}.
 *
 * @author Lorenzo Dee
 */
public class TcpNetConnectionFactory implements ConnectionFactory {

	private static final Log logger = LogFactory.getLog(TcpNetConnectionFactory.class);

	private final String host;
	private final int port;
	private final int timeout;

	private final String username;
	private final String password;

	private Executor executor;

	public TcpNetConnectionFactory(
			String host, int port, String username, String password) {
		this(host, port, username, password, 0);
	}

	public TcpNetConnectionFactory(String host, int port,
			String username, String password, int timeout) {
		super();
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.timeout = timeout;
		this.executor = Executors.newFixedThreadPool(5);
	}

	@Override
	public Connection getConnection() throws Exception {
		Socket socket = SocketFactory.getDefault().createSocket();
		if (logger.isDebugEnabled()) {
			logger.debug("Connecting to [" + host + ":" + port + "]...");
		}
		socket.connect(new InetSocketAddress(this.host, this.port), 2000);
		if (logger.isDebugEnabled()) {
			logger.debug("Connected to [" + host + ":" + port + "]");
		}
		if (this.timeout > 0) {
			socket.setSoTimeout(this.timeout);
		}
		PacketSerializer serializer = new PacketSerializer();
		serializer.setSequenceNumberGenerator(
				new ApplicationPacketSequenceNumberGenerator());
		TcpNetConnection newConnection = new TcpNetConnection(
				socket, serializer, this.username, this.password);
		this.executor.execute(newConnection);
		newConnection.login();
		return newConnection;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

}