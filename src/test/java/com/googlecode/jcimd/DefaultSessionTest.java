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


import static org.junit.Assert.*;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.googlecode.jcimd.DefaultSession;
import com.googlecode.jcimd.Session;
import com.googlecode.jcimd.StringUserData;
import com.googlecode.jcimd.TimePeriod;
import com.googlecode.jcimd.UserData;


public class DefaultSessionTest {

	private static DummyCimdServer server;
	private static int port = 9971;
	private static String host = "localhost";

	private String username = "user01";
	private String password = "seCreT";
	private Session session;

	private ConnectionFactory connectionFactory;

	@BeforeClass
	public static void setUpCimd2Server() throws Exception {
		server = new DummyCimdServer(port);
		server.start();
	}

	@AfterClass
	public static void tearDownCimd2Server() throws Exception {
		server.stop();
	}

	@Before
	public void setUp() throws Exception {
		connectionFactory = new TcpNetConnectionFactory(
				host, port, username, password);
	}

	@After
	public void tearDown() throws Exception {
		server.getReceivedCommands().clear();
	}

	private void submitMessage(String destinationAddress, UserData userData) throws Exception {
		String originatingAddress = null;
		String alphanumericOriginatingAddress = null;
		Boolean moreMessagesToSend = null;
		TimePeriod validityPeriod = null;
		Integer protocolIdentifier = null;
		TimePeriod firstDeliveryTime = null;
		Boolean replyPathEnabled = null;
		Integer statusReportRequest = null;
		Boolean cancelEnabled = null;
		Integer tariffClass = null;
		Integer serviceDescription = null;
		Integer priority = null;

		session.submitMessage(
				destinationAddress,
				originatingAddress, alphanumericOriginatingAddress,
				userData,
				moreMessagesToSend,
				validityPeriod,
				protocolIdentifier,
				firstDeliveryTime,
				replyPathEnabled,
				statusReportRequest,
				cancelEnabled,
				tariffClass,
				serviceDescription,
				priority);
	}

	@Test
	public void testSubmitMessage() throws Exception {
		session = new DefaultSession(connectionFactory);
		try {
			String destinationAddress = "+19098858888";
			UserData userData = new StringUserData("Hi there");
			
			submitMessage(destinationAddress, userData);

			assertEquals("Two message expected", 2, server.getReceivedCommands().size());
			assertEquals("Login message expected", 1, server.getReceivedCommands().get(0).getOperationCode());
			Packet submitMessagePacket = server.getReceivedCommands().get(1);
			assertEquals("Submit message expected", 3, submitMessagePacket.getOperationCode());
			assertEquals("Destination address parameter expected",
					destinationAddress, submitMessagePacket.getParameter(21).getValue());
			assertEquals("User data parameter expected",
					userData.getBody(), submitMessagePacket.getParameter(33).getValue());
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		} finally {
			server.getReceivedCommands().clear();
			session.close();
			assertEquals("One message expected", 1, server.getReceivedCommands().size());
			assertEquals("Logout message expected", 2, server.getReceivedCommands().get(0).getOperationCode());
		}
	}

	@Test
	public void reconnectsAfterServerDisconnectsDueToInactivity() throws Exception {
		session = new DefaultSession(connectionFactory);
		try {
			String destinationAddress = "+19098858888";
			UserData userData = new StringUserData("Hi there");
			
			submitMessage(destinationAddress, userData);

			System.out.println("Pausing for server to disconnect...");
			Thread.sleep(3000);

			// The session should get a new connection from connection factory
			submitMessage(destinationAddress, userData);

		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		} finally {
			server.getReceivedCommands().clear();
			session.close();
			assertEquals("One message expected", 1, server.getReceivedCommands().size());
			assertEquals("Logout message expected", 2, server.getReceivedCommands().get(0).getOperationCode());
		}
	}
}
