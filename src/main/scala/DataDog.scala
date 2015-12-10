package com.udacity.datadog

import java.util.Date
import java.net._
import argonaut._
import Argonaut._

case class DataDogClient(ddHost: String, apiKey: String) {

  val localHost: String =
    sys.env.get("HOSTNAME").getOrElse("(unknown)")

  def addCheckRun(check: Check, status: Status): Unit =
    addCheckRun(CheckRun(check, localHost, status))

  def addCheckRun(checkRun: CheckRun): Option[Result] = {
    val url = new URL(s"${ddHost}/api/v1/check_run?api_key=${apiKey}")
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setDoInput(true)
    conn.setDoOutput(true)

    val os = conn.getOutputStream
    val req = checkRun.asJson.spaces2
    os.write(req.getBytes("UTF-8"))
    os.close

    val is = conn.getInputStream
    val resp = scala.io.Source.fromInputStream(is).mkString
    is.close
    resp.decodeOption[Result]
  }

}

sealed trait Check
case class Up(service: String) extends Check
case class Other(check: String) extends Check

object Check {

  private val up = """^([^.]+)\.up$""".r

  def decode(x: String): Check =
    x match {
      case up(service) => Up(service)
      case _ => Other(x)
    }

  def encode(x: Check): String =
    x match {
      case Up(service) => s"${service}.up"
      case Other(y) => y
    }

}

sealed trait Status
case object Ok extends Status
case object Warning extends Status
case object Critical extends Status
case object Unknown extends Status

object Status {

  def decode(x: Int): Status =
    x match {
      case 0 => Ok
      case 1 => Warning
      case 2 => Critical
      case 3 => Unknown
    }

  def encode(x: Status): Int =
    x match {
      case Ok => 0
      case Warning => 1
      case Critical => 2
      case Unknown => 3
    }

}

case class CheckRun(
  check: Check,
  hostName: String,
  status: Status,
  timestamp: Option[Date] = None,
  message: Option[String] = None,
  tags: Option[List[String]] = None
)

object CheckRun {

  implicit def decodeJson: DecodeJson[CheckRun] =
    DecodeJson(c => for {
      check     <- (c --\ "check").as[String]
      hostName  <- (c --\ "host_name").as[String]
      status    <- (c --\ "status").as[Int]
      timestamp <- (c --\ "timestamp").as[Option[Long]]
      date       = timestamp map { x => new Date(x * 1000L) } 
      message   <- (c --\ "message").as[Option[String]]
      tags      <- (c --\ "tags").as[Option[List[String]]]
    } yield CheckRun(Check.decode(check), hostName,
                     Status.decode(status), date, message, tags))

  implicit def encodeJson: EncodeJson[CheckRun] =
    EncodeJson((x: CheckRun) =>
      ("check"     := Check.encode(x.check)) ->:
      ("host_name" := x.hostName) ->:
      ("status"    := Status.encode(x.status)) ->:
      x.timestamp.map("timestamp" := _.getTime / 1000L) ->?:
      x.message.map("message" := _) ->?:
      x.tags.map("tags" := _) ->?:
      jEmptyObject
    )
}

case class Result(
  status: Option[String] = None,
  errors: Option[List[String]] = None
)

object Result {

  implicit def codecJson: CodecJson[Result] =
    casecodec2(Result.apply, Result.unapply)("status", "errors")

}
