package other

import java.util.Properties

import app.conf.Conf
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.log4j.LogManager
import org.apache.spark.sql.SparkSession

object Producer {
  @transient lazy val log = LogManager.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    run()
  }

  def run() = {
    //生产者配置
    val props = new Properties()
    props.put("bootstrap.servers", Conf.brokers)
    props.put("acks", "all") // 确认的级别
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer") //生产端用序列化
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props);
    //读取源数据
    var path = "D://novel1.txt"
    val session = SparkSession
      .builder()
      .appName("kafkaProduce")
      .master("local[*]")
      .getOrCreate()
    val cache = session.sparkContext
      .textFile(path)
      .cache()
    var count = 0
    cache.map(line => {
      var seq = line
      if (seq.length > 100)
        seq = seq.substring(0, 100)
      seq
    })
      .collect()
      .foreach(line => {
        var start = System.currentTimeMillis()
        //发送消息到kafka
        val pr = new ProducerRecord[String, String](Conf.topics, s"User_$count", line)
        producer.send(pr)
        log.info(s"[send successful] time:${System.currentTimeMillis() - start} content:(${pr.key()},${pr.value()})")
        Thread.sleep(10000)
        count += 1
      })
    producer.close()
  }
}
