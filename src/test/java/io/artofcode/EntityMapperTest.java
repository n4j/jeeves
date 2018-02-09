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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
public class EntityMapperTest extends TestCase {

    private static final String QUEUE_NAME = "url-crawlers-test";

    public EntityMapperTest(String testName) throws IOException {
        super(testName);
        TestHelper.setupEnvironment();
    }

    public static Test suite() {
        return new TestSuite(EntityMapperTest.class);
    }

    public void testBasic() {
        final QueueProcessor processor = new QueueProcessor.Builder<ScrapJob>(QUEUE_NAME)
                        .model(ScrapJob.class)
                        .consumer((job) -> {})
                        .build();

        assertTrue(processor != null);
    }

    public void testModelRequired() {
        QueueProcessor processor;
        try {
            processor = new QueueProcessor.Builder<ScrapJob>(QUEUE_NAME)
                            .consumer((job) -> {})
                            .build();
        } catch(RuntimeException re) {
            assertTrue(re != null);
            return;
        }
        assertTrue(processor != null);
    }
}