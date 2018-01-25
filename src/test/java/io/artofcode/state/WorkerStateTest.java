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

import io.artofcode.TestHelper;
import io.artofcode.state.WorkerState;

import static java.lang.String.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class WorkerStateTest extends TestCase {

    private static final String QUEUE_NAME = "url-crawlers-test";

	public WorkerStateTest(String testSuiteName) throws IOException {
		super(testSuiteName);
        TestHelper.setupEnvironment();
	}

	public static Test suite() {
        return new TestSuite( WorkerStateTest.class );
    }

    public void testWorkerId() {
    	WorkerState workerFirstRun = new WorkerState(QUEUE_NAME);
    	long workerId = workerFirstRun.getWorkerId();
    	assertTrue( workerId > 0 );

    	WorkerState workerSecondRun = new WorkerState(QUEUE_NAME);
    	assertTrue( workerSecondRun.getWorkerId() == workerId );
    }

    public void testInProcessQueueName() {
    	WorkerState workerState = new WorkerState(QUEUE_NAME);
    	long workerId = workerState.getWorkerId();

    	assertTrue(workerState.getInprocessQueueName(workerId).equals(
    		format("%d:%s:%s", workerId, QUEUE_NAME, "processing")
    	));
    }

}