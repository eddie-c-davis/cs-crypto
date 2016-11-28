#!/bin/bash

export CLASSPATH="`pwd`:`pwd`/target/Peapod-1.0-jar-with-dependencies.jar"
export CP=$CLASSPATH
echo $CP

#java -cp $CP web.Application "$@"
mvn spring-boot:run

