package ch.fram.medlineGeo.explore

import javax.inject.Inject

import play.api.cache.Cached
import play.api.mvc.{Action, Controller}

/**
 * Created by alex on 28/09/15.
 */
class CountryPairCountController @Inject()(cached: Cached) extends Controller {
  def index = Action {
    Ok("CountryCountController")
  }

  def count = cached(req => "rest-" + req.uri) {
    Action {
      Ok(CountryCountService.size.toString)
    }
  }

  def countByYear(year: Int) = cached(req => "rest-" + req.uri) {
    Action {
      val ret = CountryPairCountService.countByYear(year).toJSON.collect.mkString(",\n")
      Ok("[\n"+ret+"\n]").as("application/json")
    }
  }
}
