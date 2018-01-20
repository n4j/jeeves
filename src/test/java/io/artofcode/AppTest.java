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
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AppTest 
    extends TestCase
{

    public AppTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testBasic()
    {
        final QueueProcessor processor = new QueueProcessor.Builder<ScrapJob>("crawlers:url")
            .consumer((url)->{
                ThreadLocalRandom random = ThreadLocalRandom.current();
                logger.log(Level.INFO, format("[Object %d]: Processing url %s", this.hashCode(), url.getUrl()));
                try {
                    Thread.currentThread().sleep(random.nextLong(200L, 2000L));
                } catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            })
            .model(ScrapJob.class)
            .retries(5)
            .build();

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                processor.close();
                logger.log(Level.INFO, "Queue closed");
            }
        };

        Date date = new Date(System.currentTimeMillis() + 5000L);
        System.out.println(date);
        timer.schedule(task, date);

        processor.start();
    }
    private final Logger logger = Logger.getLogger(AppTest.class.toString());
}
