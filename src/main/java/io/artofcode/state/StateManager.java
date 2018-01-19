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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

/**
 * This class implements State management for task processing jobs. Every time a queue specific
 * processing job is initiated, all the information which will help in task processing to recover
 * after JVM restart is stored as state. The state is stored in .state directory in the
 * directory from which job was executed.
 *
 * @author  Neeraj Shah
 * @since   0.1
 */
class StateManager {

	private static StateManager instance;

	static {
		if(instance == null) {
			synchronized(StateManager.class) {
				instance = new StateManager();
			}
		}
	}

	private final String stateDirName = ".state";

	private final String stateDirPath;

	private StateManager() {
		String userHome = System.getProperty("user.home");
		this.stateDirPath = ((userHome == null || userHome.equals("")) ? 
								System.getProperty("user.dir") : userHome)
								+ File.separator + stateDirName;
		ensureStateDirExists();
	}

	/**
	 * @return Singleton instance of class StateManager
	 */
	public static StateManager getInstance() {
		return instance;
	}

	/**
	 * Returns saved state information for the task processor
	 * identified by queue name
	 *
	 * @return A Map&lt;String, String&gt; containing state information
	 * stored as key value paris
	 */
	public Map<Object, Object> getState(String queue) {
		try(FileInputStream fin = new 
			FileInputStream(stateDirPath + File.separator + queue)){
			Properties props = new Properties();
			props.load(fin);
			return new HashMap<Object, Object>(props);
		} catch(FileNotFoundException fnfe) {
			throw new RuntimeException(fnfe);
		} catch(IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}

	/**
	 * Prevents defeating singleton contract by returning singleton
	 * StateManger instance. Helpful when the instance is serialized 
	 * and then de-serialized.
	 *
	 * @return System wide singleton StateManager instance
	 */
	private StateManager readResolve() {
		return instance;
	}

	/**
	 * Ensures that directory where we save state information exists
	 * and create one if it doesn't. By default the state directory 
	 * is created in a user's home directory, it that is not set then
	 * it is created in the current working directory
	 */
	private void ensureStateDirExists() {
		try {
			File f = new File(stateDirPath);
			if(!f.exists())
				f.mkdir();
		} catch(SecurityException ex) {
			logger.log(Level.SEVERE, "", ex);
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Convinience method for testing
	 *
	 * @return Path of state directory
	 */
	String getStateDirPath() {
		return this.stateDirPath;
	}

	private final Logger logger = Logger.getLogger(StateManager.class.toString());
}
