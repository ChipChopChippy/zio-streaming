package com.srfsoftware.http

import java.io.{BufferedInputStream, ByteArrayInputStream, FileInputStream}
import java.net.Socket
import java.security.cert.{CertificateFactory, X509Certificate}
import java.security.{KeyStore, Principal, PrivateKey}

import javax.net.ssl._
import org.apache.http.entity.InputStreamEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.methods.{HttpPost => HttpPostApache}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.cryptacular.util.KeyPairUtil

class HttpClientUsingCertificates(certFilePath: String, certKeyPath: String) extends ApacheHttpClient {
  override val httpClient = {
    val connectionPoolManager = new PoolingHttpClientConnectionManager()
    val client = HttpClientBuilder.create().setConnectionManager(connectionPoolManager)
    val ssf = sslContext
    client.setSSLContext(ssf)
    client.build()
  }

  override def httpPost(uri: String, header: String, data: Array[Byte]) = {
    val p = new HttpPostApache(uri)
    p.setHeader("Accept", header)
    p.setEntity(new InputStreamEntity(new ByteArrayInputStream(data)))
    p
  }

  private def sslContext = {
    val keyStore = KeyStore.getInstance("JKS")
    keyStore.load(null, null)
    val trustManager = new X509TrustManager {
      override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

      override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

      override def getAcceptedIssuers: Array[X509Certificate] = null
    }
    val ctx = SSLContext.getInstance("TLSv1.2")
    ctx.init(keyManager, Array[TrustManager] {
      trustManager
    }, _)
    ctx
  }

  private def keyManager: Array[KeyManager] =
    Array[KeyManager](new X509KeyManager {
      override def getClientAliases(s: String, principals: Array[Principal]): Array[String] = null

      override def chooseClientAlias(strings: Array[String], principals: Array[Principal], socket: Socket): String = null

      override def getServerAliases(s: String, principals: Array[Principal]): Array[String] = null

      override def chooseServerAlias(s: String, principals: Array[Principal], socket: Socket): String = "abc"

      override def getCertificateChain(s: String): Array[X509Certificate] = certificate

      override def getPrivateKey(s: String): PrivateKey = key
    })

  private def certificate = {
    var x509Certificate: X509Certificate = null
    new BufferedInputStream(new FileInputStream(certFilePath)) {
      bis =>
      val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
      while (bis.available() > 0) {
        x509Certificate = cf.generateCertificate(bis).asInstanceOf[X509Certificate]
      }
    }
    Array(x509Certificate)
  }

  private def key = {
    var privateKey: PrivateKey = null
    new FileInputStream(certKeyPath) {
      fis =>
      privateKey = KeyPairUtil.readPrivateKey(fis)
    }
    privateKey
  }
}
