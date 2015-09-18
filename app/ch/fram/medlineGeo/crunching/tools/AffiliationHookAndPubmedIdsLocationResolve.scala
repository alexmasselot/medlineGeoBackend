package ch.fram.medlineGeo.crunching.tools

import java.io.{FileOutputStream, ObjectOutputStream}

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import ch.fram.medlineGeo.crunching.LocalizedAffiliationPubmedIds
import ch.fram.medlineGeo.crunching.localize.{GeonamesLocationResolver, LocationResolutionSkipException, LocationResolver}
import ch.fram.medlineGeo.models.Location
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SQLContext, SaveMode}
import play.api.Logger

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 * gett all the citation and extracti unique affiliation hooks (the part needed for localization) together with  least of pubmed ids
 *
 * @author Alexandre Masselot.
 */
object AffiliationHookAndPubmedIdsLocationResolve extends PreProcessApp {
  val sc = new SparkContext(sparkConf)


  class SerializeLocalizedAffiliationPubmedIdsWriter(filename: String) extends Actor {
    val oos = new ObjectOutputStream(new FileOutputStream(filename))

    override def receive: Receive = {
      case lapi: LocalizedAffiliationPubmedIds => oos.writeObject(lapi)
      case "EOF" => oos.close()
    }
  }

  val currentFile = latestResolvedAffiliationPubmedIdsObjectsFilename
  val nextFile = nextResolvedAffiliationPubmedIdsObjectsFilename

  Logger.info(s"$currentFile -> $nextFile")

  val rdd: RDD[LocalizedAffiliationPubmedIds] = sc.objectFile(currentFile)

  val resolverName: String = "geonames"
  val resolver: LocationResolver = GeonamesLocationResolver
  val resolverMaxCounter: Int = Int.MaxValue


  val sout = new SerializeLocalizedAffiliationPubmedIdsWriter("/tmp/a.obj")
  val rddOut = rdd
    .sortBy(-_.citationCount)
    .foreach({
    x => sout.append(x)
  })

  Logger.info("DONE")

}
