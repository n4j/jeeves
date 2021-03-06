# jeeves 
A Redis backed task processor for Java

[![Build Status](https://travis-ci.org/n4j/jeeves.svg?branch=master)](https://travis-ci.org/n4j/jeeves) [![BCH compliance](https://bettercodehub.com/edge/badge/n4j/jeeves?branch=master)](https://bettercodehub.com/) [![codecov](https://codecov.io/gh/n4j/jeeves/branch/master/graph/badge.svg)](https://codecov.io/gh/n4j/jeeves) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/e1d7a00df77a4ef1b63c84656f10634f)](https://www.codacy.com/app/neerajx86/jeeves)

Design goals
============
- Submitted jobs should not be lost
- Jobs are processed in FIFO order
- Job picked up by one worker is guaranteed not to be picked up by any worker
- A worker needs to explicitly specify job success. However, if in pre-determined time a success status is not submitted then the job is retried
- Clear visibility in terms of number of jobs submitted, jobs being processed, jobs in queue, jobs failed
- A job history is maintained for a given period of time for all submitted jobs


### jeeves is still in development phase and all contributions are welcome.
