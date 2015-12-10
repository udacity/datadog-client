package com.udacity.datadog.test

import org.scalatest._
import com.udacity.datadog._
import argonaut._
import Argonaut._

class DataDogTests extends FunSuite with BeforeAndAfterAll {

  val apiKey = "42"
  val dataDogClient = DataDogClient(DataDogServer.host, apiKey)

  override def beforeAll(): Unit = {
    DataDogServer.start()
  }

  override def afterAll(): Unit = {
    DataDogServer.stop()
  }

  test("no service checks yet") {
    assert(DataDogServer.checkRuns.lift(0) === None)
  }
 
  test("add service check") {
    dataDogClient.addCheckRun(Up("myservice"), Ok)
    assert(DataDogServer.checkRuns.lift(0).map(_.check) === Some(Up("myservice")))
    assert(DataDogServer.checkRuns.lift(0).map(_.status) === Some(Ok))
  }

  test("status encoding and decoding") {
    assert(Status.decode(Status.encode(Ok)) === Ok)
    assert(Status.decode(Status.encode(Warning)) === Warning)
    assert(Status.decode(Status.encode(Critical)) === Critical)
    assert(Status.decode(Status.encode(Unknown)) === Unknown)
  }

  test("service check encoding and decoding") {
    val now = new java.util.Date()
    val nowish = new java.util.Date(now.getTime() / 1000L * 1000L)

    val checkRun =
      CheckRun(
        check = Other("check"),
        hostName = "hostName",
        status = Unknown,
        timestamp = Some(nowish),
        message = Some("message"),
        tags = Some(List("tag1", "tag2"))
      )

    val encoded = checkRun.asJson.spaces2
    val decoded = encoded.decodeOption[CheckRun]

    assert(Some(checkRun) === decoded)
  }

}
