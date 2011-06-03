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

/**
 * Factory for {@link Connection CIMD connection}.
 *
 * @author Lorenzo Dee
 */
public interface ConnectionFactory {

	/**
	 * Returns a new CIMD connection.
	 * @return a new CIMD connection.
	 * @throws Exception when an error occurs while
	 * 		creating a new connection
	 */
	Connection getConnection() throws Exception;

}