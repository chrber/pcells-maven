#!/bin/sh
set -x;

if [ -d dcache ]; then
  rm -rf dcache;
fi
git clone https://github.com/dCache/dcache.git;
cd dcache;
git checkout -b branch/${dCacheVersion} origin/${dCacheVersion};
CURRENT_DIR=`pwd`;
SNAPSHOTVERSION=$(/bin/egrep -Eho [0-9]\.[0-9]+\.[0-9]+\-SNAPSHOT "$CURRENT_DIR/pom.xml");
mvn clean;
mvn install -am -pl modules/cells -DskipTests;
mvn install -am -pl modules/dcache -DskipTests;

cd ..;
if [ -d pcells-maven ]; then
  rm -rf pcells-maven;
fi

git clone https://github.com/dCache/pcells-maven.git;
cd pcells-maven;
TAG='2.0.3'
git checkout -b tag/$TAG $TAG;
sed -i "s/<dependency\.dcache\.version>2\.10\.10\-SNAPSHOT<\/dependency\.dcache\.version>/<dependency\.dcache\.version\>${SNAPSHOTVERSION}<\/dependency\.dcache\.version>/g" pom.xml;
mvn package || echo 'First mvn package failed';
zip -d  ~/.m2/repository/eu/emi/vomsjapi/2.0.6/vomsjapi-2.0.6.jar ../../../src/api/java/log4j.properties;
mvn package;

cd modules/org.pcells/target;
tar czf pcells-$TAG.tar.gz ./*.jar
