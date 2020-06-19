package app.biz

import java.net.URLEncoder

import app.conf.Conf
import app.tools.{BroadcastWrapper, UnicodeTrans}
import org.apache.log4j.LogManager

import scala.collection.mutable
import scala.io.Source

object WordService {
  @transient lazy val log = LogManager.getLogger(this.getClass)

  def service(line: String): mutable.HashSet[String] = {
    var result = mutable.HashSet[String]()
    try {
      //记录请求开始的时间
      var start = System.currentTimeMillis()
      //对参数进行编码
      var param = URLEncoder.encode(line)
      //发起请求获取响应数据
      var content = Source.fromURL(Conf.segmentorHost + param)
      content
        .getLines()
        .next()
        .split(",")
        .foreach(c => {
          result.add(c)
        })
      //输出响应时间
      log.info("WordService [Response time]:" + (System.currentTimeMillis() - start))
      content.close()
      result
    } catch {
      case e: Exception => {
        e.printStackTrace()
        result
      }
    }
  }

  def filterWithDic(line: String, dic: mutable.HashSet[String]): Map[String, Int] = {
    //记录过滤开始的时间
    val start = System.currentTimeMillis
    //转换编码
    val unLine = UnicodeTrans.convert(line)
    log.info(s"[发送到分词服务器的数据]$unLine")
    //返回值
    var keyCount = Map[String, Int]()
    //参数为空时
    if (unLine == "" || unLine.isEmpty()) {
      log.warn("param is null:" + unLine)
      keyCount
    } else {
      try {
        // 开始分词请求
        var wordSet = retry(3)(service(unLine))
        // 进行词语统计 去掉需要过滤的词语
        wordSet.foreach(ws => {
          var flag = true
          for (word <- dic) {
            if (ws.contains(word) || ws.length < 2)
              flag = false
          }
          if (flag)
            keyCount += ws -> 1
        })
        //记录过滤总时长
        log.warn(s"WordService [Filter time]: ${System.currentTimeMillis - start}")
        keyCount
      } catch {
        case e: Exception => {
          e.printStackTrace()
          keyCount
        }
      }
    }
  }

  @annotation.tailrec
  def retry[T](n: Int)(fn: => T): T = {
    /*
    scala.util.Try 的结构与 Either 相似，Try 是一个 sealed 抽象类，具有两个子类，分别是 Succuss(x) 和 Failure(exception)。  模式匹配(  Option: Some(x)/None )
    Succuss会保存正常的返回值。 Failure 总是保存 Throwable 类型的值。
     */
    util.Try {
      fn
    } match { //利用scala中的Try函数:
      case util.Success(x) => {
        log.info(s"第${4 - n}次请求")
        x
      }
      case _ if n > 1 => { // _ 代表任意类型及任意值
        log.warn(s"[重试第 ${4 - n}次]")
        retry(n - 1)(fn) // 递归
      }
      case util.Failure(e) => {
        log.error(s"[segError] 尝试调用API失败了三次", e)
        throw e
      }
    }
  }
}
