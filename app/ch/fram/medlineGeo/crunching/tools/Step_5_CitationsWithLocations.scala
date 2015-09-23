package ch.fram.medlineGeo.crunching.tools

import ch.fram.medlineGeo.models._
import org.apache.spark.SparkContext
import play.api.Logger
import org.apache.spark.sql._
import scala.collection.mutable.ArrayBuffer

/**
 *
 * gett all the citation and extracti unique affiliation hooks (the part needed for localization) together with  least of pubmed ids
 *
 * @author Alexandre Masselot.
 */
object Step_5_CitationsWithLocations extends PreProcessApp {
  val sc = new SparkContext(sparkConf)
  val sqlContext = new SQLContext(sc)

  import sqlContext.implicits._

  Logger.info(s"from json to parquet $parquetAffiliationPubmedIdsLocated")
  sqlContext.read.json(jsonAffiliationPubmedIdsLocated).write.mode(org.apache.spark.sql.SaveMode.Overwrite).parquet(parquetAffiliationPubmedIdsLocated)

  val df = sqlContext.read.parquet(parquetAffiliationPubmedIdsLocated)

  Logger.info("group by pubmed ids")
  val pubmedIdsLoc = df.
    filter("location is not null").
    select("pubmedIds", "location").
    withColumn("lat", df("location.coordinates.lat")).
    withColumn("lng", df("location.coordinates.lng")).
    withColumn("countryIso", df("location.countryIso")).
    explode[ArrayBuffer[Long], Long]("pubmedIds", "pubmedId")({
    case l: ArrayBuffer[Long] => l.toList
  }).
    drop("pubmedIds").
    drop("location")

  val dfPubmedId2Coordinates = pubmedIdsLoc.rdd.map({ case Row(lat: Double, lng: Double, countryIso: String, pmid: Long) => (pmid, Location(GeoCoordinates(lat, lng), countryIso)) }).
    groupByKey().
    map({ case (pmid: Long, coords: Iterable[Location]) => (pmid, coords.toList.distinct) }).
    toDF("pmid", "locations")

  Logger.info(s"joining citation+location $parquetCitationsLocated")
  val dfCitations = sqlContext.read.parquet(parquetCitations)
  val dfJoined = dfCitations.join(dfPubmedId2Coordinates,
    dfCitations("pubmedId") === dfPubmedId2Coordinates("pmid")).
    drop("pmid")
  dfJoined.write.mode(org.apache.spark.sql.SaveMode.Overwrite).parquet(parquetCitationsLocated)

  Logger.info("DONE")


}
