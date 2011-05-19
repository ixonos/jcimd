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
 * Strategy interface for user data. If {@link #isBodyBinary()}
 * returns <code>true</code>, {@link #getBinaryBody()} shall be
 * used to retrieve user data. Otherwise, {@link #getBody()}
 * shall be used to retrieve user data.
 *
 * @author Lorenzo Dee
 */
public interface UserData {

	byte[] getHeader();

	/**
	 * Returns <code>true</code> if user data contains
	 * binary data.
	 * @return <code>true</code> if user data contains
	 * binary data.
	 */
	boolean isBodyBinary();

	/**
	 * Returns the string user data. Called only if
	 * {@link #isBodyBinary()} returns <code>false</code>.
	 * @return the string user data
	 */
	String getBody();

	/**
	 * Returns the binary user data. Called only if
	 * {@link #isBodyBinary()} returns <code>true</code>.
	 * @return the binary user data
	 */
	byte[] getBinaryBody();

}
