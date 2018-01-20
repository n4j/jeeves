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

import static java.lang.String.*;
import java.io.File;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class StateManagerTest extends TestCase {

    private final String queueName = "test-queue";

    private final Logger logger = Logger.getLogger(StateManagerTest.class.toString());

	public StateManagerTest(String testName) { 
		super(testName);
        persistState();
	}

	public static Test suite() {
        return new TestSuite( StateManagerTest.class );
    }

    public void testStateDirCreation() {
        StatePersistenceManager manager = StatePersistenceManager.getInstance();
        String stateDirPath = manager.getStateDirPath();
        
        assertTrue( new File(stateDirPath).exists() );

        logger.info(format("Path of state directory %s", stateDirPath));
    }

    private void persistState() {
        Map<String, String> state = new HashMap<>();
        state.put("queue-id", "495012");
        state.put("lastjob-id", "2341324");
        state.put("worker-name", "jeeves:url:processor");

        StatePersistenceManager manager = StatePersistenceManager.getInstance();
        manager.saveState(state, queueName);
        
        assertTrue( new File(manager.getQueueStatePath(queueName)).exists() );
    }

    public void testStateRead() {
        StatePersistenceManager manager = StatePersistenceManager.getInstance();
        Map<String, String> state = manager.getState(queueName);

        for(Map.Entry<String, String> entry : state.entrySet()) {
            logger.info(format("[%s]=%s", entry.getKey(), entry.getValue()));
        }

        assertTrue( state!= null );
        assertTrue(state.get("queue-id").equals("495012"));
        assertTrue(state.get("lastjob-id").equals("2341324"));
        assertTrue(state.get("worker-name").equals("jeeves:url:processor"));
    }
}
