import java.sql.{DriverManager, PreparedStatement}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._
import org.apache.log4j._

class Consumer(topics: String, bootstrap_server: String, group_name: String) {

  // To display only ERROR level logs
  Logger.getLogger("org").setLevel(Level.ERROR)

  // Configuring Spark
  val sparkConf = new SparkConf().setAppName("DirectKafkaWordCount")
    .setMaster("local[*]")
    .set("spark.executor.memory","1g")
  sparkConf.set("spark.scheduler.mode", "FAIR")
  
  /*
  By default, Spark’s scheduler runs jobs in FIFO fashion.
  But with a FAIR scheduler, Spark assigns tasks between jobs in a “round robin” fashion, 
  so that all jobs get a roughly equal share of cluster resources.
  */

  // A streaming context variable
  val ssc = new StreamingContext(sparkConf, Seconds(1))

  // To deal with multiple topics
  val topicsSet = topics.split(",")

  
  
  val kafkaParams = Map[String, Object](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> bootstrap_server,
    ConsumerConfig.GROUP_ID_CONFIG -> group_name,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    "auto.offset.reset" ->"earliest")
  
  /* Earliest - Consumes buffered messages also which weren't consumed/committed before. 
                This lets the consumer to catch up to the messages during Streaming.  
     Latest - Ignores buffered messages and only consumes the subsequent messages
  */

  val messages = KafkaUtils.createDirectStream[String, String](
    ssc,
    LocationStrategies.PreferConsistent,
    ConsumerStrategies.Subscribe[String, String](topicsSet, kafkaParams))

  // Counts per Path
  val lines = messages.map(_.value)
  val pathsPerHour = lines.flatMap(_.split(" "))
  val pathCounts = pathsPerHour.map(x => (x, 1)).reduceByKey(_ + _)

  pathCounts.print()

  // Storing these counts of particular paths into a Database
  // For each batch interval, one RDD is produced.
  pathCounts.foreachRDD {
    rdd =>
      rdd.foreachPartition {
        it =>

          val url = "jdbc:mysql://127.0.0.1/url"
          val conn = DriverManager.getConnection(url, "root", "Password8$")
          val sql = "insert into finalurl2 set urlt=?,count=? on duplicate key update count=count+?"
          val del: PreparedStatement = conn.prepareStatement(sql)

          for (a <- it) {
            del.setString(1, a._1.toString)
            del.setInt(2, a._2)
            del.setInt(3, a._2)
            del.addBatch()
          }
          del.executeBatch()
          del.close()
          conn.close()

      }
  }
  ssc.start()
  ssc.awaitTermination()
}

