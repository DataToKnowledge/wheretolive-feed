package it.dtk.kafka

import akka.actor.{ ActorRef, ActorSystem }
import akka.util.Timeout
import com.sclasen.akka.kafka.{ AkkaConsumer, CommitConfig, AkkaConsumerProps }
import kafka.serializer.StringDecoder
import scala.concurrent.duration._
import it.dtk.concurrent.JavaFutureConversions._
import scala.concurrent.Future

/**
 * Created by fabiofumarola on 09/08/15.
 */
class ConsumerKafka(system: ActorSystem,
                    zkConnect: String,
                    topic: String,
                    consumerGroup: String,
                    receiver: ActorRef) {

  val commitConfig = CommitConfig(
    commitInterval = Some(10 seconds),
    commitAfterMsgCount = Some(10),
    commitTimeout = Timeout(10 seconds))

  private val consumerProps = AkkaConsumerProps.forSystem(
    system = system,
    zkConnect = zkConnect,
    topic = topic,
    group = consumerGroup,
    streams = 4,
    maxInFlightPerStream = 64,
    keyDecoder = new StringDecoder(),
    msgDecoder = new StringDecoder(),
    receiver = receiver,
    startTimeout = Timeout(2 seconds),
    commitConfig = commitConfig)

  private val consumer = new AkkaConsumer(consumerProps)

  def start(): Future[Unit] = consumer.start()
  def stop(): Future[Unit] = consumer.stop()
}
