# SpringBoot / JNI Tracing from Java to Native code

## Instructions


A detailed step-by-step showing how tracing can be implemented for a SpringBoot app loading a C++ library.
Tracing will be implemented on both layers.


**_Preliminary tasks and first time steps_**

Clone this repository

```sh
MacOSX:springjni - root$ git clone https://github.com/ptabasso2/SpringJniCpp
```


**_Spin up the Datadog Agent (Provide your API key  to the  below command)_** 


```sh
MacOSX:springjni - root$ DOCKER_CONTENT_TRUST=1 docker run -d --rm --name datadog_agent -h datadog \ 
-v /var/run/docker.sock:/var/run/docker.sock:ro -v /proc/:/host/proc/:ro -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro \
-p 8126:8126 -p 8125:8125/udp -e DD_API_KEY=<Api key to enter> -e DD_APM_ENABLED=true \
-e DD_APM_NON_LOCAL_TRAFFIC=true -e DD_PROCESS_AGENT_ENABLED=true -e DD_DOGSTATSD_NON_LOCAL_TRAFFIC="true" \ 
-e DD_LOG_LEVEL=debug datadog/agent:7
```


**_Generate header file_**

```sh
MacOSX:springjni - root$ javac -h ./src/main/java/com/datadog/pej/springjni \
-cp /Users/pejman.tabassomi/JNI/springboot/build/classes/java/main:/Users/pejman.tabassomi/JNI/springboot/build/resources/main:jars/* \
-d /Users/pejman.tabassomi/JNI/springboot/build/classes/java/main/com/datadog/pej/springjni src/main/java/com/datadog/pej/springjni/SpringController.java
```


**_Build Springboot app and copy it under the cpp folder_**

```sh
MacOSX:springjni - root$ ./springboot/gradlew build
MacOSX:springjni - root$ cp ./springboot/build/libs/springjni-0.0.1-SNAPSHOT.jar ./cpp
```

**_Build the c++ lib_**

```sh
MacOSX:springjni - root$ ./cpp/make
```

This will place the libspringjni.so library in the ./cpp/lib

**_Running the app_**
```sh
MacOSX:springjni - root$ ./springboot/build/libs/springjni-0.0.1-SNAPSHOT.jar -Djava.library.path=$(LD_LIBRARY_PATH):./lib
```

**_Testing the app_**
```sh
MacOSX:springjni - root$ curl localhost:8080/
C++ ended job done...
```




<br>

