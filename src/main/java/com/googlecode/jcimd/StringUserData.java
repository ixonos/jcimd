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

/**
 * Implementation of {@link UserData} that has a {@link String} body.
 * The {@link #isBodyBinary()} method will always return <code>false</code>,
 * and {@link #getBinaryBody()} method will always return <code>null</code>.
 *
 * @author Lorenzo Dee
 * @see BinaryUserData
 */
public class StringUserData extends AbstractUserData {

	private String body;

	public StringUserData(String body, byte[] header, int dataCoding) {
		super(header, dataCoding);
		if (body == null) {
			throw new IllegalArgumentException("body cannot be null");
		}
		this.body = body;
	}

	public StringUserData(String body, byte[] header) {
		this(body, header, 0);
	}

	public StringUserData(String body) {
		this(body, null, 0);
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.UserData#isBodyBinary()
	 */
	@Override
	public boolean isBodyBinary() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.UserData#getBody()
	 */
	@Override
	public String getBody() {
		return this.body;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.UserData#getBinaryBody()
	 */
	@Override
	public byte[] getBinaryBody() {
		return null;
	}

}
