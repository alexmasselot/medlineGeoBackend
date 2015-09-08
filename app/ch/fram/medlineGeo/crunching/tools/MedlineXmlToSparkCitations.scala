package ch.fram.medlineGeo.crunching.tools

import java.io.File

import ch.fram.medlineGeo.crunching.parsers.MedlineParser
import com.typesafe.config.ConfigFactory
import play.api.Logger

/**
 * @author Alexandre Masselot.
 */
object MedlineXmlToSparkCitations extends App {

  import org.apache.spark.{SparkConf, SparkContext}
  import org.apache.spark.sql.SaveMode

  val config = ConfigFactory.defaultApplication()
  val sparkDataDir = config.getConfig("spark").getString("dataDir")
  val medlineFileDir = config.getString("thirdParties.medlineDir")


  val medlineFiles = new File(medlineFileDir).listFiles.filter(_.getName endsWith ".gz").toList.map(_.getAbsolutePath)
  val parquetFile = s"$sparkDataDir/citations.parquet"

  println(medlineFiles)
  val conf = new SparkConf(false)
    .setMaster("local[*]") // run locally with enough threads
    .setAppName("just a worksheet") // name in Spark web UI
    .set("spark.logConf", "true")

  val sc = new SparkContext(conf)
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)

  import sqlContext.implicits._

  for {
    filename <- medlineFiles
  } {
    Logger.info(s"importing $filename")
    val rdd = sc.parallelize(MedlineParser.parse(filename))
    rdd.toDF()
      .write
      .mode(SaveMode.Append)
      .parquet(parquetFile)
  }
  Logger.info("DONE")

  //  val path = "/Users/amasselo/private/dev/medline-graph/data/spark/processed/citations-with-coordinates-actual.parquet"
  //  val citations = sqlContext.read.parquet("/Users/amasselo/private/dev/medline-graph/data/spark/processed/citations-with-coordinates-actual.parquet")
  //  citations.printSchema()


}
