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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jcimd.Packet;
import com.googlecode.jcimd.PacketSerializer;
import com.googlecode.jcimd.Parameter;


public class DummyCimd2Server {
	private final Log logger = LogFactory.getLog(this.getClass());

	private int port;
	private ServerSocket serverSocket;
	private Thread thread;
	private PacketSerializer serializer;
	private List<Packet> receivedCommands;

	public DummyCimd2Server(int port) {
		this.port = port;
		this.serializer = new PacketSerializer("DummyCimd2Server");
		this.receivedCommands = new LinkedList<Packet>();
	}

	public void start() throws IOException {
		this.serverSocket = new ServerSocket(this.port);
		if (this.logger.isInfoEnabled()) {
			this.logger.info("Listening on port " + this.port);
		}
		Runnable listener = new Runnable() {
			@Override
			public void run() {
				try {
					while (!Thread.currentThread().isInterrupted()) {
						try {
							Socket socket = serverSocket.accept();
							if (logger.isInfoEnabled()) {
								logger.info("Starting session with " + socket.getInetAddress().getHostAddress());
							}
							DummyCimd2Server.Session session = new Session(socket);
							//List<Session> sessions = ...;
							//sessions.add(session);
							new Thread(session).start();
						} catch (SocketException e) {
							// Ignore, as this was due to #stop
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		this.thread = new Thread(listener, this.getClass().getName() + "-listener");
		this.thread.start();
	}

	public void stop() throws IOException {
		if (this.thread != null) {
			this.thread.interrupt();
		}
		if (this.serverSocket != null) {
			this.serverSocket.close();
		}
	}
	
	class Session implements Runnable {
		private Socket socket;
		private InputStream inputStream;
		private OutputStream outputStream;

		public Session(Socket socket) throws IOException {
			this.socket = socket;
			this.inputStream = socket.getInputStream();
			this.outputStream = socket.getOutputStream();
		}

		@Override
		public void run() {
			try {
				while (!Thread.currentThread().isInterrupted()
						&& this.socket.isConnected()
						&& !this.socket.isClosed()) {
					Packet request = null;
					try {
						request = serializer.deserialize(this.inputStream);
					} catch (IOException e) {
						System.out.println(e.getMessage());
						if (logger.isInfoEnabled()) {
							logger.info("Ending session with " + socket.getInetAddress().getHostAddress());
						}
						break;
					}
					receivedCommands.add(request);
					Packet response;
					switch (request.getOperationCode()) {
					// The operation code of the response packet is
					// fixed to be 50 more than the operation code of 
					// the request packet. The packet number is the
					// same as the request message.
					case Packet.LOGIN:
					case Packet.LOGOUT:
						response = new Packet(
								request.getOperationCode() + 50,
								request.getSequenceNumber());
						break;
					case Packet.SUBMIT_MESSAGE:
						response = new Packet(
								request.getOperationCode() + 50,
								request.getSequenceNumber(),
								new Parameter(60, new SimpleDateFormat("yyMMddHHmmss").format(new Date())));
						break;
					default:
						response = new Packet(Packet.GENERAL_ERROR_RESPONSE);
						break;
					}
					serializer.serialize(response, this.outputStream);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				try {
					this.inputStream.close();
				} catch (IOException e2) {
					System.out.println(e2.getMessage());
				}
				try {
					this.outputStream.close();
				} catch (IOException e2) {
					System.out.println(e2.getMessage());
				}
			}
		}
	}

	public List<Packet> getReceivedCommands() {
		return receivedCommands;
	}

}