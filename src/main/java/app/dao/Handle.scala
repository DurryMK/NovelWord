package app.dao

import java.util.Properties

import app.conf.Conf
import other.Producer.log
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.log4j.LogManager
import org.apache.spark.sql.SparkSession

object Handle {

  @transient lazy val log = LogManager.getLogger(this.getClass)

  def service(path: String): Unit = {
    var session = SparkSession
      .builder()
      .appName("kafka")
      .master("local[*]")
      .getOrCreate()
    //生产者配置
    val props = new Properties()
    props.put("bootstrap.servers", Conf.brokers)
    props.put("acks", "all") // 确认的级别
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer") //生产端用序列化
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props);
    val cache = session.sparkContext
      .textFile(path)
      .cache()
    var count = 0
    log.info("[开始上传数据]")
    val start = System.currentTimeMillis()
    cache.map(line => {
      line.replace("style5();", "").replace("style6();", "")
    })
      .collect()
      .foreach(line => {
        //发送消息到kafka
        val pr = new ProducerRecord[String, String](Conf.topics, s"User_$count", line)
        producer.send(pr)
        log.info(s"[send successful] content:(${pr.key()},${pr.value()})")
        count += 1
      })
    log.info(s"[send time] ${System.currentTimeMillis() - start} ")
    producer.close()
  }

}
