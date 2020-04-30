package com.jumbo.simulation

import com.jumbo.common.Common
import com.jumbo.common.Common._
import com.jumbo.requests.Postalcodes
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.HeaderValues._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

import scala.concurrent.duration._

/**
  * Running this stress test :
  *
  * LOCAL
  * mvn clean gatling:test -Dgatling.simulationClass=com.jumbo.simulation.PostalCodeSimulation
  *
  * DEV
  * mvn clean gatling:test -Dgatling.simulationClass=com.jumbo.simulation.PostalCodeSimulation -Denvironment=DEV
  *
  * ACC
  * mvn clean gatling:test -Dgatling.simulationClass=com.jumbo.simulation.PostalCodeSimulation -Denvironment=ACC
  *
  */
class PostalCodeSimulation extends Simulation {
  println("===> PostalCodeSimulation <===")
  println("Values for PostalCodeSimulation simulation")
  println(Common)

  val jsonFileFeeder = jsonFile("gatling-data/feeders/gatling-postalcode-feed.json").circular

  val httpConf: HttpProtocolBuilder = http
    .baseUrl(baseURL)
    .disableCaching
    .acceptHeader(ApplicationJson)
    .userAgentHeader("Performance Tests - PostalCodeSimulation")

  var scnReqPostalCode: ScenarioBuilder = scenario("Get PostalCode Scenario")
    .feed(jsonFileFeeder)
    .exec(Postalcodes.reqPostalCode)

  setUp(
    scnReqPostalCode.inject(
      incrementUsersPerSec(usersToIncrementPerIteration.toInt)
        .times(iterations.toInt)
        .eachLevelLasting(iterationsLengthInSeconds.toInt seconds)
        .startingFrom(1)
    ))
    .assertions(global.responseTime.percentile3.lte(200)) // 95th percentile
    .assertions(global.successfulRequests.percent.gte(95)) // 98 percent of request should succeed
    .maxDuration(maxDurationInSeconds.toInt seconds)
    .protocols(httpConf)
}