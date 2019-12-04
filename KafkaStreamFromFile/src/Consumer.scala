import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010._
import org.apache.log4j._

class Consumer(topics: String, bootstrap_server: String, group_name: String) {
 
    Logger.getLogger("org").setLevel(Level.ERROR)

    val sparkConf = new SparkConf().setAppName("DirectKafkaWordCount")
    .setMaster("local[*]")  // All available cores
    .set("spark.executor.memory","1g")
    
    val ssc = new StreamingContext(sparkConf, Seconds(1))

    val topicsSet = topics.split(",")
    
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> bootstrap_server,
      ConsumerConfig.GROUP_ID_CONFIG -> group_name,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      "auto.offset.reset" ->"earliest")
      
      
    val messages = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent, // it consistently distributes partitions across all executors
      ConsumerStrategies.Subscribe[String, String](topicsSet, kafkaParams))

    val lines = messages.map(_.value)
    val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1)).reduceByKey(_ + _)
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
}
