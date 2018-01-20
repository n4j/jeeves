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

package io.artofocde.state;

import io.artofcode.RedisQueueManager;
import io.artofcode.state.StatePersistenceManager;

import static java.lang.String.*;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class to provide vital information for a job to execute and maintain consistent state
 * across JVM restarts.
 *
 * @see     io.artofcode.state.StatePersistenceManager
 * @author  Neeraj Shah
 * @since   0.1
 */
public class QueueState {

	private final String queue;

	private final Map<String, String> state;

	private static final String WORKER_ID = "worker-id";

	private static final String INPROCESS_QUEUE_SUFFIX = "processing";

	public QueueState(String queue) {
		this.queue = queue;
		this.state = StatePersistenceManager.getInstance().getState(queue);
	}

	/**
	 * Check the state Map to see if an id already exists for the given worker.
	 * If it doesn't exist then one is created and returned
	 *
	 * @return id of the task processor
	 */
	public long getWorkerId() {
		String oldWorkerId = state.get(WORKER_ID);
		if(oldWorkerId != null) {
			return Long.parseLong(oldWorkerId);
		} else {
			long newWorkerId = createWorkerId();
			saveState(WORKER_ID, Long.toString(newWorkerId));
			return newWorkerId;
		}
	}

	private long createWorkerId() {
		try(RedisQueueManager qm = new RedisQueueManager(queue)) {
			return qm.createWorkerId();
		}
	}

	/**
	 * Saves the state Map to an OS specific location
	 */
	private void saveState(String key, String value) {
		state.put(key, value);
		StatePersistenceManager.getInstance().saveState(state, queue);
	}

	/**
	 * All the workers atomically move the task to be processed by them to a specific queue
	 * identified by there unique worker id number. This ensures that no two workers process
	 * same task and tasks are persisted in case a worker instance fails and restarts
	 *
	 */
	public String getInprocessQueueName(long workerId) {
		return format("%d:%s:%s", workerId, queue, INPROCESS_QUEUE_SUFFIX);
	}
}