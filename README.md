pcells-maven
============

Maven project with the code taken from the pcells github repo.

# Build #

    * mvn package in parent directory
    * Fix error in voms-api jar: zip -d  ~/.m2/repository/eu/emi/vomsjapi/2.0.6/vomsjapi-2.0.6.jar ../../../src/api/java/log4j.properties
    * all jars are under modules/org.pcells/target after successful package
    
# Run #

    * cd modules/org.pcells/target
    * java -jar org.pcells-2.0-SNAPSHOT-jar-with-dependencies.jar
