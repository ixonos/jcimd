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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
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
		send(new Packet(Packet.OP_LOGIN,
				new Parameter(Parameter.USER_IDENTITY, username),
				new Parameter(Parameter.PASSWORD, password)));
	}

	private void logout() throws IOException, SessionException {
		send(new Packet(Packet.OP_LOGOUT));
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
	public String submitMessage(String destinationAddress,
			String originatingAddress, String alphanumericOriginatingAddress,
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
		parameters.add(new Parameter(Parameter.DESTINATION_ADDRESS, destinationAddress));
		addParameterIfNotNull(Parameter.ORIGINATING_ADDRESS, originatingAddress, parameters);
		addParameterIfNotNull(Parameter.ALPHANUMERIC_ORIGINATING_ADDRESS, alphanumericOriginatingAddress, parameters);

		if (userData != null) {
			addParameterIfNotNull(Parameter.DATA_CODING_SCHEME, userData.getDataCodingScheme(), parameters);
			addParameterIfNotNull(Parameter.USER_DATA_HEADER, userData.getHeader(), parameters);
			if (!userData.isBodyBinary()) {
				// TODO: Use UserData#getBody() that returns byte[]
				addParameterIfNotNull(Parameter.USER_DATA, userData.getBody(), parameters);
			} else {
				// TODO: Use UserData#getBody() that returns byte[]
				addParameterIfNotNull(Parameter.USER_DATA_BINARY, userData.getBinaryBody(), parameters);
			}
		}

		addParameterIfNotNull(Parameter.MORE_MESSAGES_TO_SEND, moreMessagesToSend, parameters);

		if (validityPeriod != null) {
			if (validityPeriod.isRelative()) {
				addParameterIfNotNull(Parameter.VALIDITY_PERIOD_RELATIVE,
						validityPeriod.getRelativeTime(), parameters);
			} else {
				addParameterIfNotNull(Parameter.VALIDITY_PERIOD_ABSOLUTE,
						validityPeriod.getAbsoluteTime(), parameters);
			}
		}

		addParameterIfNotNull(52, protocolIdentifier, parameters);

		if (firstDeliveryTime != null) {
			if (firstDeliveryTime.isRelative()) {
				addParameterIfNotNull(Parameter.FIRST_DELIVERY_TIME_RELATIVE,
						firstDeliveryTime.getRelativeTime(), parameters);
			} else {
				addParameterIfNotNull(Parameter.FIRST_DELIVERY_TIME_ABSOLUTE,
						firstDeliveryTime.getAbsoluteTime(), parameters);
			}
		}

		addParameterIfNotNull(Parameter.REPLY_PATH, replyPathEnabled, parameters);
		addParameterIfNotNull(Parameter.STATUS_REPORT_REQUEST, statusReportRequest, parameters);
		addParameterIfNotNull(Parameter.CANCEL_ENABLED, cancelEnabled, parameters);
		addParameterIfNotNull(Parameter.TARIFF_CLASS, tariffClass, parameters);
		addParameterIfNotNull(Parameter.SERVICE_DESCRIPTION, serviceDescription, parameters);
		addParameterIfNotNull(Parameter.PRIORITY, priority, parameters);

		Packet response = send(new Packet(Packet.OP_SUBMIT_MESSAGE,
				parameters.toArray(new Parameter[0])));
		Parameter serviceCenterTimeStampParameter = response.getParameter(Parameter.MC_TIMESTAMP);
		if (serviceCenterTimeStampParameter == null) {
			throw new IOException("Missing response parameter " +
					"(Message Center Timestamp - 060)");
		}
		return serviceCenterTimeStampParameter.getValue();
		/*
		try {
			return dateFormat.parse(serviceCenterTimeStampParameter.getValue());
		} catch (java.text.ParseException e) {
			throw new IOException("Invalid response parameter " +
					"(Message Center Timestamp - 060). " +
					"Expecting yyMMddHHmmss format. But got [" +
					serviceCenterTimeStampParameter.getValue() + "]");
		}
		*/
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
			parameters.add(new Parameter(number, value));
		}
	}

	private void addParameterIfNotNull(
			int number, Boolean value, List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, value));
		}
	}

	private void addParameterIfNotNull(
			int number, byte[] value, List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, value));
		}
	}

	private final DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
	private void addParameterIfNotNull(
			int number, Date value, List<Parameter> parameters) {
		if (value != null) {
			parameters.add(new Parameter(number, dateFormat.format(value)));
		}
	}

	@Override
	public MessageStatus enquireMessageStatus(String destinationAddress,
			String messageCenterTimestamp) throws IOException, SessionException {
		// TODO Auto-generated method stub
		return null;
	}

}
