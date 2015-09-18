package ch.fram.medlineGeo.crunching.tools

import java.io.File
import ch.fram.medlineGeo.crunching.LocalizedAffiliationPubmedIds
import ch.fram.medlineGeo.crunching.localize.{LocationResolver, GeonamesLocationResolver}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{SaveMode, SQLContext}

import scala.collection.JavaConversions._

import org.apache.spark.SparkContext
import play.api.Logger

import scala.util.{Failure, Success}

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

  val rddOut = rdd
    .sortBy(-_.citationCount)
    .map({ lapi =>
    lapi.location match {
        //we have a location
      case Some(loc) => lapi
        //we have no location but the resolver has already bee tried
      case None if (lapi.locResolverTried.contains(resolverName)) => lapi
        //we have no location and the resolver has not been tried
      case _ => resolver.resolve(lapi.affiliationHook) match {
        case Success(loc) => lapi.resolveSuccess(loc, resolverName)
        case Failure(e) => lapi.resolveFailure(resolverName)
      }
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
