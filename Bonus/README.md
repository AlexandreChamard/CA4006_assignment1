# Dependancies

#### Gradle 6.2.2

Detailed information about installing Gradle can be found [here](https://gradle.org/install/).

#### NodeJS 12.9.1 with npm 6.14.3

Detailed information about installing NodeJS can be found [here](https://nodejs.org/en/download/)

# Build the sources

To build the source type `gradle shadowJar`, it will build a jar file which can be found in `build/libs/CA4006_assignment1-all.jar`.
You can also use our script `compile.sh`.

# Starting the project

First go to the server directory, type "npm install" then "npm start".

From another terminal, go to the frontend directory, type "npm install" then "npm start".

Finally go to the root directory and execute the jar file with the command "java -jar factory.jar --redirected".

