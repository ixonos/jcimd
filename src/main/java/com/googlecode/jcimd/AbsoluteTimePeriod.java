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

import java.util.Date;

/**
 * Absolute implementation of {@link TimePeriod time period}.
 * The {@link #isRelative()} method will always return <code>false</code>.
 * The {@link #getRelativeTime()} method will always throw
 * an {@link UnsupportedOperationException}.
 *
 * @author Lorenzo Dee
 * @see RelativeTimePeriod
 */
public class AbsoluteTimePeriod implements TimePeriod {

	private Date absoluteTime;

	public AbsoluteTimePeriod(Date time) {
		if (time == null) {
			throw new IllegalArgumentException("time cannot be null");
		}
		this.absoluteTime = time;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.TimePeriod#isRelative()
	 */
	@Override
	public boolean isRelative() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.TimePeriod#getRelativePeriod()
	 */
	@Override
	public int getRelativeTime() {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.TimePeriod#getAbsoluteTime()
	 */
	@Override
	public Date getAbsoluteTime() {
		return this.absoluteTime;
	}

}
