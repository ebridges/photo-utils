#!/bin/bash

WORKING=../../..
LIB=~/.m2/repository
GDATA=1.40.2

cd ${WORKING}

CP=.
CP=${CP}:target/classes
CP=${CP}:${LIB}/com/google/common/google-collect/1.0-rc1/google-collect-1.0-rc1.jar
CP=${CP}:${LIB}/com/google/gdata/gdata-photos-meta/${GDATA}/gdata-photos-meta-${GDATA}.jar
CP=${CP}:${LIB}/com/google/gdata/gdata-photos/${GDATA}/gdata-photos-${GDATA}.jar
CP=${CP}:${LIB}/com/google/gdata/gdata-media/${GDATA}/gdata-media-${GDATA}.jar
CP=${CP}:${LIB}/com/google/gdata/gdata-core/${GDATA}/gdata-core-${GDATA}.jar
CP=${CP}:${LIB}/com/google/gdata/gdata-client/${GDATA}/gdata-client-${GDATA}.jar
CP=${CP}:${LIB}/com/google/gdata/gdata-client-meta/${GDATA}/gdata-client-meta-${GDATA}.jar
CP=${CP}:${LIB}/com/google/gdata/gdata-base/${GDATA}/gdata-base-${GDATA}.jar
#CP=${CP}:${LIB}/org/metaphile/metaphile/0.2.1/metaphile-0.2.1.jar
CP=${CP}:${LIB}/org/apache/sanselan/sanselan/0.97-incubator/sanselan-0.97-incubator.jar
CP=${CP}:${LIB}/commons-io/commons-io/2.0.1/commons-io-2.0.1.jar
CP=${CP}:${LIB}/log4j/log4j/1.2.15/log4j-1.2.15.jar
CP=${CP}:${LIB}/javax/mail/mail/1.4.1/mail-1.4.1.jar

java -cp ${CP} \
    -Xms1024m \
    -Xmx2048m \
    -Dlog4j.debug=false \
    -Dlog4j.configuration=log4j.xml \
    tinfoil.Uploader
