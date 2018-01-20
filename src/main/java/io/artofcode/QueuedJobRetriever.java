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