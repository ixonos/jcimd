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

import java.util.Date;

/**
 * Strategy interface for time periods. If {@link #isRelative()}
 * returns <code>true</code>, {@link #getRelativeTime()} should
 * be used to retrieve the time period. Otherwise, if {@link #isRelative()}
 * return <code>false</code>, {@link #getAbsoluteTime()} should
 * be used to retrieve the absolute time.
 *
 * @author Lorenzo Dee
 */
public interface TimePeriod {

	boolean isRelative();
	int getRelativeTime(); /* -1 - 255 */
	Date getAbsoluteTime(); /* to be formatted to yymmddhhmmss */

}
