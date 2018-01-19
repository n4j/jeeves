/*
Copyright 2018 Neeraj Shah

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.artofcode;

import static java.lang.String.*;
import io.artofocde.state.QueueState;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;

class JobPoller implements Iterable<String> {

	private final String queue;

	private final Logger logger;

	private JobPollerIterator jobIterator;

	private volatile boolean continuePolling = true;

	private QueueState state;

	public JobPoller(String queue, String cron, Logger logger) {
		this.queue = queue;
		this.logger = logger;
		this.jobIterator = new JobPollerIterator();
		this.state = new QueueState(queue);
		logger.info(format("Queue id %d", state.getWorkerId()));
	}

	@Override
	public Iterator<String> iterator() {
		return jobIterator;
	}

	public void stopPolling() {
		continuePolling = false;
	}

	private boolean shouldPoll() {
		return continuePolling;
	}

	private class JobPollerIterator implements Iterator<String> {

		private final RedisQueueManager qm;

		private JobPollerIterator() {
			this.qm = new RedisQueueManager(queue);
		}

		@Override
		public boolean hasNext() { return shouldPoll(); }

		@Override
		public String next() {
			String item = null;
			while(shouldPoll() && (item = qm.get()) == null) {
				try{
					logger.info(format("Queue %s is empty, sleeping.", queue));
					Thread.sleep(1000);
				} catch(InterruptedException ioe) {
					logger.log(Level.SEVERE, "", ioe);
				}
			}
			try{
				qm.close();
			} finally {
				
			}
			return item;
		}

	}

}