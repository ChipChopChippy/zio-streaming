package com.srfsoftware.http


import org.apache.http.client.{HttpClient => HttpClientApache}
import org.apache.http.client.methods.{HttpPost => HttpPostApache}
import sttp.client._
import sttp.model.Uri


trait SttpClient {
  def httpGet(uri: Uri): Request[Either[String, String], Nothing]
  def httpPost: RequestT[Identity, String, Nothing]
}

trait ApacheHttpClient {
  val httpClient: HttpClientApache
  def httpPost(uri: String, header: String, data: Array[Byte]): HttpPostApache
}


