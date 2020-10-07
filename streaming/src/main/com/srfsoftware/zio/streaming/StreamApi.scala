package com.srfsoftware.zio.streaming

import com.srfsoftware.http.HttpClientUsingCertificates
import org.apache.http.client.methods.CloseableHttpResponse
import zio._
import zio.blocking.effectBlocking
import zio.console._
import zio.stream._

object StreamApi extends App {

  def unsafeResponse(certFile: String, keyFile: String) = {
    val client = new HttpClientUsingCertificates(certFile, keyFile)
    client.httpClient.execute(client.httpPost("uri", "header", Array[Byte]()))
  }

  def open(certFile: String, keyFile: String) = {
    val acquire = effectBlocking(unsafeResponse(certFile: String, keyFile: String)).refineToOrDie[Exception]
    val release = (request: CloseableHttpResponse) => effectBlocking(request.close()).orDie
    Managed.make(acquire)(release)
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = ???
}
