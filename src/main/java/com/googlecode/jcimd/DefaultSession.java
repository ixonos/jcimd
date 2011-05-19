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
import java.math.BigInteger;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Default {@link Session CIMD session} implementation.
 *
 */
public class DefaultSession implements Session {

	private final Log logger = LogFactory.getLog(this.getClass());

	private String host;
	private int port;

	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	
	private PacketSerializer serializer;

	public DefaultSession(String host, int port, String username, String password)
	throws IOException, SessionException {
		this.host = host;
		this.port = port;
		this.socket = new Socket(host, port);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Connected to " + host + ":" + port);
		}
		this.inputStream = this.socket.getInputStream();
		this.outputStream = this.socket.getOutputStream();
		this.serializer = new PacketSerializer(this.getClass().getSimpleName());
		this.serializer.setSequenceNumberGenerator(
				new ApplicationPacketSequenceNumberGenerator());
		login(username, password);
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	private Packet send(Packet packet) throws IOException, SessionException {
		this.serializer.serialize(packet, this.outputStream);
		Packet response = this.serializer.deserialize(this.inputStream);
		if (!response.isPositiveResponse()) {
			throw new NonPositiveResponseException(
					Integer.valueOf(response.getParameter(900).getValue()),
					response.getParameter(901).getValue());
		}
		return response;
	}

	private void login(String username, String password)
	throws IOException, SessionException {
		send(new Packet(Packet.LOGIN,
				new Parameter(10, username), new Parameter(11, password)));
	}

	private void logout() throws IOException, SessionException {
		send(new Packet(Packet.LOGOUT));
	}

	@Override
	public void close() throws SessionException {
		try {
			logout();
			if (this.socket != null) {
				this.socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Date submitMessage(String destinationAddress,
			String originatingAddress, String alphanumericOriginatingAddress,
			Integer dataCodingScheme,
			UserData userData,
			Boolean moreMessagesToSend,
			TimePeriod validityPeriod,
			Integer protocolIdentifier,
			TimePeriod firstDeliveryTime,
			Boolean replyPathEnabled,
			Integer statusReportRequest,
			Boolean cancelEnabled,
			Integer tariffClass,
			Integer serviceDescription,
			Integer priority)
	throws IOException, SessionException {
		List<Parameter> parameters = new LinkedList<Parameter>();
		parameters.add(new Parameter(21, destinationAddress));
		addParameterIfNotNull(23, originatingAddress, parameters);
		addParameterIfNotNull(27, alphanumericOriginatingAddress, parameters);
		addParameterIfNotNull(30, dataCodingScheme, parameters);

		if (userData != null) {
			addParameterIfNotNull(32, userData.getHeader(), parameters);
			if (!userData.isBodyBinary()) {
				addParameterIfNotNull(33, userData.getBody(), parameters);
			} else {
				addParameterIfNotNull(34, userData.getBinaryBody(), parameters);
			}
		}

		addParameterIfNotNull(44, moreMessagesToSend, parameters);

		if (validityPeriod != null) {
			/*
			/ int 50 Validity period relative 3 O
			\ int 51 Validity period absolute 3 O
			*/
			if (validityPeriod.isRelative()) {
				addParameterIfNotNull(50, validityPeriod.getRelativeTime(), parameters);
			} else {
				addParameterIfNotNull(51, validityPeriod.getAbsoluteTime(), parameters);
			}
		}

		addParameterIfNotNull(52, protocolIdentifier, parameters);

		if (firstDeliveryTime != null) {
			/*
			/ int 053 First delivery time relative 4 O
			\ int 054 First delivery time absolute 4 O
			*/
			if (firstDeliveryTime.isRelative()) {
				addParameterIfNotNull(53, firstDeliveryTime.getRelativeTime(), parameters);
			} else {
				addParameterIfNotNull(54, firstDeliveryTime.getAbsoluteTime(), parameters);
			}
		}

		addParameterIfNotNull(55, replyPathEnabled, parameters);
		addParameterIfNotNull(56, statusReportRequest, parameters);
		addParameterIfNotNull(58, cancelEnabled, parameters);
		addParameterIfNotNull(64, tariffClass, parameters);
		addParameterIfNotNull(65, serviceDescription, parameters);
		addParameterIfNotNull(67, priority, parameters);

		Packet response = send(new Packet(Packet.SUBMIT_MESSAGE,
				parameters.toArray(new Parameter[0])));
		Parameter serviceCenterTimeStampParameter = response.getParameter(60);
		if (serviceCenterTimeStampParameter == null) {
			throw new IOException("Missing response parameter " +
					"(Service Center Time Stamp - 060)");
		}
		try {
			return dateFormat.parse(serviceCenterTimeStampParameter.getValue());
		} catch (ParseException e) {
			throw new IOException("Invalid response parameter " +
					"(Service Center Time Stamp - 060). " +
					"Expecting yyMMddHHmmss format. But got " +
					serviceCenterTimeStampParameter.getValue());
		}
	}

	private void addParameterIfNotNull(
			int number, String value, List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, value));
		}
	}

	private void addParameterIfNotNull(
			int number, Integer value, List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, value.toString()));
		}
	}

	private void addParameterIfNotNull(
			int number, Boolean value, List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, (value ? "1" : "0")));
		}
	}

	private void addParameterIfNotNull(
			int number, byte[] value, List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, new BigInteger(value).toString(16)));
		}
	}

	private final DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
	private void addParameterIfNotNull(
			int number, Date value, List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, dateFormat.format(value)));
		}
	}

}
