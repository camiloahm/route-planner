package com.jumbo.simulation

import com.jumbo.common.Common._
import com.jumbo.common.Common
import com.jumbo.requests.{Postalcodes, Route}
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.HeaderNames._
import io.gatling.http.HeaderValues._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration._

/**
  * LOCAL
  * mvn clean gatling:test -Dgatling.simulationClass=com.jumbo.simulation.RouteSimulation
  *
  * DEV
  * mvn clean gatling:test -Dgatling.simulationClass=com.jumbo.simulation.RouteSimulation -Denvironment=DEV
  *
  * DEV
  * mvn clean gatling:test -Dgatling.simulationClass=com.jumbo.simulation.RouteSimulation -Denvironment=DEV
  *
  */
class RouteSimulation extends Simulation {
  println("===> RouteSimulation <===")
  println("Values for RouteSimulation simulation")
  println(Common)

  val customSeparatorFeeder = separatedValues("gatling-data/feeders/gatling-routes-feed.txt", '#').circular

  val httpConf: HttpProtocolBuilder = http
    .baseUrl(Common.baseURL)
    .disableCaching
    .acceptHeader(ApplicationJson)
    .userAgentHeader("Performance Tests - RouteSimulation")

  var scnReqRoutes: ScenarioBuilder = scenario("Get Routes Scenario")
    .feed(customSeparatorFeeder)
    .exec(Route.reqRoutes)

  setUp(
    scnReqRoutes.inject(
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