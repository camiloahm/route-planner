package com.jumbo.common


object Common {

  val environments =
    Map(
      "LOCAL" -> "http://localhost:8080",
      "DEV" -> "https://internal.dev.cloud.jumbo.com/v1/routeplanner",
      "ACC" -> "https://internal.acc.cloud.jumbo.com/v1/routeplanner",
    )

  val environment = Option(System.getProperty("environment")) getOrElse "LOCAL"
  val baseURL = Option(System.getProperty("baseURL")) getOrElse environments(environment)
  val usersToIncrementPerIteration = Option(System.getProperty("usersToIncrementPerIteration")) getOrElse "15"
  val iterations = Option(System.getProperty("iterations")) getOrElse "15"
  val iterationsLengthInSeconds = Option(System.getProperty("iterationsLengthInSeconds")) getOrElse "40"
  var maxDurationInSeconds = Option(System.getProperty("maxDurationInSeconds")) getOrElse "620"

  override def toString = s"Common(baseURL=$baseURL, usersToIncrementPerIteration=$usersToIncrementPerIteration, iterations=$iterations, iterationsLengthInSeconds=$iterationsLengthInSeconds, maxDurationInSeconds=$maxDurationInSeconds)"
}
