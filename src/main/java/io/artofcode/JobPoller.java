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

import io.artofcode.state.WorkerState;
import java.util.Iterator;

/**
 * This class exposes an Iterable which which fetches available jobs from the queue. It is guaranteed that no two
 * workers will fetch the same job ever. This class also exposes methods to stop polling for more jobs.
 *
 * @author Neeraj Shah
 * @since 0.1
 */
class JobPoller implements Iterable<String> {

    private final String queue;

    private final JobPollerIterator jobIterator;

    public JobPoller(String queue) {
        this.queue = queue;
        this.jobIterator = new JobPollerIterator();
    }

    @Override
    public Iterator<String> iterator() {
        return jobIterator;
    }

    /**
     * This method will be usually called from an external Thread to notify the poller to stop polling gracefully. The
     * poller may not stop immediately and a task may be returned before it finally stops.
     */
    public synchronized void stopPolling() {
        jobIterator.stopPolling();
    }
   
    private class JobPollerIterator implements Iterator<String> {

        /**
         * The worker id of this job processor
         * */
        private final long workerId;

        /**
         * Saved previous state of the worker
         * */
        private final WorkerState state;

        private final QueuedJobRetriever retriver;

        private JobPollerIterator() {
            this.state = new WorkerState(queue);
            this.workerId = state.getWorkerId();
            this.retriver = new QueuedJobRetriever(queue, state.getInprocessQueueName(workerId));
        }

        /**
         *
         * @return - This method always returns true as we are assuming constant flow of tasks to be coming in the queue
         * and there is no way for us to tell when the jobs would cease to come
         * */
        @Override
        public boolean hasNext() { return true; }

        @Override
        public String next() {
            return retriver.retrieveNext();
        }

        /**
         * Indicates JobRetriever that it should stop polling for more jobs. After calling this method its possible that
         * at the most one job is returned by the JobRetriever before it ceases to poll for more jobs
         * */
        private synchronized void stopPolling() {
            retriver.stopPolling();
        }

    }
}