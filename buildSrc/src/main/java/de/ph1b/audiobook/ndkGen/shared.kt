package de.ph1b.audiobook.ndkGen

import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import org.gradle.api.Task
import java.io.File
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun execute(command: String, directory: File? = null): List<String> {
  val processBuilder = ProcessBuilder().command("/bin/sh", "-c", command)
      .inheritIO()
  directory?.let { processBuilder.directory(directory) }

  val process = processBuilder
      .start()

  process.waitFor()
  val exitValue = process.exitValue()
  if (exitValue != 0) {
    throw IOException("Command $command exited with exitCode=$exitValue")
  }

  return process.inputStream.bufferedReader()
      .readLines()
}

fun download(what: URI, to: File) {
  val tmpFile = File("${to.absolutePath}.tmp")
  tmpFile.delete()
  val sink = Okio.buffer(Okio.sink(tmpFile))

  val client = OkHttpClient.Builder().build()
  val request = Request.Builder()
      .url(what.toURL())
      .get()
      .build()
  val source = client.newCall(request)
      .execute()
      .body()!!
      .source()

  sink.writeAll(source)
  sink.close()

  tmpFile.renameTo(to)
}

fun Task.d(message: Any?) {
  logger.debug(message.toString())
}

fun File.listFilesSafely() = listFiles()?.toList() ?: emptyList<File>()