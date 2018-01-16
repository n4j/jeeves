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
import java.lang.InterruptedException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This class implements a queue processor which polls the tasks from
 * a specified Redis queue and delivers it as POJO to a user specified
 * {@code Consumer}
 *
 * @author  Neeraj Shah
 * @see     java.util.function.Consumer
 * @since   0.1
 */
public class QueueProcessor<T> implements AutoCloseable {

	private final String queue;

	private Consumer<T> consumer;

	private String cronExpression;

	private ExecutorService executor;

	private Logger logger;

	private int numRetries;

	private JobPoller poller;

	private volatile boolean isClosed = false;

	private Class<T> model;

	private final Semaphore semaphore;

	private final Lock globalLock;

	/**
	 * Creates a Redis Queue processor
	 *
	 * @param queue a Redis queue from which to fetch the tasks
	 * @param consumer a functional object to which the taks is delivered as POJO
	 * @param cronExpression specifies the polling schedule
	 * @param executor the messages are processed by {@code Consumer} in this ThreadPool
	 * @param model received task is parsed as this POJO class
	 */
	private QueueProcessor(String queue,
						   Consumer<T> consumer,
						   String cronExpression,
						   ExecutorService executor,
						   Logger logger,
						   int numRetries,
						   Class<T> model) {
		this.queue = queue;
		this.consumer = consumer;
		this.cronExpression = cronExpression;
		this.executor = executor;
		this.logger = logger;
		this.numRetries = numRetries;
		this.model = model;
		this.semaphore = new Semaphore(4, true);
		this.poller = new JobPoller(queue, cronExpression, logger);
		this.globalLock = new ReentrantLock();
	}

	/**
	 * Starts fetching tasks from the Redis queue specified earlier. If no tasks
	 * are found then the processor sleeps for 500ms before retrying. The processor
	 * repeats this process unless close method is called on this instance or the
	 * JVM exists.
	 *
	 * @since 0.1
	 */
	public void start() {
		try {
			logger.log(Level.INFO, format("Starting queue processing on %s", queue));

			for(String json : poller) {
				if(executor.isShutdown()) {
					break;
				}
				
				try {
					logger.info("Finding next thread for task allocation");
					semaphore.acquire();
					logger.info("Thread found");
				} catch(InterruptedException ie) {
					logger.log(Level.SEVERE, "", ie);
				}
				
				executor.execute(() -> {
					try {
						EntityMapper<T> mapper = new EntityMapper<>(model);
						final T obj = mapper.parse(json);
						consumer.accept(obj);
					}
					catch(Exception ex) {
						logger.log(Level.SEVERE, "", ex);
					} 
					finally {
						semaphore.release();
					}
				});
			}
			shutdownPool(executor);
		} catch(Exception ex) {
			logger.log(Level.SEVERE, "", ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Stops polling the specified Redis queue and frees up underlying resources.
	 * All the tasks which were being processed before this method is called will be
	 * allowed to complete.
	 *
	 * @since 0.1
	 */
	@Override
	public void close() {
		if(isClosed) {
			return;
		}

		globalLock.lock();
		try{
			executor.shutdown();
			poller.stopPolling();
		} finally {
			isClosed = true;
			globalLock.unlock();
		}
	}

	private void shutdownPool(ExecutorService pool) {
		try {
			pool.shutdown();
			if(!executor.awaitTermination(60L, TimeUnit.SECONDS)) {
				executor.shutdownNow();
				executor.awaitTermination(120L, TimeUnit.SECONDS);
			}
		} catch(InterruptedException ie) {

		}
	}

	public static class Builder<T> {

		private final String queue;

		private Consumer<T> consumer;

		private String cronExpression;

		private ExecutorService executor;

		private Logger logger;

		private int numRetries;

		private Class<T> model;

		public Builder(String queue) {
			this.queue = queue;
		}

		public Builder consumer(Consumer<T> consumer) {
			this.consumer = consumer;
			return this;
		}

		public Builder cron(String expression) {
			this.cronExpression = expression;
			return this;
		}

		public Builder executor(ExecutorService executor) {
			this.executor = executor;
			return this;
		}

		public Builder logger(Logger logger) {
			this.logger = logger;
			return this;
		}

		public Builder retries(int numRetries) {
			this.numRetries = numRetries;
			return this;
		}

		public Builder model(Class<T> model) {
			this.model = model;
			return this;
		}

		public QueueProcessor build() {
			if(consumer == null) consumer = (v)->{};
			if(cronExpression == null) cronExpression = "";
			if(executor == null) executor = Executors.newWorkStealingPool(4);
			if(logger == null) logger = Logger.getLogger(QueueProcessor.class.toString());
			if(model == null) 
				throw new RuntimeException("Mapping entity is required via a call to model() method of the Builder object.");
			return new QueueProcessor(queue, consumer, cronExpression, executor, logger, numRetries, model);
		}
	}
}