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
import java.util.logging.Logger;

/**
 * Retrives queued jobs from the specified queue and moves it to worker specific
 * in-process queue. This operation is atomic and hence it is guaranteed that no
 * two workers will pickup same job for processing. 
 *
 * @author Neeraj Shah
 * @since 0.1
 */
class QueuedJobRetriever {

    private final RedisQueueManager rqm;

    private final String queue;

    private final String inProcessQueue;

    private static final int WAIT_TIMEOUT = 10;

    private volatile boolean continuePolling = true;

    private final Logger logger = Logger.getLogger(QueuedJobRetriever.class.toString());

    QueuedJobRetriever(String queue, String inProcessQueue) {
        this.queue = queue;
        this.inProcessQueue = inProcessQueue;
        this.rqm = new RedisQueueManager(queue);
    }

    /**
     * Retrieves next available job from the queue, before the job is returned it is moved
     * to an in process queue. If no jobs are available in the specified queue then this 
     * method blocks for WAIT_TIMEOUT seconds. After WAIT_TIMEOUT elapses it retries again.
     * This continues infinitely until the JVM exists or JobPoller classes' stopPolling() method
     * is called.
     *
     * @return raw string payload
     */
    public String retrieveNext() {
        String payload = null;
        while(continuePolling && payload == null) {
            logger.info(format("Polling job from queue %s with wait timeout %d", queue, WAIT_TIMEOUT));
            payload = rqm.brpoplpush(inProcessQueue, WAIT_TIMEOUT);
        }
        return payload;
    }

    public void stopPolling() {
        continuePolling = false;
    }
}