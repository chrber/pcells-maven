#!/bin/sh
set -x;

if [ -d pcells-maven ]; then
  rm -rf pcells-maven;
fi

git clone https://github.com/dCache/pcells-maven.git;
cd pcells-maven;
git checkout dcache/${dCacheVersion};
mvn package || echo 'First mvn package failed';
zip -d  ~/.m2/repository/eu/emi/vomsjapi/2.0.6/vomsjapi-2.0.6.jar ../../../src/api/java/log4j.properties; || echo 'Removing problematic file from ~/.m2/repository/eu/emi/vomsjapi/2.0.6/vomsjapi-2.0.6.jar failed."
mvn package;

cd modules/org.pcells/target;
tar czf pcells-$TAG.tar.gz ./*.jar
