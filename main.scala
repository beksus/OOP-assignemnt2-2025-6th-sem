import scala.io.Source
import scala.util.Using

case class DataRow(
  year: Int,
  country: String,
  lifeExpectancy: Option[Double],
  childMortality: Option[Double],
  schoolEnrollment: Option[Double],
  healthcareCapacity: Option[Double],
  healthDevRatio: Option[Double],
  forestArea: Option[Double]
)

object DevelopmentIndicators:

  def main(args: Array[String]): Unit =
    val filename = "src/main/resources/Global_Development_Indicators_2000_2020.csv"
