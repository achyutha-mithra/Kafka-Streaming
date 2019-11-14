object KafkaMain {
  def main(args: Array[String]){
    val consumer = new Consumer("topic1","localhost:9092","consumer-group")
  }
}
