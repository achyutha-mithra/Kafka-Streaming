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
      
      /*
      Messages are stored in the topic in a round robin fashion accross partitions. 
      */

    }
  }

  def main(args: Array[String]) {

    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    implicit val timeout: Timeout = 1.seconds

    val actor = system.actorOf(Props[ActorKafkaProd], "AkkaActor")

    val hours = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

    /*
      Randomly generated  hours from the array hours
      are sent to an actor to be processed further.
     */

    val route =
      concat(
      
        path("path1") {

        val rand = new Random(System.currentTimeMillis())
        val random_index2 = rand.nextInt(hr.length)
        val hour = hr(random_index2)
        val finalresult = "path1-"+hour

        actor1 ! fetchURL(finalresult)
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Path1: Hello</h1> <"))
      }
        ,
        path("path2") {

          val rand = new Random(System.currentTimeMillis())
          val random_index2 = rand.nextInt(hr.length)
          val hour = hr(random_index2)
          val finalresult = "path2-"+hour

          actor1 ! fetchURL(finalresult)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Path2: Hello</h1> <"))
        },
        path("path3") {

          val rand = new Random(System.currentTimeMillis())
          val random_index2 = rand.nextInt(hr.length)
          val hour = hr(random_index2)
          val finalresult = "path3-"+hour

          actor1 ! fetchURL(finalresult)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>Path3: Hello</h1> <"))
        }
      )


    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    val _ = bindingFuture
      .flatMap(_.unbind())

  }
}
