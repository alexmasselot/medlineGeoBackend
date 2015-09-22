package ch.fram.medlineGeo.crunching.tools

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import ch.fram.medlineGeo.crunching.LocalizedAffiliationPubmedIds
import ch.fram.medlineGeo.crunching.localize.{GeonamesLocationResolver, LocationResolver}
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import play.api.Logger

import scala.concurrent.duration._
import scala.util.{Failure, Try}

/**
 *
 * Step 3
 * Take the exported location + pubmedids and save them in a classic binray stream object file
 *
 * @author Alexandre Masselot.
 */
object Step_3_AffiliationHookAndPubmedIdsSerializeInit extends PreProcessApp {
  val sc = new SparkContext(sparkConf)


  class SerializeWriter(filename:String){
    val oos = new ObjectOutputStream(new FileOutputStream(filename))

    def append(lapi:LocalizedAffiliationPubmedIds) = oos.writeObject(lapi)

    def close = oos.close()
  }

  class WriterActor(filename: String) extends Actor {
    val writer = new SerializeWriter(filename)

    override def receive: Receive = {
      case lapi: LocalizedAffiliationPubmedIds => writer.append(lapi)
      case "EOF" => writer.close
        context.system.shutdown()
    }
  }


  val system = ActorSystem("medline-geo-resolver")
  val writerActor = system.actorOf(Props(classOf[WriterActor], nextResolvedAffiliationPubmedIdsObjectsFilename), "location-writer")
  implicit val timeout = Timeout(5 seconds)


  val rdd: RDD[LocalizedAffiliationPubmedIds] = sc.objectFile(objectsAffiliationPubmedIdsInit)


  val rddOut = rdd
    .sortBy(-_.citationCount)
    .foreach({
    x => writerActor ! x
  })

  writerActor ! "EOF"
  Logger.info("DONE")

}
