package com.srfsoftware.io

import zio._
import zio.nio.channels._
import zio.nio.core.file._
import zio.console._

class ReadFile {
  val path = Path("file.txt")
  val channelM = AsynchronousFileChannel.open(path).use { channel =>
    readWriteOp(channel) *> lockOp(channel)
  }

  val readWriteOp = (channel: AsynchronousFileChannel) =>
    for {
      chunk <- channel.read(20, 0)
      text  = chunk.map(_.toChar).mkString
      _     <- putStrLn(text)

      input = Chunk.fromArray("message".toArray.map(_.toByte))
      _     <- channel.write(input, 0)
    } yield ()

  val lockOp = (channel: AsynchronousFileChannel) =>
    for {
      isShared     <- channel.lock().bracket(_.release.ignore)(l => IO.succeed(l.isShared))
      _            <- putStrLn(isShared.toString)                                      // false

      managed      = Managed.make(channel.lock(position = 0, size = 10, shared = false))(_.release.ignore)
      isOverlaping <- managed.use(l => IO.succeed(l.overlaps(5, 20)))
      _            <- putStrLn(isOverlaping.toString)                                  // true
    } yield ()

}
