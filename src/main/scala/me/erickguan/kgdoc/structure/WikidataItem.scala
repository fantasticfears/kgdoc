package me.erickguan.kgdoc.structure

import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.generic.extras._
import io.circe.syntax._

case class LangItem(language: String, value: String)
case class SiteLink(site: String, title: String)
sealed trait DataValue
case class StringDataValue(value: String) extends DataValue
case class GlobeCoordinateDataValue(latitude: Double,
                                    longitude: Double,
                                    precision: Double,
                                    globe: String)
    extends DataValue
case class QuantityDataValue(amount: String,
                             upperBound: String,
                             lowerBound: String,
                             unit: String)
    extends DataValue
@ConfiguredJsonCodec case class TimeDataValue(
    time: String,
    timezone: Long,
    before: Long,
    after: Long,
    precision: Long,
    @JsonKey("calendarmodel") calendarModel: String)
    extends DataValue
object TimeDataValue {
  implicit val config: Configuration = Configuration.default
}
object DataValue {
  implicit val decodeDataValue: Decoder[DataValue] =
    Decoder.instance[DataValue](c => {
      c.downField("type").as[String].flatMap {
        case "string" => c.as[StringDataValue] // has to be treated differently
        // we don't use `wikibase-entityid`
        // case "wikibase-entityid" => valueField.as[Map[String, String]]
        case "globecoordinate" =>
          c.downField("value").as[GlobeCoordinateDataValue]
        case "quantity" => c.downField("value").as[QuantityDataValue]
        case "time"     => c.downField("value").as[TimeDataValue]
      }
    })
}
case class Snak(snaktype: String,
                property: String,
                datatype: String,
                datavalue: DataValue)
case class Claim(`type`: String,
                 mainsnak: Snak,
                 rank: String,
                 qualifiers: Map[String, List[Snak]])
case class WikidataItem(id: String,
                        `type`: String,
                        labels: Map[String, LangItem],
                        descriptions: Map[String, LangItem],
                        aliases: Map[String, List[LangItem]],
                        claims: List[Map[String, Claim]],
                        sitelinks: Map[String, SiteLink])
