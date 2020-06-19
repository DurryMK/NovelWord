package app.service

import org.apache.log4j.LogManager

import scala.sys.process._

object PythonExecute {

  @transient lazy val log = LogManager.getLogger(this.getClass)
  def service(url:String,path:String): Unit = {
    s"python ./data/test2.py $url $path" !
  }
  def service(url:String): Unit = {
    log.info("[调用了PythonExecute]")
    s"python ./data/test2.py $url" !
  }
}
