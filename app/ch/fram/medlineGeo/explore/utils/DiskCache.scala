package ch.fram.medlineGeo.explore.utils

import java.io.{FileWriter, File}

import play.api.Play
import play.api.cache.CacheApi
import play.api.Play.current

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.io.Source
import scala.reflect.ClassTag

/**
  * Created by alex on 06/01/16.
  * a super dummy cache, keeping everything in memory and on disk String -> String
  * All that because ehcache does not provide disk persistence without the enterprise edition...
  */
class DiskCache extends CacheApi{
  var mCache=mutable.Map[String, Any]()

  val path=Play.application.configuration.getString("disk.cache.path") match{
    case Some(x) => x
    case None => "/tmp/disk-cache"
  }

  val cachedOnly = Play.application.configuration.getString("disk.cache.cachedOnly").map(_.toBoolean).getOrElse(false)

  def getFile(key:String):File = return new File(s"$path$key")

  override def set(key: String, value: Any, expiration: Duration): Unit = ???

  override def get[T](key: String)(implicit evidence$2: ClassTag[T]): Option[T] = ???

  override def getOrElse[A](key: String, expiration: Duration)(orElse: => A)(implicit evidence$1: ClassTag[A]): A = {
    mCache.get(key) match{
      case Some(x:A) => x
      case None =>
        val file = getFile(key)
        val content = if(file.exists()){
          Source.fromFile(file).getLines() mkString "\n"
        }else{
          if(cachedOnly){throw new IllegalArgumentException(s"cached only access: key is not on file (consider turning off the disk.cache.cachedOnly param in applicaation.conf): $key")}
          file.getParentFile.mkdirs()
          val tmp = orElse.toString
          val w = new FileWriter(file)
          w.write(tmp)
          w.close
          tmp
        }
        mCache.put(key, content)
        content.asInstanceOf[A]
    }
  }


  override def remove(key: String): Unit = ???
}
