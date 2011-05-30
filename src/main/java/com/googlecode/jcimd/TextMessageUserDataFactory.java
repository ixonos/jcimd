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

import com.googlecode.jcimd.charset.GsmCharsetProvider;

/**
 * User data factory for text messages.
 * 
 * @author Lorenzo Dee
 */
public class TextMessageUserDataFactory {

	public static UserData newInstance(String textMessage) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(textMessage.length() * 2);
		byte[] bytes;
		if (GsmCharsetProvider.noNonGsmCharacters(textMessage)) {
			// Encode the text message to GSM 3.38 (7-bit default alphabet) septets.
			Charset.forName("GSM").newEncoder().encode(
					CharBuffer.wrap(textMessage), byteBuffer, true);
			//System.out.println(byteBuffer.position());
			bytes = new byte[byteBuffer.position()];
			byteBuffer.flip();
			//System.out.println(byteBuffer.position());
			byteBuffer.get(bytes);
			return new BinaryUserData(bytes, null, 0x00);
		}
		Charset.forName("UTF-16BE").newEncoder().encode(
				CharBuffer.wrap(textMessage), byteBuffer, true);
		//System.out.println(byteBuffer.position());
		bytes = new byte[byteBuffer.position()];
		byteBuffer.flip();
		//System.out.println(byteBuffer.position());
		byteBuffer.get(bytes);
		return new BinaryUserData(bytes, null, 0x08);
	}

}
