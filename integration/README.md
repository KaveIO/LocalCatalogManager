# Integration Tests

The integration test suite is still under development. We rely on a couple different technologies to run this: 

 - Python 3.6 
 - Docker 

To work with this we'd defer to their repective manuals and installation instructions. To give an easy baseline we utilize the [Anaconda](https://www.continuum.io/downloads) python distribution. For the purpose of the integration tests this is a bit of an overkill, however it will align already most of the packages we depend on. Anaconda only misses the docker library by default. You can get it via: 

```bash
   pip install docker
``` 

After this you should be ready to run the integration tests.

## Working with the tests
The integration test module relies on Docker. The code uses the latest kave/lcm image on your machine. If you are developing new integrations it is therefore necesssary to rebuild the Docker image on your machine. This can be done by runnign: 

```bash
   mvn clean 
   mvn package 
   docker-build.sh
``` 

This will build the LCM, build a docker image, and embed the packaged LCM within it. This new image will be tagged with the :buildnumber and :latest. 

After this setup tests can be run by executing the specific test files. e.g. 

```bash
   python integration/core/test.py 
```

This will: 
 - boot up the required docker images
 - load the test fixture directly in the MongoDB docker 
 - execute all the tests
 - stop and remove the used docker containers

## Developing further 
The goal is to have reusable codebase. The current setup isn't yet completely done yet. The highlevel architecture is planned as such: 

 - **integration.py** : Parent class for unittests. This takes care of booting and killing the containers.
 - **core/** : Suite containting tests for the core lcm setup. This should contain all integration tests for:
   - Application data object storge on mongo 
   - File based data storage 
   - UI -> Server integration 
   - Server -> Server integration
   - Server -> Server integration accross versions 
 - **hdp/** : We integrate with HDP and should therefore have a specific hdp backend test suite in here. 
 - **???/** : When we have additional backend implementations we add specific folders for their test suites. 

