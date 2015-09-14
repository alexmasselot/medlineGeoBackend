package ch.fram.medlineGeo.crunching.localize.geonames

import ch.fram.medlineGeo.crunching.SparkUtils
import org.specs2.mutable.Specification

/**
 * Created by alex on 13/09/15.
 */
class GeonameCountriesSpecs extends Specification {
  "GeoNameCountries" should {
    def cx = GeonameCountries.load

    "get config" in {
      GeonameCountries.srcFile must beEqualTo("test/resources/countryInfo-samples.txt")
    }

    "count" in {
      cx.size must beEqualTo(55)
    }

    "findById, present" in {
      val ogc = cx.findById(933860)
      ogc must beSome

      ogc.get must beEqualTo(GeonameCountry(933860, "BW", "Botswana"))
    }

    "findById, none" in {
      val ogc = cx.findById(42)
      ogc must beNone
    }

    "findById, present" in {
      val ogc = cx.findById(933860)
      ogc must beSome

      ogc.get must beEqualTo(GeonameCountry(933860, "BW", "Botswana"))
    }

    "findByName, present" in {
      cx.findByName("Botswana") must beEqualTo(List(GeonameCountry(933860, "BW", "Botswana")))
    }

    "findByName, not present" in {
      cx.findByName("Atlantis") must beEqualTo(Nil)
    }

    "findByAlternateName" in {
      cx.findByAlternateName("Islamic Republic of Afghanistan") must beEqualTo(List(GeonameCountry(1149361, "AF", "Afghanistan")))
    }

  }
}
