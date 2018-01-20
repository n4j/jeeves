package io.artofcode;

import io.artofcode.RedisQueueManager;

import static java.lang.String.*;
import java.util.logging.Logger;

class QueuedJobRetriver {

	private final RedisQueueManager rqm;

    private final String queue;

    private final String inprocessQueue;

	private static final int WAIT_TIMEOUT = 10;

    private volatile boolean continuePolling = true;

    private final Logger logger = Logger.getLogger(QueuedJobRetriver.class.toString());

	QueuedJobRetriver(String queue, String inprocessQueue) {
        this.queue = queue;
        this.inprocessQueue = inprocessQueue;
		this.rqm = new RedisQueueManager(queue);
	}

	public String retrieveNext() {
		String payload = null;
		while(continuePolling && payload == null) {
            logger.info(format("Polling job from queue %s with wait timeout %d", queue, WAIT_TIMEOUT));
            payload = rqm.brpoplpush(inprocessQueue, WAIT_TIMEOUT);
		}
		return payload;
	}

    public void stopPolling() {
        continuePolling = false;
    }
}