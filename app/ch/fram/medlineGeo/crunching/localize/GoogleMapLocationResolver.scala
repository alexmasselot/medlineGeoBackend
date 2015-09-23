package ch.fram.medlineGeo.crunching.localize

import ch.fram.medlineGeo.models.{GeoCoordinates, Location}
import com.google.maps.errors.ApiException
import com.google.maps.model.{AddressComponentType, AddressType, AddressComponent}
import com.google.maps.{GeocodingApi, GeoApiContext}
import play.api.Logger

import scala.util.{Success, Failure, Try}

/**
 * Created by alex on 18/09/15.
 */
class GoogleMapLocationResolver(val apiKey: String, val limit: Int) extends LocationResolver {
  def conflictingMinDistance = 50 * 1000

  case class LocationResolutionCannotFindCountryException(message: String) extends Exception(message)

  case class LocationResolutionTooDistantException(l1: Location, l2: Location) extends Exception(s"$l1 / $l2")

  lazy val context = new GeoApiContext().setApiKey(apiKey);

  def getCountryFromAddress(components: Array[AddressComponent]): Option[String] =
    components.toList
      .filter({
      c =>
        c.types.toList.contains(AddressComponentType.COUNTRY)
    })
      .map(_.shortName)
      .headOption

  var cpt = 0;

  override def resolve(affiliationHook: String): Try[Location] = {
    cpt = cpt + 1
    if (cpt >= limit) {
      Failure(LocationResolutionSkipException("google api limit reache"))
    } else {
      try {
        val locations = GeocodingApi.geocode(context, affiliationHook)
          .await()
          .toList
          .map({
          x =>
            getCountryFromAddress(x.addressComponents) map { countryIso =>
              Location(
                GeoCoordinates(x.geometry.location.lat, x.geometry.location.lng),
                countryIso
              )
            }
        })
        locations.filter(_.isDefined)
          .map(_.get) match {
          case Nil => Failure(LocationResolutionNotfoundException)
          case x1 :: x2 :: Nil if x1.coordinates.distance(x1.coordinates) > conflictingMinDistance => Failure(LocationResolutionTooDistantException(x1, x2))
          case x :: xs => Success(x)
        }
      }catch{
        case e:ApiException => Failure(LocationResolutionSkipException(s"google ApiException ${e.getMessage}"))
      }
    }
  }

}
