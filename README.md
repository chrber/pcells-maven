pcells-maven
============

Maven project with the code taken from the pcells github repo.

# Build #

    * mvn package in parent directory
    * Fix error in voms-api jar: zip -d  ~/.m2/repository/eu/emi/vomsjapi/2.0.6/vomsjapi-2.0.6.jar ../../../src/api/java/log4j.properties
    * all jars are under modules/org.pcells/target after successful package

# Jenkins #

    * Create a matrix job with a shell execution job that does the following:
      wget https://raw.githubusercontent.com/dCache/pcells-maven/master/scripts/jenkins/pcells-supported-branches.sh --no-check-certificate
      bash -ex pcells-supported-branches.sh
    * Archive the jar artifacts that are in modules/org.pcells/target
    
# Run #

    * cd modules/org.pcells/target
    * java -jar org.pcells-2.0-SNAPSHOT-jar-with-dependencies.jar
