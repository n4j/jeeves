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

import io.artofocde.state.WorkerState;
import java.util.Iterator;

/**
 * This class exposes an Iterable which which fetches available jobs from the queue.
 * It is guaranteed that no two workers will fetch the same job ever.
 *
 * @author Neeraj Shah
 * @since 0.1
 */
class JobPoller implements Iterable<String> {

    private final String queue;

    private JobPollerIterator jobIterator;

    public JobPoller(String queue) {
        this.queue = queue;
        this.jobIterator = new JobPollerIterator();
    }

    @Override
    public Iterator<String> iterator() {
        return jobIterator;
    }

    /**
     * This method will be usually called from an external Thread to notify the
     * poller to stop polling gracefully. The poller may not stop immediately and
     * a task may be returned before it finally stops.
     */
    public synchronized void stopPolling() {
        jobIterator.stopPolling();
    }
   
    private class JobPollerIterator implements Iterator<String> {

        private long workerId;

        private WorkerState state;

        private final QueuedJobRetriver retriver;

        private JobPollerIterator() {
            this.state = new WorkerState(queue);
            this.workerId = state.getWorkerId();
            this.retriver = new QueuedJobRetriver(queue, state.getInprocessQueueName(workerId));
        }

        @Override
        public boolean hasNext() {
            // Always return true
            return true; 
        }

        @Override
        public String next() {
            return retriver.retrieveNext();
        }

        private synchronized void stopPolling() {
            retriver.stopPolling();
        }

    }
}