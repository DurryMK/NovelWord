package app.biz

import app.dao.MysqlManager
import app.tools.{TableNameManager, TimeParse}
import org.apache.log4j.LogManager
import org.apache.spark.rdd.RDD

import scala.collection.mutable

object MysqlService {
  @transient lazy val log = LogManager.getLogger(this.getClass)

  /**
   * 加载用户词典
   */
  def getUserDic(): mutable.HashSet[String] = {
    val preTime = System.currentTimeMillis
    val sql = "select distinct(word) from user_words" //distinct 去重
    val conn = MysqlManager.getMysqlManager.getConnection
    val statement = conn.createStatement
    try {
      val rs = statement.executeQuery(sql)
      val words = mutable.HashSet[String]()
      while (rs.next) {
        words += rs.getString("word")
      }
      log.warn(s"[loadSuccess] load user words from db count: ${words.size}\ttime elapsed: ${System.currentTimeMillis - preTime}")
      words
    } catch {
      case e: Exception =>
        log.error("[loadError] error: ", e)
        mutable.HashSet[String]()
    } finally {
      statement.close()
      conn.close()
    }
  }

  def save(rdd: RDD[(String, Int)]) = {
    if (!rdd.isEmpty) {
      log.info(s"[Consumer操作了数据库]")
      //按分区循环RDD, 这样一个分区的所有数据只要一个Connection操作即可.
      rdd
        .foreachPartition(partitionRecords => {
          val preTime = System.currentTimeMillis
          //从连接池中获取一个连接
          val conn = MysqlManager.getMysqlManager.getConnection
          val statement = conn.createStatement
          try {
            conn.setAutoCommit(false)
            partitionRecords.foreach(record => {
              val tablename = TableNameManager.name
             // log.info(s"[当前操作的表]:$tablename")
              //log.info("待操作的记录>>>>>>>" + record)
              val createTime = System.currentTimeMillis()
              //按月建立一张新表存储数据
              var sql = s"CREATE TABLE if not exists `$tablename` ( `id` int(11) NOT NULL AUTO_INCREMENT, `word` varchar(64) NOT NULL, `count` int(11) DEFAULT '0', PRIMARY KEY (`id`), UNIQUE KEY `word` (`word`) ) ENGINE=InnoDB DEFAULT CHARSET=utf8;"
              statement.addBatch(sql) //将sql语句添加到批
              //log.info(s"[建表sql]:$sql")
              sql = s"insert into `$tablename` (word, count) VALUES('${record._1}',${record._2}) on duplicate key update count=count+values(count);" //   在更新表格中每个词的统计值时，用 on duplicate key进行词频数量的累加.
              //log.info(s"[添加sql]:$sql")
              //   word列为  unique列
              // 如果在INSERT语句末尾指定了ON DUPLICATE KEY UPDATE，并且插入行后会导致在一个UNIQUE索引或PRIMARY KEY中出现重复值，则执行旧行UPDATE；如果不会导致唯一值列重复的问题，则插入新行
              statement.addBatch(sql)
              //log.warn(s"[记录添加的批处理操作成功] record: ${record._1}, ${record._2}")
            })
            statement.executeBatch //执行批处理   -> 当一个 RDD中的数据量太大   batch存不下  mysql 缓存存不下，调整batch的批的大小
            conn.commit
            //log.warn(s"[保存的批处理操作完成] 耗时: ${System.currentTimeMillis - preTime}")
          } catch {
            case e: Exception =>
              log.error("[保存的批处理操作失败]", e)
          } finally {
            conn.setAutoCommit(true)
            statement.close()
            conn.close()
          }
        })
    }
  }
}
