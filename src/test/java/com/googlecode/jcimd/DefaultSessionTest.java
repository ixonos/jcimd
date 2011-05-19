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

	private static DummyCimd2Server server;
	private static int port = 9971;
	private static String host = "localhost";
	private String username = "user01";
	private String password = "seCreT";
	private Session session;

	@BeforeClass
	public static void setUpCimd2Server() throws Exception {
		server = new DummyCimd2Server(port);
		server.start();
	}

	@AfterClass
	public static void tearDownCimd2Server() throws Exception {
		server.stop();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		server.getReceivedCommands().clear();
	}

	@Test
	public void testLogin() throws Exception {
		new DefaultSession(host, port, username, password);
		assertEquals("Login message expected", 1, server.getReceivedCommands().size());
		assertEquals("Login message expected", 1, server.getReceivedCommands().get(0).getOperationCode());
	}

	@Test
	public void testLogout() throws Exception {
		session = new DefaultSession(host, port, username, password);
		server.getReceivedCommands().clear();
		session.close();
		assertEquals("Logout message expected", 1, server.getReceivedCommands().size());
		assertEquals("Logout message expected", 2, server.getReceivedCommands().get(0).getOperationCode());
	}

	@Test
	public void testSubmitMessage() throws Exception {
		session = new DefaultSession(host, port, username, password);
		server.getReceivedCommands().clear();
		try {
			String destinationAddress = "+19098858888";
			String originatingAddress = null;
			String alphanumericOriginatingAddress = null;
			Integer dataCodingScheme = null;
			UserData userData = new StringUserData("Hi there", null);
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
					dataCodingScheme,
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
			assertEquals("Submit message expected", 1, server.getReceivedCommands().size());
			assertEquals("Submit message expected", 3, server.getReceivedCommands().get(0).getOperationCode());
			assertEquals("Destination address parameter expected",
					destinationAddress, server.getReceivedCommands().get(0).getParameter(21).getValue());
			assertEquals("User data parameter expected",
					userData.getBody(), server.getReceivedCommands().get(0).getParameter(33).getValue());
		} catch (Exception e) {
			fail("Unexpected exception: " + e.getMessage());
		} finally {
			session.close();
		}
	}
}
