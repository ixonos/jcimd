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
 * Implementation of {@link UserData} that has a <code>byte[]</code> binaryBody.
 * The {@link #isBodyBinary()} method will always return <code>true</code>,
 * and {@link #getBody()} method will always return <code>null</code>.
 *
 * @author Lorenzo Dee
 * @see StringUserData
 */
public class BinaryUserData extends AbstractUserData {

	private byte[] binaryBody;

	public BinaryUserData(byte[] binaryBody, byte[] header, int dataCoding) {
		super(header, dataCoding);
		if (binaryBody == null) {
			throw new IllegalArgumentException("binaryBody cannot be null");
		}
		this.binaryBody = binaryBody;
	}

	public BinaryUserData(byte[] binaryBody, byte[] header) {
		this(binaryBody, header, 0);
	}

	public BinaryUserData(byte[] binaryBody) {
		this(binaryBody, null, 0);
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.UserData#isBodyBinary()
	 */
	@Override
	public boolean isBodyBinary() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.UserData#getBody()
	 */
	@Override
	public String getBody() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.UserData#getBinaryBody()
	 */
	@Override
	public byte[] getBinaryBody() {
		return this.binaryBody;
	}

}
