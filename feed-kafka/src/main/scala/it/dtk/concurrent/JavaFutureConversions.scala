package it.dtk.concurrent

import java.util.concurrent.{Future => JFuture}

import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
 * Created by fabiofumarola on 09/08/15.
 */
object JavaFutureConversions {
  implicit def javaFutureToScala[T](jFuture: JFuture[T]): Future[T] = {
    val promise = Promise[T]()
    val thread = new Thread(new Runnable {
      override def run(): Unit = promise.complete(Try(jFuture.get()))
    })
    thread.start()
    promise.future
  }
}
