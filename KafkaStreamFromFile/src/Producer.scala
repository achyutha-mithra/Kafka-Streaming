import java.util.Properties
import scala.io.Source
import org.apache.kafka.clients.producer._

class Producer(topic: String, broker: String, file: String) {
    
    val props = new Properties()
    props.put("bootstrap.servers", broker)
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    
    val producer = new KafkaProducer[String, String](props)
    
    val lines = Source.fromFile(file).getLines()
     
    //val pat = """[^\w\s\\$]"""
    //val pat2 = """\s\w{2}\s"""
    
    for(line <- lines) {
      // Removing punctuation and filtering out words whose length is lesser than 2
      var filteredText = line.replaceAll("""[^\w\s\\$]""", "").replaceAll("""\s\w{2}\s"""," ")
      filteredText = filteredText.toLowerCase()
      val record = new ProducerRecord[String, String](topic, "key", filteredText)
      producer.send(record)
    }
     
     producer.close()
}
