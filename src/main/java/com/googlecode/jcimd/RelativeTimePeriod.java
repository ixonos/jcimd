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
 * Relative implementation of {@link TimePeriod time period}.
 * The {@link #isRelative()} method will always return <code>true</code>.
 * The {@link #getAbsoluteTime()} method will always throw
 * an {@link UnsupportedOperationException}.
 *
 * @author Lorenzo Dee
 * @see AbsoluteTimePeriod
 */
public class RelativeTimePeriod implements TimePeriod {

	private int relativeTime;

	public RelativeTimePeriod(int relativeTime) {
		if (!(relativeTime >= -1 && relativeTime <= 255)) {
			throw new IllegalArgumentException("relativeTime must be between -1 and 255 (inclusive)");
		}
		this.relativeTime = relativeTime;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.TimePeriod#isRelative()
	 */
	@Override
	public boolean isRelative() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.TimePeriod#getRelativePeriod()
	 */
	@Override
	public int getRelativeTime() {
		return this.relativeTime;
	}

	/* (non-Javadoc)
	 * @see org.springframework.integration.cimd.TimePeriod#getAbsoluteTime()
	 */
	@Override
	public Date getAbsoluteTime() {
		throw new UnsupportedOperationException();
	}

}
