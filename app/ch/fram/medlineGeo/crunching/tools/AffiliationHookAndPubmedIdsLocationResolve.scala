package ch.fram.medlineGeo.crunching.tools

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



  val currentFile = latestResolvedAffiliationPubmedIdsObjectsFilename
  val nextFile = nextResolvedAffiliationPubmedIdsObjectsFilename

  Logger.info(s"$currentFile -> $nextFile")

  val rdd: RDD[LocalizedAffiliationPubmedIds] = sc.objectFile(currentFile)

  val resolverName: String = "geonames"
  val resolver: LocationResolver = GeonamesLocationResolver
  val resolverMaxCounter:Int = Int.MaxValue

  class LocationResolverActor(resolver:LocationResolver, maxCount:Int) extends Actor{
    var cpt = 0;
    override def receive: Receive = {
      case _ if cpt >= maxCount =>
        sender ! Failure(LocationResolutionSkipException("reach limit"))
      case affiliationHook:String =>
        cpt = cpt+1;
        sender ! resolver.resolve(affiliationHook)
    }
  }

  val system = ActorSystem("medline-geo-resolver")
  val locationResolverActor = system.actorOf(Props(classOf[LocationResolverActor], resolver, resolverMaxCounter), "location-resolver")
  implicit val timeout = Timeout(5 seconds)

  val rddOut = rdd
    .sortBy(-_.citationCount)
    .map({ lapi =>
    lapi.location match {
        //we have a location
      case Some(loc) => lapi
        //we have no location but the resolver has already bee tried
      case None if (lapi.locResolverTried.contains(resolverName)) => lapi
        //we have no location and the resolver has not been tried
      case _ => (locationResolverActor ? lapi.affiliationHook).onSuccess({ case tryLoc:Try[Location] =>
         tryLoc match {
          case Success(loc) => lapi.resolveSuccess(loc, resolverName)
          case Failure(e:LocationResolutionSkipException) => lapi
          case Failure(e) => lapi.resolveFailure(resolverName)
        }
      })
    }
  })

  Logger.info(s"saving $nextFile")
  rddOut.saveAsObjectFile(nextFile)

  val rdd2: RDD[LocalizedAffiliationPubmedIds] = sc.objectFile(nextFile)

  val sqlContext = new SQLContext(sc)
  import sqlContext.implicits._

  Logger.info(s"reading back and piping to parquet $parquetAffiliationPubmedIds")
  rdd2.toDF().write.mode(SaveMode.Overwrite).parquet(parquetAffiliationPubmedIds)


}
