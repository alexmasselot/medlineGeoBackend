package ch.fram.medlineGeo.crunching.tools

import java.io.File

import ch.fram.medlineGeo.crunching.parsers.MedlineParser
import org.apache.spark.SparkContext
import play.api.Logger

/**
 * takes a directory dull of mdelnie exported xml.gz and put them in a parquet citation file
 *
 * @author Alexandre Masselot.
 */
object Step_1_MedlineXmlToSparkCitations extends PreProcessApp {

  import org.apache.spark.sql.SaveMode

  val medlineFileDir = config.getString("thirdParties.medlineDir")

  val medlineFiles = new File(medlineFileDir).listFiles.filter(_.getName endsWith ".gz").toList.map(_.getAbsolutePath)

  val sc = new SparkContext(sparkConf)
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
      .parquet(parquetCitations)
  }
  Logger.info("DONE")


}
