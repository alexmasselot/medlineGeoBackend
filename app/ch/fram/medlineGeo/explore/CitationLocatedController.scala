package ch.fram.medlineGeo.explore

import javax.inject.Inject

import play.api.Play
import play.api.cache.Cached
import play.api.mvc.{Action, Controller}

/**
 * Created by alex on 28/09/15.
 */
class CitationLocatedController @Inject()(cached: Cached) extends Controller {
  def index = Action {
    Ok("hello workd")
  }

  def count = cached(req => "rest-" + req.uri) {
    Action {
      Ok(CitationLocatedService.size.toString)
    }
  }

  def countByHexagon(radius: Double, year: Option[Int]) = cached(req => "rest-" + req.uri) {
    Action {
      val ret = CitationLocatedService.countByHexagon(radius, year.getOrElse(2014)).toJSON.collect.mkString(",\n")
      Ok("[\n"+ret+"\n]").as("application/json")
    }
  }
}
