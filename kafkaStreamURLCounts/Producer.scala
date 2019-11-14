import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import scala.concurrent.duration._
import scala.io.StdIn
import java.util.{Date, Properties}
import scala.util.Random
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object Producer {

  // Kafka producer properties
  val props = new Properties()
  props.put("bootstrap.servers", "localhost:9092")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, String](props)

  // A case class which will be used by an Akka Actor
  final case class fetchURL(param: String)

  // This actor receives a path and sends it through a producer to a topic
  class ActorKafkaProd extends Actor with ActorLogging {
    def receive = {
      case fetchURL(param) =>
        val record = new ProducerRecord[String, String]("topic1", param)
        producer.send(record)

    }
  }

  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    implicit val timeout: Timeout = 1.seconds

    val actor = system.actorOf(Props[ActorKafkaProd], "AkkaActor")

    val paths = Array("path1", "path2", "path3")
    val hours = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    /*
      Randomly generated paths and their hours from the array paths and hours
      are sent to an actor to be processed further.
     */

    val route =
      path("hi") {
        val random_value = new Random(System.currentTimeMillis())

        val random_index = random_value.nextInt(paths.length)
        val final_path = paths(random_index)

        val random_index2 = random_value.nextInt(hours.length)
        val final_hour = hours(random_index2)

        val finalresult = final_path + "-" + final_hour
        actor ! fetchURL(finalresult)

        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Hello, A path and its corresponding hour are sent to kafka!</h1> <"))
      }


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    val _ = bindingFuture
      .flatMap(_.unbind())

  }
}
