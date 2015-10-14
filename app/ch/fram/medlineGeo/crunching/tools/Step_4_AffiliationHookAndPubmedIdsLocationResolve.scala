package ch.fram.medlineGeo.crunching.tools

import java.io._

import ch.fram.medlineGeo.crunching.LocalizedAffiliationPubmedIds
import ch.fram.medlineGeo.crunching.localize.{GoogleMapLocationResolver, GeonamesLocationResolver, LocationResolutionSkipException, LocationResolver}
import ch.fram.medlineGeo.crunching.tools.JsonSerializer._
import org.apache.commons.io.FileUtils
import play.api.Logger
import play.api.libs.json.Json

import scala.util.{Failure, Success, Try}
/**
 *
 * Step 4 : open the last serilized object files, apply a resolver and save into the next
 *
 * @author Alexandre Masselot.
 */
object Step_4_AffiliationHookAndPubmedIdsLocationResolve extends PreProcessApp {

  class SerializeReader(filename: String) extends Iterator[LocalizedAffiliationPubmedIds] {
    val ois = new ObjectInputStream(new FileInputStream(filename))

    var nextOne: Try[LocalizedAffiliationPubmedIds] = Failure(new UnsupportedOperationException())
    tryNext

    override def hasNext: Boolean = nextOne.isSuccess

    override def next(): LocalizedAffiliationPubmedIds = {
      val n = nextOne.get
      tryNext
      n
    }

    def tryNext: Unit = {
      nextOne = Try {
        ois.readObject().asInstanceOf[LocalizedAffiliationPubmedIds]
      }
    }
  }

  class SerializeWriter(filename: String) {
    val oos = new ObjectOutputStream(new FileOutputStream(filename))

    def append(lapi: LocalizedAffiliationPubmedIds) = oos.writeObject(lapi)

    def close = oos.close()
  }


  Logger.info(s"setting up resolver")
  val currentFile = latestResolvedAffiliationPubmedIdsObjectsFilename.get
  val nextFile = nextResolvedAffiliationPubmedIdsObjectsFilename

  val (resolverName, resolver) =  if(currentFile.endsWith("000")){
    ("geonames", GeonamesLocationResolver)
  }else{
    ("google geocoding", new GoogleMapLocationResolver(privateConfig.getString("google.api.key"), 2300))
  }

  Logger.info(s"$resolverName: $currentFile -> $nextFile")

  val reader = new SerializeReader(currentFile)
  val writer = new SerializeWriter(nextFile)
  for {lapi <- reader} {
    val newLapi = lapi.location match {
      //we have a location
      case Some(loc) => lapi
      //we have no location but the resolver has already bee tried
      case None if (lapi.locResolverTried.contains(resolverName)) => lapi
      //we have no location and the resolver has not been tried
      case _ =>
        resolver.resolve(lapi.affiliationHook) match {
          case Success(loc) => lapi.resolveSuccess(loc, resolverName)
          case Failure(e: LocationResolutionSkipException) => lapi
          case Failure(e) => lapi.resolveFailure(resolverName)
        }
    }
    writer.append(newLapi)
  }
  writer.close

  Logger.info(s"json to $jsonAffiliationPubmedIdsLocated")
  FileUtils.deleteDirectory(new File(jsonAffiliationPubmedIdsLocated))
  new File(jsonAffiliationPubmedIdsLocated).mkdirs()

  val nPerFile = 50000
  var i = 0
  for {
    slice <- new SerializeReader(nextFile).sliding(nPerFile, nPerFile)

  } {
    val fname = f"$jsonAffiliationPubmedIdsLocated/$i%03d.json"
    Logger.info(fname)
    val writer = new FileWriter(fname)
    slice.foreach(lapi => writer.write(Json.stringify(Json.toJson(lapi))+"\n"))
    writer.close()
    i=i+1
  }

  Logger.info("DONE")

}
