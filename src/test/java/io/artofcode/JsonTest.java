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
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.google.gson.*;

/**
 * Unit test for simple App.
 */
public class JsonTest extends TestCase {

    private final Logger logger = Logger.getLogger(JsonTest.class.toString());

	public JsonTest(String testName) {
		super(testName);
	}

	public static Test suite() {
        return new TestSuite( JsonTest.class );
    }

    public void testPojoToJson() {
    	Gson gson = new Gson();
    	ScrapJob job = new ScrapJob("https://google.com", 12, new GregorianCalendar());
    	String json = gson.toJson(job);

    	assertTrue( json != null );
    	logger.log( Level.INFO, json );
    }

    public void testJsonGsonParse() {
    	String json = "{\"url\":\"https://google.com\",\"numHits\":12,\"createdOn\":{\"year\":2018,\"month\":0,\"dayOfMonth\":15,\"hourOfDay\":10,\"minute\":35,\"second\":21}}";
    	Gson gson = new Gson();
    	ScrapJob job = gson.fromJson( json, ScrapJob.class );

    	assertTrue( job != null );
    	logger.log( Level.INFO, job.toString() );
    }

}