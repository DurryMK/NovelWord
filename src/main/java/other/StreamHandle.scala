package other

import java.util.Properties

import app.conf.Conf
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 动态读取日志
 * 筛选之后存入数据库
 **/
object StreamHandle {
  def main(args: Array[String]): Unit = {
    service()
  }

  def service(): Unit = {
    println("StreamHandle is started")
    val conf = new SparkConf().setMaster("local[*]").setAppName("logHandle")
    val ssc = new StreamingContext(conf, Seconds(2))
    val session = SparkSession
      .builder()
      .appName("log")
      .master("local[*]")
      .getOrCreate()

    val input = "./logs/" //日志存储路径
    //配置数据库连接参数
    val prop = new Properties()
    prop.setProperty("user", "root")
    prop.setProperty("password", "sys123")
    //生产者配置
    val props = new Properties()
    props.put("bootstrap.servers", Conf.brokers)
    props.put("acks", "all") // 确认的级别
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer") //生产端用序列化
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props);
    //动态监听
    ssc.textFileStream(input).foreachRDD(_.
      map(line => {

        var encode = new String(line.getBytes(), "utf-8")
          .replace("style5();", "")
          .replace("style6();", "")
        encode

      })
      .foreach(line => {
        val start = System.currentTimeMillis()

        //发送消息到kafka
        val pr = new ProducerRecord[String, String](Conf.topics, "User", line)
        producer.send(pr)
        println(s"[send successful] time:${System.currentTimeMillis() - start} content:(${pr.key()},${pr.value()})")
      })
    )
    ssc.start()
    ssc.awaitTermination()
  }
}
