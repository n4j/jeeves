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

import redis.clients.jedis.*;

public class RedisQueueManager implements AutoCloseable {

	private final String queue;

	private final Jedis jedis;

	public RedisQueueManager(String queue) {
		this.queue = queue;
		this.jedis = new Jedis("localhost");
	}

	public String get() {
		return jedis.lpop(queue);
	}

	@Override
	public void close() {
		jedis.close();
	}
}