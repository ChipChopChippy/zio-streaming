package com.srfsoftware.http

import sttp.client._
import sttp.model.Uri

class SttpHttpClient extends SttpClient {
  override def httpGet(uri: Uri) = basicRequest.get(uri)

  override def httpPost = ???
}
