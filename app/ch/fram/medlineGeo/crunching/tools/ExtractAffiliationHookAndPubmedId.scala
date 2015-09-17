package ch.fram.medlineGeo.crunching.tools

import ch.fram.medlineGeo.crunching.{LocalizedAffiliationPubmedIds, AffiliationHook}
import org.apache.spark.SparkContext
import org.apache.spark.sql._
import play.api.Logger

import scala.collection.mutable.ArrayBuffer

/**
 *
 * gett all the citation and extracti unique affiliation hooks (the part needed for localization) together with  least of pubmed ids
 *
 * @author Alexandre Masselot.
 */
object ExtractAffiliationHookAndPubmedId extends PreProcessApp {
  val sc = new SparkContext(sparkConf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)

  import sqlContext.implicits._

  val df = sqlContext.read.parquet(parquetCitations)

  //we need case class (Product) for the flatmap
  case class StringCC(value: String)

  val dfe = (df.explode(df("authors.affiliations")) {
    case Row(l: ArrayBuffer[ArrayBuffer[String]]) => l.flatMap(xl => xl.map(x => StringCC(AffiliationHook(x)))).distinct
  }).select("pubmedId", "value").withColumnRenamed("value", "affiliationHook")

  dfe.map({
    case Row(id: Long, aff: String) => (aff, id)
  }).groupByKey().map({
    case (aff: String, ids: Seq[Long]) => LocalizedAffiliationPubmedIds(aff, ids.toList)
  })
    .saveAsObjectFile(objectsAffiliationPubmedIds)


  Logger.info("DONE")


}
