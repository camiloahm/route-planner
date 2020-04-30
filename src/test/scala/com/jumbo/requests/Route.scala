package com.jumbo.requests

import io.gatling.core.Predef._
import io.gatling.http.HeaderNames._
import io.gatling.http.HeaderValues._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

object Route {

  val reqRoutes: HttpRequestBuilder = http("/routes")
    .post("/routes")
    .body(StringBody("${customRequest}"))
    .headers(
      Map(
        ContentType -> ApplicationJson,
        Accept -> ApplicationJson))
    .check(status.in(200, 400))
}
