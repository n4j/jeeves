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

import static java.lang.String.format;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Monitors a connected Jedis client and re-connects if the underlying connection has closed.
 *
 * @author Neeraj Shah
 * @since 0.1
 * */
class RedisConnectionMonitor {

    private final Logger logger = Logger.getLogger(RedisConnectionMonitor.class.toString());

    private final Jedis client;

    private final int maxRetries;

    public RedisConnectionMonitor(Jedis client, int maxRetries) {
        this.client = client;
        this.maxRetries = maxRetries;
    }

    /**
     * Validates that underlying Redis connection is not closed, if it is closed then a re-connection attempt is made
     * for maxRetries times. If the connection is not closed or the connection is re-established then the specified
     * action is executed in the current Thread. If the client doesn't re-connect after specified number of times then
     * Exception is thrown to the client
     *
     * @param action - Action to be executed which produces result of type T
     *
     * @throws RuntimeException
     */
    public <T> T invoke(Callable<T> action) {
            try {
                return action.call();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error while calling Jedis client, reconnection will be attempted", ex);
                checkAndReconnect();
                try {
                    return action.call();
                }catch (Exception ex2) {
                    throw new RuntimeException(ex2);
                }
            }
    }

    private void checkAndReconnect() {
        for (int retryCount = 0; retryCount < maxRetries; retryCount++) {
            try {
                logger.info(format("Attempting re-connection %2d /%2d", (retryCount + 1), maxRetries));

                /**
                 * Need to call disconnect because in several scenarios, like Redis server restart, Jedis' isConnected
                 * method weirdly returns true and connect method first checks for isConnected before connecting.
                 * So need to call disconnect first in order to bring Jedis to a clean state
                 */
                client.disconnect();
                client.connect();

                if(client.isConnected())
                    break;
                else
                    shouldRetryConnection(retryCount, null);
            } catch (JedisConnectionException jce) {
                shouldRetryConnection(retryCount, jce);
            }
        }
    }

    private void shouldRetryConnection(int retryCount, JedisConnectionException jce) {
        /**
         * If we have attempted re-connection for maxRetries then give up and throw the exception to the client
         **/
        if ((retryCount + 1) == maxRetries) {
            if(jce != null)
                throw new RuntimeException(jce);
            else
                throw new RuntimeException("Failed to connect to Redis server. Givign up retries.");
        }

        logger.log(Level.SEVERE, "Connection attempt failed", jce);
        logger.info("Retrying after 1000ms");

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException ie) {
            logger.log(Level.SEVERE, "InterruptedException swallowed", ie);
        }
    }
}
