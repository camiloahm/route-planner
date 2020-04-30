# Routeplanner API
[![coverage](https://jmb.gitlab.schubergphilis.com/microservices/routeplanner/badges/master/coverage.svg)]()
[![pipeline](https://jmb.gitlab.schubergphilis.com/microservices/routeplanner/badges/master/pipeline.svg)](https://jmb.gitlab.schubergphilis.com/microservices/routeplanner/pipelines)

## About
This application is responsible to be used as a proxy to the following Google Maps services:

- distanceMatrix - Service responsible to calculate the travel time and the distance of a one origin address and a destination address.
More information about this Google Distance Matrix service can be found in: 
[DistanceMatrix Documentation](https://developers.google.com/maps/documentation/distance-matrix/start)
 
- geocoding - Service responsible to retrieve the Latitude and Longitude of a country (NL or BE) with respective postalcode and street name.
More information about this Google Distance Matrix service can be found in:
[Geocoding Documentation](https://developers.google.com/maps/documentation/geocoding/intro)

## Types os testing in this application
  This application is developed with 3 different types of testing:
  1. Unit testing: tests the each class with external dependencies mocked
  2. Integration tests: test the real application. This test starts the application, an in memory MongoDB and a Wiremock (to no make real calls to Google) and consume the endpoints. Afterwards check in mongo if the data has been correctly stored
  
  3. Stress Testing: Tests the endpoints of the application

## How to
  - Execute UNIT tests of this application and get the JaCoCo coverage of the unit tests
    - Execute the terminal command: `mvn clean test`
    - The result coverage will be inside the `./target/jacoco-ui/index.html` file. Just open this file with any browser
  
  - Execute INTEGRATION tests of this application and get the JaCoCo coverage of the integration tests
    - Execute the command: `mvn clean verify`
    - The result coverage will be inside the `./target/jacoco-it/index.html` file. Just open this file with any browser
    
  - Execute STRESS tests (locally) 
    - Start the application (check how to in details below) 
    - For the postalcode endpoint, execute the terminal command `mvn clean gatling:test -Dgatling.simulationClass=com.jumbo.simulation.PostalCodeSimulation `
    - For the route endpoint, execute the terminal command `mvn clean gatling:test -Dgatling.simulationClass=com.jumbo.simulation.RouteSimulation `
   
  - Start the application locally
    - This application require communication with Google API and a Mongo database. There's a `docker-compose` file to start the MongoDB.
    - Execute the command: `docker-compose up -d`. This will start all the required infrastructure for this application
    - Execute the command `mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev`
   
### Required parameters do Start the application

| Name                     | Description                     |
|--------------------------|---------------------------------|
| `APP_GOOGLE_APIKEY`      | Google Maps API key             |
| `SPRING_DATA_MOGODB_URI` | Connection String to MongoDB     |