package com.udacity.datadog.test

import com.udacity.datadog._
import org.scalatra.ScalatraServlet
import argonaut._
import Argonaut._

class DataDogServlet extends ScalatraServlet {

  post("/api/v1/check_run") {
    request.body.decodeOption[CheckRun] match {
      case Some(checkRun) =>
        DataDogServer.checkRuns = checkRun :: DataDogServer.checkRuns
        Result(status = Some("ok"))
      case None =>
        Result(errors = Some(List("Run missing fields")))
    }
  }

}

object DataDogServer {

  import org.eclipse.jetty.server.Server
  import org.eclipse.jetty.server.ServerConnector
  import org.eclipse.jetty.servlet.ServletContextHandler

  var checkRuns: List[CheckRun] = Nil

  val port = 11115
  val host = s"http://localhost:${port}"

  val server = new Server(port)

  val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
  context.setContextPath("/")
  context.addServlet(classOf[DataDogServlet], "/*")
  server.setHandler(context)

  def start(): Unit = {
    server.start()
  }

  def stop(): Unit = {
    server.stop()
  }

}

