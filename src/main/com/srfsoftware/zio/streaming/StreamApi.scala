package com.srfsoftware.zio.streaming

import com.srfsoftware.http.SttpHttpClient
import zio._
import zio.blocking.effectBlocking
import zio.console._
import zio.stream._
import sttp.client._
import sttp.model.Uri

object StreamApi extends App {

  def unsafeResponse(uri: Uri)(implicit backend: SttpBackend[Identity, Nothing, NothingT]) = {
    val client = new SttpHttpClient
    client.httpGet(uri).send()
  }

  def open(uri: Uri) = {
    implicit val backend = HttpURLConnectionBackend()
    val acquire = effectBlocking(unsafeResponse(uri)).refineToOrDie[Exception]
    val release = (request: Identity[Response[Either[String, String]]]) => effectBlocking(backend.close()).orDie
    Managed.make(acquire)(release)
  }

  def download(uri: Uri) = {
    ZStream.fromIterable(List("GBP")).flatMap { param =>
      ZStream.unwrapManaged(open(uri).map(req => ZStream((req, param))))
    }.mapM {case (req, param) =>
      val x = effectBlocking(req.body).refineToOrDie[Exception]
      ZIO.effect(x)
    }
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val uri = uri"https://api.exchangeratesapi.io/latest"
    (for {
      e <- download(uri)
      f <- ZStream.fromEffect(e).flatMap {
        case Right(value) => ZStream(value)
        case Left(exception) => ZStream(exception)
      }
    } yield f).foreach(putStrLn(_)).exitCode
  }
}
