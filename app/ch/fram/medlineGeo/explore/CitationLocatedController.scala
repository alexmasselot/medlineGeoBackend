package ch.fram.medlineGeo.explore

import play.api.mvc.{Action, Controller}

/**
 * Created by alex on 28/09/15.
 */
object CitationLocatedController extends Controller{
  def index = Action {
    Ok("hello workd")
  }

  def count = Action {
    Ok(CitationLocatedService.count.toString)
  }

  def countByHexagon(radius:Double, year:Option[Int]) = Action {
    val ret =  CitationLocatedService.countByHexagon(radius, year.getOrElse(2014)).toJSON.collect.mkString("\n")
    Ok(ret)
  }
}
