package scala.ch.fram.medlineGeo.crunching.localize

import ch.fram.medlineGeo.crunching.localize.GeonamesLocationResolver
import ch.fram.medlineGeo.crunching.localize.geonames.GeonamesCity
import ch.fram.medlineGeo.models._
import org.specs2.mutable.Specification

import scala.util.Success

/**
 * Created by alex on 15/09/15.
 */
class GeonamesLocationResolverSpecs extends Specification {
  def check(comment: String, affiliationHook: String, expLat: Double, expLng: Double, expectedCountryIso: String) = {
    s"""$comment: GeonamesLocationResolver.resolve("$affiliationHook")""" should {
      s"""get (($expLat, $expLng), "$expectedCountryIso") """ in {
        val tloc = GeonamesLocationResolver.resolve(affiliationHook)
        tloc must beASuccessfulTry
        val loc = tloc.get
        loc must beEqualTo(Location(GeoCoordinates(expLat, expLng), expectedCountryIso))
      }
    }
  }

  def checkFail(comment: String, affiliationHook: String) = {
    s"""$comment: GeonamesLocationResolver.resolve("$affiliationHook")""" should {
      s"""fail""" in {
        val tloc = GeonamesLocationResolver.resolve(affiliationHook)
        tloc must beAFailedTry
      }
    }
  }

  check("straight name", "College of Physicians and Surgeons, Columbia University, New York City, USA", 40.71427, -74.00597, "US")
  check("alternate", "College of Physicians and Surgeons, Columbia University, New York, USA", 40.71427, -74.00597, "US")
  check("spurious token", "College of Physicians and Surgeons, Columbia University, New York, badaboum, USA", 40.71427, -74.00597, "US")
  checkFail("city does not exist", "College of Physicians and Surgeons, Columbia University, New York les Bains, badaboum, USA")
  check("wtf", "College of Physicians and Surgeons, Columbia University, New York, NY, USA", 40.71427, -74.00597, "US")
  check("wtf", "CJ. N. Adam Memorial Hospital, Perrysburg, N. Y", 41.557, -83.62716, "US")
  check("city only, case", "CJ. N. Adam Memorial Hospital, pErrysburg, N. Y", 41.557, -83.62716, "US")
  check("no country, unique city", "Department of Medicine, University of Washington, Seattle 98195", 47.60621, -122.33207, "US")
  check("no country, city is way more populated", "Department of Medicine, University of Washington, London", 51.50853, -0.12574, "GB")
  checkFail("no country, city with similar population", "Department of Medicine, University of Washington, Lonpaf")
  check("unequivocal", "University of Paf le Chien, Shīnḏanḏ, Afghanistan", 33.30294, 62.1474, "AF")
  check("unequivocal, accent", "University of Paf le Chien, Shindand, Afghanistan", 33.30294, 62.1474, "AF")
  check("case issue", "University of Paf le Chien, shindand, afghanistan", 33.30294, 62.1474, "AF")
  check("unequivocal city, missing country", "University of Paf le Chien, shindand, prout", 33.30294, 62.1474, "AF")
  //checkFail("incompatible country", "University of Paf le Chien, Shīnḏanḏ, Lebanon")
  //checkFail("incompatible country, case issue", "University of Paf le Chien, shīnḏanḏ, lebanon")
  check("city in multiple countries, but country rules", "University of Paf le Chien, Tripoli, Lebanon", 34.43667, 35.84972, "LB")
  check("city in multiple countries, but country rules, case", "University of Paf le Chien, tripoli, lebanon", 34.43667, 35.84972, "LB")
  checkFail("multiple city", "University of Paf le Chien, Springfield, USA")
  checkFail("multiple city", "University of Paf le Chien, Springfield, United States")
  checkFail("multiple city case", "University of Paf le Chien, springfield, United States")
  checkFail("unknown city, unknow country", "University of Paf le Chien, prout la belle, Glork")
  check("city country eponyms", "blabla, Queen Mary Hospital, Hong Kong", 22.28552, 114.15769, "HK")

  "GeonamesLocationResolver.resolveList" should {
    "empty fails" in {
      GeonamesLocationResolver.resolveList(List()) must beAFailedTry
    }
    "single succees" in {
      val tl = GeonamesLocationResolver.resolveList(List(GeonamesCity(123, "paf", Location(GeoCoordinates(1, 2), "XX"), 90)))
      tl must beASuccessfulTry
      tl.get must beEqualTo(Location(GeoCoordinates(1, 2), "XX"))
    }
    "multiple populated enough" in {
      val tl = GeonamesLocationResolver.resolveList(List(
        GeonamesCity(123, "paf", Location(GeoCoordinates(1, 2), "XX"), 90),
        GeonamesCity(123, "paf", Location(GeoCoordinates(3, 4), "YY"), 9000),
        GeonamesCity(123, "paf", Location(GeoCoordinates(5, 6), "ZZ"), 600)
      ))
      tl must beASuccessfulTry
      tl.get must beEqualTo(Location(GeoCoordinates(3, 4), "YY"))
    }
    "multiple not diff enough in population" in {
      val tl = GeonamesLocationResolver.resolveList(List(
        GeonamesCity(123, "paf", Location(GeoCoordinates(1, 2), "XX"), 90),
        GeonamesCity(123, "paf", Location(GeoCoordinates(3, 4), "YY"), 9000),
        GeonamesCity(123, "paf", Location(GeoCoordinates(5, 6), "ZZ"), 6000)
      ))
      tl must beAFailedTry
    }
  }
}
