package app.service

import app.biz.{MysqlService, WordService}
import app.conf.Conf
import app.tools.BroadcastWrapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.LogManager
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable

object Consumer {
  @transient lazy val log = LogManager.getLogger(this.getClass)

  def service(): Unit = {
    log.info("[Consumer is started]")
    val session = SparkSession
      .builder()
      .appName("kafka")
      .master("local[*]")
      .getOrCreate()

    val ssc = new StreamingContext(session.sparkContext, Seconds(2))
    ssc.checkpoint(Conf.checkpointPath)

    val kafkaParams = Map(
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> Conf.brokers,
      ConsumerConfig.GROUP_ID_CONFIG -> Conf.group,
      ConsumerConfig.MAX_POLL_RECORDS_CONFIG -> Conf.maxPoll,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer]
    )
    val kafkaTopicDS = KafkaUtils
      .createDirectStream(ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](Set(Conf.topics), kafkaParams))
    //读取  用户词典库，加载成广播变量
    val dic = BroadcastWrapper[(Long, mutable.HashSet[String])](ssc, (System.currentTimeMillis, MysqlService.getUserDic))

    var segmentedStream = kafkaTopicDS.map(_.value()).repartition(10).transform(rdd => {
      if (System.currentTimeMillis - dic.value._1 > Conf.updateFreq) { //   更新频率  300000 //5min
        dic.update((System.currentTimeMillis, MysqlService.getUserDic()), true)
        log.info("词典已更新")
      }
      //   rdd中是消息( 语句)        调用分词服务，并传递  待争分的语句及用户词典
      rdd.flatMap(record => WordService.filterWithDic(record, dic.value._2))
    })
    //以entity_timestamp_beeword为key,统计本batch内各个key的计数
    val countedStream = segmentedStream.reduceByKey(_ + _)

    // countedStream  (单词，总和)
    countedStream.foreachRDD(MysqlService.save(_))

    ssc.start()
    ssc.awaitTermination()
  }
}
