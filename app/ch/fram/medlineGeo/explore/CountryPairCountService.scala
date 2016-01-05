package ch.fram.medlineGeo.explore

import org.apache.spark.sql.DataFrame

import scala.collection.mutable

/**
 * Created by alex on 28/09/15.
 */
object CountryPairCountService extends SparkService {

  import org.apache.spark.sql.functions._
  import SparkCommons.sqlContext.implicits._
  import org.apache.spark.sql._

  lazy val df = SparkCommons.sqlContext.read.parquet(parquetCitationsLocated)
  val maxCountries = 30

  def size: Long = {
    df.count()
  }

  //the maxCountries countries with the topmost overall publications
  def getMostProductiveCountries: Set[String] =
    df.select("pubmedId", "locations.countryIso").
      explode[mutable.WrappedArray[String], String]("countryIso", "countryIso2")({
      case l: mutable.WrappedArray[String] => l.array.toList.distinct
    }).
      drop("countryIso").
      withColumnRenamed("countryIso2", "countryIso").
      groupBy(s"countryIso").
      agg(count("pubmedId")).
      withColumnRenamed("count(pubmedId)", "n").
      orderBy($"n".desc).
      select("countryIso").
      limit(maxCountries).
      collect().
      map(_(0).toString).
      toSet


  lazy val dfCountCountryPairs = {
    val countrySet = getMostProductiveCountries

    val fMultValues: (mutable.WrappedArray[String] => Boolean) = (xs: mutable.WrappedArray[String]) => xs.array.toList.distinct.size >= 2
    val udfMultiValues = udf(fMultValues)
    val fPairs: (mutable.WrappedArray[String] => List[(String, String)]) = { (xs: mutable.WrappedArray[String]) =>
      val uniques = xs.array.toList.distinct.filter(x => countrySet.contains(x)).sorted
      if (uniques.size > maxCountries) {
        List()
      } else {
        uniques.combinations(2).map(x => (x.head, x(1))).toList
      }
    }
    val udfPairs = udf(fPairs)

    val dfToFrom = df.select("pubmedId", "pubDate.year", "locations.countryIso").
      filter(udfMultiValues($"countryIso")).
      withColumn("countryPairs", udfPairs($"countryIso")).
      explode[mutable.WrappedArray[(String, String)], (String, String)]("countryPairs", "countryPair")({
        case l: mutable.WrappedArray[(String, String)] => l.array.toList
    }).
      drop("countryPairs").
      drop("countryIso").
      groupBy(s"countryPair", "year").
      agg(count("pubmedId")).
      withColumnRenamed("count(pubmedId)", "nbPubmedIds").
      withColumn("countryFrom", $"countryPair._1").
      withColumn("countryTo", $"countryPair._2").
      drop("countryPair").
      orderBy($"year")
      .cache

    //add columns with the total count for th to/from country for the given year
    val dfCountry = df.select("pubmedId", "locations.countryIso", "pubDate.year").
      explode[mutable.WrappedArray[String], String]("countryIso", "countryIso2")({
      case l: mutable.WrappedArray[String] => l.array.toList.filter(c => countrySet.contains(c))
    }).
      drop("countryIso").
      withColumnRenamed("countryIso2", "countryIso").
      groupBy("year", "countryIso").
      agg(count("pubmedId")).
      withColumnRenamed("count(pubmedId)", "nbPubmedIdsPerCountryYear")
      .withColumnRenamed("year", "year1")
      .cache

    val df1 = dfToFrom.join(dfCountry, dfToFrom("countryFrom") === dfCountry("countryIso") &&
      dfToFrom("year") === dfCountry("year1")).
      drop("countryIso").
      drop("year1").
      withColumnRenamed("nbPubmedIdsPerCountryYear", "nbPubmedIdTotalFrom")

    df1.join(dfCountry, df1("countryTo") === dfCountry("countryIso") &&
      df1("year") === dfCountry("year1")).
      drop("countryIso").
      drop("year1").
      withColumnRenamed("nbPubmedIdsPerCountryYear", "nbPubmedIdTotalTo")
      .cache
  }

  /**
   *
   * count by country, for a given year
   * @param fYear
   * @return
   */
  def countByYear(fYear: Int) = {
    dfCountCountryPairs.filter(s"year = $fYear")
  }
}
