package com.srfsoftware.zio.streaming

import java.io.InputStream

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

  def download = {
    ZStream.fromIterable(List(123,456,789)).flatMap { param =>
      ZStream.unwrapManaged(open("","").map(req => ZStream((req, param))))
    }.mapM {case (req, param) =>
      val x = effectBlocking(req.getEntity.getContent).refineToOrDie[Exception]
      x.flatMap(fm => ZIO.effect(parseTheByteArray(fm), param))
    }
  }

  def parseTheByteArray(stream: InputStream) = ""

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    download.flatMap {
      case (s:String, i:Int) => ZStream.fromIterable(s).map(_.toString)
      case _                 => ZStream.empty
    }.foreach(putStrLn(_)).exitCode
  }
}
