package io.artofocde.state;

import io.artofcode.RedisQueueManager;
import io.artofcode.state.StatePersistenceManager;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import redis.clients.jedis.*;

public class QueueState {

	private final String queue;

	private final Map<String, String> state;

	private static final String WORKER_ID = "worker-id";

	public QueueState(String queue) {
		this.queue = queue;
		this.state = StatePersistenceManager.getInstance().getState(queue);
	}

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

	private void saveState(String key, String value) {
		state.put(key, value);
		StatePersistenceManager.getInstance().saveState(state, queue);
	}

	public String getInprocessQueueName() {
		return "";
	}
}