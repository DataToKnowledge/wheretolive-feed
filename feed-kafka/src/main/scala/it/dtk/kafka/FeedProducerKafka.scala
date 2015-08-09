package it.dtk.kafka

import scala.concurrent.{ Future, Promise }
import java.util.Properties

import it.dtk.feed.Model._
import org.apache.kafka.clients.producer.{ RecordMetadata, ProducerRecord, KafkaProducer }
import org.apache.kafka.clients.producer.ProducerConfig._
import org.json4s.NoTypeHints
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import it.dtk.concurrent.JavaFutureConversions._

/**
 * Created by fabiofumarola on 09/08/15.
 */
class FeedProducerKafka(val topic: String,
  val clientId: String,
  brokersList: String,
  batchSize: Int = 1,
  retries: Int = 3,
  ack: Int = -1) {
  implicit val formats = Serialization.formats(NoTypeHints)

  private val props = new Properties()
  props.put(BOOTSTRAP_SERVERS_CONFIG, brokersList)
  props.put(CLIENT_ID_CONFIG, clientId)
  props.put(BATCH_SIZE_CONFIG, batchSize.toString) //send each message
  props.put(RETRIES_CONFIG, retries.toString)
  props.put(ACKS_CONFIG, ack.toString)
  props.put(COMPRESSION_TYPE_CONFIG, "none") //Valid values are "none", "gzip" and "snappy".
  props.put(KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer")
  props.put(VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer")

  private val producer = new KafkaProducer[Array[Byte], Array[Byte]](props)

  def sendAsync(feed: Feed): Future[RecordMetadata] = {
    val message = new ProducerRecord[Array[Byte], Array[Byte]](topic, feed.uri.getBytes(), write(feed).getBytes)
    producer.send(message)
  }

  def sendSync(feed: Feed): RecordMetadata = {
    val message = new ProducerRecord[Array[Byte], Array[Byte]](topic, feed.uri.getBytes(), write(feed).getBytes)
    producer.send(message).get()
  }
}

