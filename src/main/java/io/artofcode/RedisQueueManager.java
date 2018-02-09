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

import io.artofcode.config.ConfigurationManager;

import static java.lang.String.*;
import java.util.Map;

import redis.clients.jedis.Jedis;


/**
 * Wrapper class for Redis client. Provides utility method to create queue specific unique worker-ids, retrieve job
 * payload from queue etc. If underlying Redis connection is closed then a connection is re-attempted for
 * MAX_RECONNECTION_ATTEMPTS of times before re-connection attempts are given up and an exception is thrown.
 *
 * @author Neeraj Shah
 * @since 0.1
 */
public class RedisQueueManager implements AutoCloseable {

    private static final int MAX_RECONNECTION_ATTEMPTS = 5;

    private final String queue;

    private final Jedis jedis;

    private final Map<String, String> configuration;

    private final RedisConnectionMonitor monitor;

    public RedisQueueManager(String queue) {
        this.queue = queue;
        this.configuration = ConfigurationManager.getInstance().get(queue);
        this.jedis = new Jedis(configuration.get("REDIS_HOST"));
        this.monitor = new RedisConnectionMonitor(jedis, MAX_RECONNECTION_ATTEMPTS);
    }

    public String get() {
        return monitor.invoke(() -> jedis.lpop(queue));
    }

    public String brpoplpush(String destination, int timeout) {
        return monitor.invoke(() -> jedis.brpoplpush(queue, destination, timeout));
    }

    /**
     * All the workers processing a given queue are given unique ids which is used to identify worker specific
     * in-process queue and metadata. The id is created by simple incrementing counter of queue:worker-ids key in Redis.
     *
     * @return Worker id created by incrementing key queue:worker-ids
     */
    public long createWorkerId() {
        return monitor.invoke(() -> jedis.incr(format("%s:worker-ids", queue)));
    }

    @Override
    public void close() {
        jedis.close();
    }

}