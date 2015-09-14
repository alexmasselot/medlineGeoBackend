package ch.fram.medlineGeo.crunching

import java.io.File

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.SQLContext

/**
 * Created by alex on 13/09/15.
 */
class SparkUtils(val sqlContext: SQLContext) {
  def text2parquet(txtFilename: String, parquetFilename: String) = {
    val f = new File(parquetFilename)
    f.getParentFile.mkdirs()

    val df = sqlContext.read.format("com.databricks.spark.csv")
      .option("comment", "#")
      .option("header", "true")
      .load(txtFilename)
    df.write
      .parquet(parquetFilename)
  }
}

object SparkUtils{
  def apply():SparkUtils = {
    val sc = new SparkContext(defaultConf)
    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    new SparkUtils(sqlContext)
  }
  def defaultConf = {
    new SparkConf(false)
      .setMaster("local[*]")
      .setAppName("medlineGeo")
      .set("spark.logConf", "true")
  }
}