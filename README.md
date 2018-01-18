# jeeves
A Redis backed task processor for Java

Design goals
============
- Submitted jobs should not be lost
- Jobs are processed in FIFO order
- Job picked up by one worker is guaranteed not to be picked up by any worker
- A worker needs to explicitly specify job success. However, if in pre-determined time a success status is not submitted then the job is retried
- Clear visibility in terms of number of jobs submitted, jobs being processed, jobs in queue, jobs failed
- A job history is maintained for a given period of time for all submitted jobs

## jeeves is still in development phase and all contributions are welcome
