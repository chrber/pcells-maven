#!/bin/sh
set -x;

if [ -d pcells-maven ]; then
  rm -rf pcells-maven;
fi

git clone https://github.com/dCache/pcells-maven.git;
cd pcells-maven;
git checkout dcache/${dCacheVersion};
mvn package || echo 'First mvn package failed';

cd modules/org.pcells/target;
tar czf pcells-$TAG.tar.gz ./*.jar
