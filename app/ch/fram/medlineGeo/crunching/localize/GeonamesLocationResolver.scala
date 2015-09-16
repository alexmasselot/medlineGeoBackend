package ch.fram.medlineGeo.crunching.localize

import ch.fram.medlineGeo.crunching.localize.geonames.{GeonamesCountryLoader, GeonamesCityLoader, GeonamesCity}
import ch.fram.medlineGeo.models.Location

import scala.annotation.tailrec
import scala.util.{Success, Failure, Try}

/**
 * Created by Alexandre Masselot on 15/09/15.
 */

case class GeonamesResolutionAmbivalentException(message: String) extends Exception(message)

object GeonamesResolutionNotfoundException extends Exception()


object GeonamesLocationResolver extends LocationResolver {
  val cities = GeonamesCityLoader.load
  val countries = GeonamesCountryLoader.load

  val populationDisambiguationFactor = 10.0

  def resolveList(potentials: List[GeonamesCity]): Try[Location] = potentials match {
    case Nil => Failure(GeonamesResolutionNotfoundException)
    case c :: Nil => Success(c.location)
    case cities =>
      val sortedCities = cities.sortBy(-_.population)
      if (sortedCities.head.population >= populationDisambiguationFactor * sortedCities(1).population)
        Success(sortedCities.head.location)
      else
        Failure(GeonamesResolutionAmbivalentException(s"${sortedCities.head} / ${sortedCities(1)}"))
  }

  trait GeonamesLocationSingleResolver {
    def resolveOne(potentialCityCountry: PotentialCityCountry): Try[Location]
  }

  object oneResolverDirect extends GeonamesLocationSingleResolver {
    override def resolveOne(potentialCityCountry: PotentialCityCountry): Try[Location] = {
      val potentials = for {
        ct <- cities.findByName(potentialCityCountry.city)
        cn <- countries.findByName(potentialCityCountry.country)
        if ct.location.countryIso == cn.iso
      } yield ct

      resolveList(potentials.distinct)
    }
  }

  object oneResolverAlternate extends GeonamesLocationSingleResolver {
    override def resolveOne(potentialCityCountry: PotentialCityCountry): Try[Location] = {
      val potentials = for {
        ct <- cities.findByName(potentialCityCountry.city) ::: cities.findByAlternateName(potentialCityCountry.city)
        cn <- countries.findByName(potentialCityCountry.country) ::: countries.findByAlternateName(potentialCityCountry.country)
        if ct.location.countryIso == cn.iso
      } yield
          ct
      resolveList(potentials.distinct)
    }
  }

  object oneResolverCityOnly extends GeonamesLocationSingleResolver {
    override def resolveOne(potentialCityCountry: PotentialCityCountry): Try[Location] = {
      val potentials = cities.findByName(potentialCityCountry.city) :::
        cities.findByAlternateName(potentialCityCountry.city) :::
        cities.findByName(potentialCityCountry.country) :::
        cities.findByAlternateName(potentialCityCountry.country)
      potentials.distinct match {
        case Nil => Failure(GeonamesResolutionNotfoundException)
        case c :: Nil => Success(c)
        case c1 :: c2 :: cx => GeonamesResolutionAmbivalentException(s"$c1 / $c2")
      }
      resolveList(potentials.distinct)
    }
  }

  def tryAllOneResolver(affiliationHook: String, resolver: GeonamesLocationSingleResolver): Try[Location] = {
    @tailrec
    def handler(pcs: List[PotentialCityCountry]): Try[Location] = pcs match {
      case Nil => Failure(GeonamesResolutionNotfoundException)
      case x :: xs => resolver.resolveOne(x) match {
        case Success(loc) => Success(loc)
        case Failure(e: GeonamesResolutionAmbivalentException) => Failure(e)
        case Failure(_) => handler(xs)
      }
    }
    handler(PotentialCityCountry(affiliationHook))
  }

  val listOneResolvers: List[GeonamesLocationSingleResolver] =
    List(
      oneResolverDirect,
      oneResolverAlternate,
      oneResolverCityOnly
    )

  override def resolve(affiliationHook: String): Try[Location] = {
    @tailrec
    def handler(resolvers: List[GeonamesLocationSingleResolver]): Try[Location] = resolvers match {
      case Nil => Failure(GeonamesResolutionNotfoundException)
      case x :: xs => tryAllOneResolver(affiliationHook, x) match {
        case Success(loc) => Success(loc)
        case Failure(e: GeonamesResolutionAmbivalentException) => Failure(e)
        case Failure(_) => handler(xs)

      }
    }
    handler(listOneResolvers)
  }
}
