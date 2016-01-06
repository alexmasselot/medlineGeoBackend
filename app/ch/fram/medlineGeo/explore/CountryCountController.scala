package ch.fram.medlineGeo.explore

import javax.inject.Inject

import ch.fram.medlineGeo.explore.utils.DiskCache
import play.api.mvc.{Action, Controller}

/**
  * Created by alex on 28/09/15.
  */
class CountryCountController @Inject()(cached: DiskCache) extends Controller {
  def index = Action {
    Ok("CountryCountController")
  }

  def countByYear(year: Int) = //cached(req => "rest-" + req.uri) {
    Action { req =>
      val ret = cached.getOrElse(req.uri)(CountryCountService.countByYear(year).toJSON.collect.mkString(",\n"))
      Ok("[\n" + ret + "\n]").as("application/json")
    }

  //  }
}
