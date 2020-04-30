package com.jumbo.requests

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object Postalcodes {

  val headerRequest = Map(
    "Accept" -> "application/json;charset=UTF-8")

  val reqPostalCode = http("PostalCode")
    .get("/postalcodes")
    .headers(headerRequest)
    .queryParam("countryCode", "${countryCode}")
    .queryParam("postalCode", "${postalCode}")
    .check(status.in(200, 400))
}
