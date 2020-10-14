package com.srfsoftware.io

import java.nio.file.StandardOpenOption

import zio._
import zio.nio.channels._
import zio.nio.core.file._
import zio.console._

class ReadFile {
  val path = Path("file.txt")
  def asynchChannelM(data: String) = AsynchronousFileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE).use { channel =>
    asynchReadWriteOp(s"$data\n", channel) *> asynchLockOp(channel)
  }
  def synchChannelM(data: String) = FileChannel.open(path, StandardOpenOption.APPEND, StandardOpenOption.WRITE).use { channel =>
    synchReadWriteOp(s"$data\n", channel) *> synchLockOp(channel)
  }

  val synchReadWriteOp = (data: String, channel: FileChannel) =>
    for {
      _     <- putStrLn("")
      input = Chunk.fromArray(data.toArray.map(_.toByte))
      _     <- channel.writeChunk(input)
    } yield ()

  val synchLockOp = (channel: FileChannel) =>
    for {
      isShared     <- channel.lock().bracket(_.release.ignore)(l => IO.succeed(l.isShared))
      _            <- putStrLn(isShared.toString)                                      // false

      managed      = Managed.make(channel.lock(position = 0, size = 10, shared = false))(_.release.ignore)
      isOverlaping <- managed.use(l => IO.succeed(l.overlaps(5, 20)))
      _            <- putStrLn(isOverlaping.toString)                                  // true
    } yield ()

  val asynchReadWriteOp = (data:String, channel: AsynchronousFileChannel) =>
    for {
      chunk <- channel.readChunk(20, 0L)
      text  = chunk.map(_.toChar).mkString
      _     <- putStrLn(text)

      input = Chunk.fromArray(data.toArray.map(_.toByte))
      _     <- channel.writeChunk(input, 0)
    } yield ()

  val asynchLockOp = (channel: AsynchronousFileChannel) =>
    for {
      isShared     <- channel.lock().bracket(_.release.ignore)(l => IO.succeed(l.isShared))
      _            <- putStrLn(isShared.toString)                                      // false

      managed      = Managed.make(channel.lock(position = 0, size = 10, shared = false))(_.release.ignore)
      isOverlaping <- managed.use(l => IO.succeed(l.overlaps(5, 20)))
      _            <- putStrLn(isOverlaping.toString)                                  // true
    } yield ()

}
