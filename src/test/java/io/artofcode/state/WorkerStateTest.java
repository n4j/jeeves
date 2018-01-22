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

package io.artofcode.state;

import io.artofocde.state.WorkerState;

import static java.lang.String.*;
import java.util.concurrent.ThreadLocalRandom;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class WorkerStateTest extends TestCase {

	public WorkerStateTest(String testSuiteName) {
		super(testSuiteName);
	}

	public static Test suite() {
        return new TestSuite( WorkerStateTest.class );
    }

    public void testWorkerId() {
    	ThreadLocalRandom random = ThreadLocalRandom.current();
    	WorkerState workerFirstRun = new WorkerState(format("queue-%d", random.nextInt()));
    	assertTrue( workerFirstRun.getWorkerId() == 1L );

    	WorkerState workerSecondRun = new WorkerState(format("queue-%d", random.nextInt()));
    	assertTrue( workerSecondRun.getWorkerId() == 1L );
    }

    public void testInProcessQueueName() {
    	ThreadLocalRandom random = ThreadLocalRandom.current();
    	String queueName = format("queue-%d", random.nextInt());
    	WorkerState workerState = new WorkerState(queueName);
    	
    	long workerId = workerState.getWorkerId();

    	assertTrue(workerState.getInprocessQueueName(workerId).equals(
    		format("%d:%s:%s", workerId, queueName, "processing")
    	));
    }

}