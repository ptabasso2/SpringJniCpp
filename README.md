# SpringBoot / JNI Tracing from Java to Native code

## Instructions


A detailed step-by-step showing how tracing can be implemented for a SpringBoot app loading a C++ library.
Tracing will be implemented on both layers.

The environment used in this tutorial is based on a linux Ubuntu (20.04).
<br>

### Preliminary tasks and first time steps

***Clone this repository***

```sh
user@ubuntu:~/JNI$ git clone https://github.com/ptabasso2/SpringJniCpp
```

***Initial directory structure***

```sh
user@ubuntu:~/JNI$ tree
.
├── cpp
│   ├── c
│   │   ├── springjni.cpp
│   │   └── text_map_carrier.h
│   ├── lib
│   │   └── libspringjni.so
│   └── Makefile
└── springboot
    ├── build.gradle
    ├── gradle
    │   └── wrapper
    │       ├── gradle-wrapper.jar
    │       └── gradle-wrapper.properties
    ├── gradlew
    ├── settings.gradle
    └── src
        └── main
            ├── java
            │   └── com
            │       └── datadog
            │           └── pej
            │               └── springjni
            │                   ├── SpringController.java
            │                   └── SpringjniApplication.java
            └── resources
                └── application.properties

```



***Install the C++ opentracing library and the C++ Datadog tracing library***

*Opentracing*

```sh
user@ubuntu:~/$ git clone https://github.com/opentracing/opentracing-cpp.git
user@ubuntu:~/$ cd opentracing-cpp
user@ubuntu:~/opentracing-cpp$ mkdir .build
user@ubuntu:~/opentracing-cpp$ cd .build
user@ubuntu:~/opentracing-cpp/.build$ cmake ..
user@ubuntu:~/opentracing-cpp/.build$ make
user@ubuntu:~/opentracing-cpp/.build$ sudo make install
```

*DD Tracing api*

```sh
user@ubuntu:~/$ git clone https://github.com/DataDog/dd-opentracing-cpp
user@ubuntu:~/$ cd dd-opentracing-cpp
user@ubuntu:~/dd-opentracing-cpp$ sudo scripts/install_dependencies.sh
user@ubuntu:~/dd-opentracing-cpp$ mkdir .build
user@ubuntu:~/dd-opentracing-cpp$ cd .build
user@ubuntu:~/dd-opentracing-cpp/.build$ cmake ..
user@ubuntu:~/dd-opentracing-cpp/.build$ make
user@ubuntu:~/dd-opentracing-cpp/.build$ sudo make install
```



The directory should normally look like this:

```sh
user@ubuntu:~/$ tree
opentracing-cpp
dd-opentracing-cpp
JNI
├── cpp
└── springboot
```


### Spin up the Datadog Agent (Provide your API key  to the  below command)


```sh
user@ubuntu:~/JNI$ DOCKER_CONTENT_TRUST=1 docker run -d --rm --name datadog_agent -h datadog \ 
-v /var/run/docker.sock:/var/run/docker.sock:ro -v /proc/:/host/proc/:ro -v /sys/fs/cgroup/:/host/sys/fs/cgroup:ro \
-p 8126:8126 -p 8125:8125/udp -e DD_API_KEY=<Api key to enter> -e DD_APM_ENABLED=true \
-e DD_APM_NON_LOCAL_TRAFFIC=true -e DD_PROCESS_AGENT_ENABLED=true -e DD_DOGSTATSD_NON_LOCAL_TRAFFIC="true" \ 
-e DD_LOG_LEVEL=debug datadog/agent:7
```


### Generate the c++ header file

First we will need to build a fat jar containing all the class files that coming with the project dependencies.  

```sh
user@ubuntu:~/JNI$ cd springboot
user@ubuntu:~/JNI/springboot$ gradle fatJar 
```

This will create the jar file `springjni-all-0.0.1-SNAPSHOT.jar` placed under  `$HOME/JNI/springboot/build/libs`
Now let's generate the header file:

```sh
user@ubuntu:~/JNI/springboot$ javac -h ../cpp/c \
-cp $HOME/JNI/springboot/build/libs/springjni-all-0.0.1-SNAPSHOT.jar \
-d $HOME/JNI/springboot/build/classes/java/main/com/datadog/pej/springjni src/main/java/com/datadog/pej/springjni/SpringController.java
```

The header file will be placed under the `$HOME/cpp/c` directory and is named: `com_datadog_pej_springjni_SpringController.h`


```sh
user@ubuntu:~/JNI$ ls -lrt ../cpp/c
total 12
-rw-r--r-- 1 pej pej  988 Dec 13 08:27 text_map_carrier.h
-rw-r--r-- 1 pej pej 2230 Dec 14 05:46 springjni.cpp
-rw-rw-r-- 1 pej pej  620 Dec 15 23:49 com_datadog_pej_springjni_SpringController.h
```


### Build the Springboot app

```sh
user@ubuntu:~/JNI/springboot$ gradle build
```

### Build the c++ lib

```sh
user@ubuntu:~/JNI/springboot$ cd ../cpp
user@ubuntu:~/JNI/cpp$ make
```

This will place the `libspringjni.so` library in the `$HOME/JNI/cpp/lib` directory

### Running the app

Setting the `LD_LIBRARY_PATH` variable to point to the location of the newly created library. It actually tells the spring boot app where to locate it.
If not specified, it will fail at startup. 

```sh
user@ubuntu:~/JNI/springboot$ export LD_LIBRARY_PATH=$HOME/JNI/cpp/lib
user@ubuntu:~/JNI/springboot$ java -jar ./build/libs/springjni-0.0.1-SNAPSHOT.jar
```

### Testing the app

Open a new terminal and run the following command

```sh
user@ubuntu:~/JNI$ curl localhost:8080/
C++ ended job done...
```

