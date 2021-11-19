# IntelligentGame

## About the Project
This software project is a six month semester subject where you have to develop a client to play the special game against other clients.
This project was developed in a team of three people, who are all mentioned [here](##contributors).
The server was provided by the professor and may not be uploaded for legal reasons.
There is also an own game analysis with which our current implementation can be displayed graphically. More information here
The bash scripts [check.sh](./check.sh), [compare.sh](./compare.sh), [run.sh](./run.sh), [/server/run.sh](./server/run.sh]) were developed by another group and are marked accordingly. This is for easier debugging during development.

Table of Contents
1. [Install Gradle](#install-gradle)
2. [How to start a java file](#how-to-start-a-java-file)
4. [How to start the client](#how-to-start-the-client)
5. [How to start the game analyzer](#how-to-start-the-game-analyzer)
6. [How to create your own map](#how-to-create-your-own-map)
7. [Licence](#licence)
8. [Contributors](#contributors)
9. [Technologies](#technologies)

## Install Gradle
Before you can build this project, you must first install gradle. Here is a short tutorial on how to install Gradle on Ubuntu. Alternatively, the ready-built .jar files are also in the /bin folder.

1. Start by updating the package index
``` sh
sudo apt update
``` 

2. Install the OpenJDK package
``` sh
sudo apt install openjdk-14-jdk
```

3. Download Gradle
``` sh
wget https://services.gradle.org/distributions/gradle-6.8.3-bin.zip -P /tmp
```

4. Extract the zip file in the /opt/gradle directory
``` sh
sudo unzip -d /opt/gradle /tmp/gradle-*.zip
```

5. Setup environment variables
``` sh
sudo nano /etc/profile.d/gradle.sh
```

6. Paste the following configuration
``` sh
export GRADLE_HOME=/opt/gradle/gradle-6.8.3
export PATH=${GRADLE_HOME}/bin:${PATH}
```

7. Make the script executable
``` sh
sudo chmod +x /etc/profile.d/gradle.sh
```

8. Load the environment variables
``` sh
source /etc/profile.d/gradle.sh
```

9. Verify the Gradle installation
``` sh
gradle -v
```


You should see something like the following
```
------------------------------------------------------------
Gradle 6.8.3
------------------------------------------------------------

Build time:   2021-01-08 16:38:46 UTC
Revision:     b7e82460c5373e194fb478a998c4fcfe7da53a7e
 
Kotlin:       1.4.20
Groovy:       2.5.12
Ant:          Apache Ant(TM) version 1.10.9 compiled on September 27 2020
JVM:          11.0.10 (Ubuntu 11.0.10+9-Ubuntu-0ubuntu1.18.04)
OS:           Linux 4.15.0-142-generic amd64
```

## How to start a java file
You can start the .jar files as follows:
``` sh
java -jar <filename.jar>
```
Alternatively, it can be started with a simple double-click, but this may cause problems.

## How to start the client
The client can only be started if the server has been executed before. The client can not be controlled manually, but searches independently for the best possible move.
You can start the client as follows:
``` sh
java -jar ./bin/client01.jar
```

## How to start the game analyzer
The GameAnalyzer was developed to be able to analyze the game afterwards and thus improve parts of the heuristics. It is also possible to view each move in detail. All information of the players at this time is displayed.
Furthermore, all evaluation functions can be viewed in detail as well as in total.

## How to create your own map
Of course, you can create your own maps. To do this, you need to create a .map file and generate it as follows.
```
<playeramount>
<overridestones>
<bombs> <bombradius>
<height> <width>
<first row of field>
<second row of field>
<...>
<first transition> (x1 y1 r1 <-> x2 y2 r2)
<second transition>
<...>
```

Here you can see a small example
```
2
0
0 0
4 4
0 0 0 0
0 1 2 0
0 2 1 0
0 0 0 0
0 0 7 <-> 3 3 3
```

## Licence
Copyright © 2021 Benedikt Halbritter, Iwan Eckert, Markus Koch
You are NOT allowed to copy or modify the code.

## Contributors
[Benedikt Halbritter](https://github.com/bhalbritter) </br>
[Iwan Eckert](https://github.com/eci46) </br>
[Markus Koch](https://github.com/markuskooche) </br>

## Technologies
Java 14.0.2 </br>
Gradle 6.8 </br>
