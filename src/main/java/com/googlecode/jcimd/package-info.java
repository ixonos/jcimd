/**
 * Provides a simple client-side API for the <a href="http://en.wikipedia.org/wiki/CIMD">CIMD</a>
 * protocol.
 * <p>
 * Below shows how the API can be used to submit a message via SMSC (message center).
 * <pre class="brush: java">
 * String host = "...";
 * int port = 9971;
 * String username = "...";
 * String password = "...";
 * ConnectionFactory connectionFactory = new TcpNetConnectionFactory(host, port, username, password);
 * Session cimdSession = new DefaultSession(connectionFactory);
 * try {
 *     String destinationAddress = "+19098858888";
 *     String originatingAddress = null;
 *     String alphanumericOriginatingAddress = null;
 *     Boolean moreMessagesToSend = null;
 *     TimePeriod validityPeriod = null;
 *     Integer protocolIdentifier = null;
 *     TimePeriod firstDeliveryTime = null;
 *     Boolean replyPathEnabled = null;
 *     Integer statusReportRequest = null;
 *     Boolean cancelEnabled = null;
 *     Integer tariffClass = null;
 *     Integer serviceDescription = null;
 *     Integer priority = null;
 *
 *     cimdSession.submitMessage(
 *            destinationAddress,
 *            originatingAddress, alphanumericOriginatingAddress,
 *            new StringUserData("Hi there"),
 *            moreMessagesToSend,
 *            validityPeriod,
 *            protocolIdentifier,
 *            firstDeliveryTime,
 *            replyPathEnabled,
 *            statusReportRequest,
 *            cancelEnabled,
 *            tariffClass,
 *            serviceDescription,
 *            priority);
 * } finally {
 *     cimdSession.close();
 * }
 * </pre>
 * </p>
 */
package com.googlecode.jcimd;
