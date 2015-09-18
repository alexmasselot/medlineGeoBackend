package ch.fram.medlineGeo.crunching.localize

import ch.fram.medlineGeo.crunching.localize.geonames.{GeonameDirectory, GeonamesCountryLoader, GeonamesCityLoader, GeonamesCity}
import ch.fram.medlineGeo.models.Location

import scala.annotation.tailrec
import scala.util.{Success, Failure, Try}

/**
 * Created by Alexandre Masselot on 15/09/15.
 */



object GeonamesLocationResolver extends LocationResolver {
  val cities:GeonameDirectory[GeonamesCity] = GeonamesCityLoader.load
  val countries = GeonamesCountryLoader.load

  val populationDisambiguationFactor = 10.0

  def resolveList(potentials: List[GeonamesCity]): Try[Location] = potentials match {
    case Nil => Failure(LocationResolutionNotfoundException)
    case c :: Nil => Success(c.location)
    case cities =>
      val sortedCities = cities.sortBy(-_.population)
      if (sortedCities.head.population >= populationDisambiguationFactor * sortedCities(1).population)
        Success(sortedCities.head.location)
      else
        Failure(LocationResolutionConflictException(s"${sortedCities.head} / ${sortedCities(1)}"))
  }

  trait GeonamesLocationSingleResolver {
    def resolveOne(potentialCityCountry: PotentialCityCountry): Try[Location]
  }

  /**
   * city & country are found and are coherent (the city name matches the country)
   */
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

  /**
   * try all possibilites of combination with alternate names and keep the country coherent pairs
   */
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

  /**
   * locate only on city, if the name is unique
   */
  object oneResolverCityOnly extends GeonamesLocationSingleResolver {
    override def resolveOne(potentialCityCountry: PotentialCityCountry): Try[Location] = {
      val potentials = cities.findByName(potentialCityCountry.city) :::
        cities.findByAlternateName(potentialCityCountry.city) :::
        cities.findByName(potentialCityCountry.country) :::
        cities.findByAlternateName(potentialCityCountry.country)
      potentials.distinct match {
        case Nil => Failure(LocationResolutionNotfoundException)
        case c :: Nil => Success(c)
        case c1 :: c2 :: cx => LocationResolutionConflictException(s"$c1 / $c2")
      }
      resolveList(potentials.distinct)
    }
  }

  /**
   * Transforms the affiliationHook into a list of PotentialCityCountry and apply the passed resolver.
   * If a GeonamesResolutionAmbivalentException is found, stop there
   *
   * @param affiliationHook
   * @param resolver
   * @return
   */
  def tryAllOneResolver(affiliationHook: String, resolver: GeonamesLocationSingleResolver): Try[Location] = {
    @tailrec
    def handler(pcs: List[PotentialCityCountry]): Try[Location] = pcs match {
      case Nil => Failure(LocationResolutionNotfoundException)
      case x :: xs => resolver.resolveOne(x) match {
        case Success(loc) => Success(loc)
        case Failure(e: LocationResolutionConflictException) => Failure(e)
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
      case Nil => Failure(LocationResolutionNotfoundException)
      case x :: xs => tryAllOneResolver(affiliationHook, x) match {
        case Success(loc) => Success(loc)
        case Failure(e: LocationResolutionConflictException) => Failure(e)
        case Failure(_) => handler(xs)

      }
    }
    handler(listOneResolvers)
  }
}
