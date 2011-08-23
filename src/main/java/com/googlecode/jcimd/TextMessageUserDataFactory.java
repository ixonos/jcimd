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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.googlecode.jcimd.charset.GsmCharsetProvider;

/**
 * User data factory for text messages.
 * <p>
 * This class supports <a href="http://en.wikipedia.org/wiki/Concatenated_SMS">concatenated SMS</a>.
 * Using this method, long messages are split into smaller messages by the sending
 * device and recombined at the receiving end. Each message is then billed separately.
 * When the feature works properly, it is nearly transparent to the user, appearing as
 * a single long text message.
 * 
 * @author Lorenzo Dee
 */
public class TextMessageUserDataFactory {

	private static final Log logger = LogFactory.getLog(TextMessageUserDataFactory.class);
	private static final Charset UTF16BE = Charset.forName("UTF-16BE");
	private static final Charset GSM = loadGsmCharset();

	private static Charset loadGsmCharset() {
		try {
			return Charset.forName("GSM");
		} catch (UnsupportedCharsetException e) {
			if (logger.isErrorEnabled()) {
				logger.error("GSM character set not loaded via context class loader." +
						" Instantiating it directly.");
			}
			return new GsmCharsetProvider().charsetForName("GSM");
		}
	}

	static {
		if (logger.isDebugEnabled()) {
			logger.debug("GSM max. bytes per char: "
					+ (int) Math.ceil(GSM.newEncoder().maxBytesPerChar()));
			logger.debug("UTF-16BE max. bytes per char: "
					+ (int) Math.ceil(UTF16BE.newEncoder().maxBytesPerChar()));
		}
	}

	private static byte[] encodeAs(Charset charset, String textMessage) {
		CharsetEncoder encoder = charset.newEncoder(); 
		ByteBuffer byteBuffer = ByteBuffer.allocate(
				textMessage.length() * (int) Math.ceil(encoder.maxBytesPerChar()));
		encoder.encode(CharBuffer.wrap(textMessage), byteBuffer, true);
		byte[] bytes = new byte[byteBuffer.position()];
		byteBuffer.flip();
		byteBuffer.get(bytes);
		return bytes;
	}

	/**
	 * Creates an array of one or more {@link UserData} objects that represent the given
	 * text message. This method does the splitting of messages (when over 140 bytes).
	 * It also handles GSM 7-bit default alphabet encoding when possible.
	 * Otherwise, UCS-2 (UTF-16 BE) encoding is used.
	 *
	 * @param textMessage the text message to be sent
	 * @return an array of one or more {@link UserData} objects that represent the given
	 * text message
	 */
	public static UserData[] newInstance(String textMessage) {
		return newInstance(textMessage, 140);
	}

	/**
	 * Creates an array of one or more {@link UserData} objects that represent the given
	 * text message. This method does the splitting of messages (when over the given
	 * part length bytes). It also handles GSM 7-bit default alphabet encoding when possible.
	 * Otherwise, UCS-2 (UTF-16 BE) encoding is used.
	 *
	 * @param textMessage the text message to be sent
	 * @param partLength the given maximum number of bytes for each part
	 * @return an array of one or more {@link UserData} objects that represent the given
	 * text message
	 */
	public static UserData[] newInstance(String textMessage, int partLength) {
		final int headerLength = 6;
		int textMessageBytes = 0;
		boolean noNonGsmCharacters = true;
		textMessageBytes = GsmCharsetProvider.countGsm7BitCharacterBytes(textMessage);
		if (textMessageBytes == -1) {
			// textMessage contains characters not in GSM 3.38 default alphabet
			textMessageBytes = textMessage.getBytes(UTF16BE).length;
			noNonGsmCharacters = false;
		}
		int numberOfParts = 1;
		int actualPartLength = partLength;
		if (textMessageBytes > partLength) {
			actualPartLength = partLength - headerLength;
			numberOfParts = (textMessageBytes + actualPartLength - 1) / actualPartLength;
		}
		if (numberOfParts > 255) {
			throw new IllegalArgumentException(
					"textMessage is too long to fit in a max. of 255 parts (max. "
					+ partLength + " for each part)");
		}
		UserData[] uds = new UserData[numberOfParts];
		if (numberOfParts > 1) {
			if (logger.isDebugEnabled()) {
				logger.debug("Splitting " + textMessageBytes + " bytes to " + numberOfParts + " parts");
			}
			byte[] udh, udhTemplate = new byte[] {
					0x05, 0x00, 0x03, 0x01 /* generate unique id */,
					(byte) (numberOfParts & 0xff), 0x00
			};
			if (noNonGsmCharacters) {
				int i = 0, part = 0;
				StringBuilder textMessagePart = new StringBuilder();
				while (i < textMessage.length()) {
					int textMessagePartBits = 0;
					textMessagePart.setLength(0);
					while (i < textMessage.length()
							&& (textMessagePartBits / 8) < actualPartLength) {
						char ch = textMessage.charAt(i);
						int chBits = GsmCharsetProvider.countGsm7BitCharacterBits(ch);
						if (((textMessagePartBits + 8 + chBits - 1) / 8) > actualPartLength) {
							// if the next character can no longer be added
							// to this part without exceeding max. part length
							break;
						}
						textMessagePart.append(ch);
						textMessagePartBits += chBits;
						i++;
					}
					String textMessagePartString = textMessagePart.toString();
					if (logger.isDebugEnabled()) {
						logger.debug("Part " + (part + 1) + " ["
								+ textMessagePartString + "]");
					}
					udh = udhTemplate.clone();
					udh[5] = (byte) ((part + 1) & 0xff);
					uds[part++] = new BinaryUserData(
							encodeAs(GSM, textMessagePartString), udh, 0x00);
				}
			} else {
				int i = 0, part = 0;
				StringBuilder textMessagePart = new StringBuilder();
				while (i < textMessage.length()) {
					textMessagePart.setLength(0);
					while (i < textMessage.length()
							&& textMessagePart.toString().getBytes(UTF16BE).length < actualPartLength) {
						textMessagePart.append(textMessage.charAt(i));
						i++;
					}
					String textMessagePartString = textMessagePart.toString();
					if (logger.isDebugEnabled()) {
						logger.debug("Part " + (part + 1) + " ["
								+ textMessagePartString + "]");
					}
					udh = udhTemplate.clone();
					udh[5] = (byte) ((part + 1) & 0xff);
					uds[part++] = new BinaryUserData(
							encodeAs(UTF16BE, textMessagePartString), udh, 0x08);
				}
			}
		} else {
			if (noNonGsmCharacters) {
				// Encode the text message to GSM 3.38 (7-bit default alphabet) septets.
				uds[0] = new BinaryUserData(encodeAs(GSM, textMessage), null, 0x00);
			} else {
				uds[0] = new BinaryUserData(encodeAs(UTF16BE, textMessage), null, 0x08);
			}
		}

		return uds; 
	}

}
