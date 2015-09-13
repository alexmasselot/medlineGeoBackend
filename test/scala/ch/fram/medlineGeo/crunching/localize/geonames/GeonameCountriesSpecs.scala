package ch.fram.medlineGeo.crunching.localize.geonames

import org.specs2.mutable.Specification

/**
 * Created by alex on 13/09/15.
 */
class GeonameCountriesSpecs extends Specification {
  "GeoNameCountries" should {
    "get config" in {
      GeonameCountries.srcFile must beEqualTo("test/resources/countryInfo-samples.txt")
    }
  }
}
