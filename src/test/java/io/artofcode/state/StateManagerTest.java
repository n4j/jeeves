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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class StateManagerTest extends TestCase {

	public StateManagerTest(String testName) { 
		super(testName); 
	}

	public static Test suite() {
        return new TestSuite( StateManagerTest.class );
    }

    public void testStateDirectoryExists() {
    	StateManager manager = StateManager.getInstance();
    	String stateDirPath = manager.getStateDirPath();
    	File stateDir = new File(stateDirPath);
    	
    	assertTrue( new File(stateDirPath).exists() );

    	logger.info(format("Path of state directory %s", stateDirPath));
    }

    private final Logger logger = Logger.getLogger(StateManagerTest.class.toString());
}
