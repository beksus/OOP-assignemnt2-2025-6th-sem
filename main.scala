import scala.io.Source
import scala.util.{Using, Try}
import java.io.File

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
    val file = new File(filename)

    if !file.exists() then
      println(s"Error: File '$filename' not found. Please place the CSV file in the correct directory.")
      System.exit(1)

    val data = Using.resource(Source.fromFile(filename)) { source =>
      val lines = source.getLines().toList
      val header = lines.head.split(",").map(_.trim.toLowerCase)
      val index = header.zipWithIndex.toMap

      lines.tail.flatMap { line =>
        val cols = line.split(",").map(_.trim)
        Try {
          DataRow(
            year = cols(index("year")).toInt,
            country = cols(index("country_name")),
            lifeExpectancy = toDouble(cols(index("life_expectancy"))),
            childMortality = toDouble(cols(index("child_mortality"))),
            schoolEnrollment = toDouble(cols(index("school_enrollment_secondary"))),
            healthcareCapacity = toDouble(cols(index("healthcare_capacity_index"))),
            healthDevRatio = toDouble(cols(index("health_development_ratio"))),
            forestArea = toDouble(cols(index("forest_area_pct")))
          )
        }.toOption
      }
    }

    if data.isEmpty then
      println("Error: No data loaded from CSV. Please check the file contents.")
      System.exit(1)

    // 1. Highest Life Expectancy
    val highestLife = data
      .filter(_.lifeExpectancy.isDefined)
      .maxByOption(_.lifeExpectancy.get)
    highestLife match
      case Some(row) =>
        println(s"1. Highest Life Expectancy: ${row.country} in ${row.year} with ${row.lifeExpectancy.get} years")
      case None =>
        println("1. No valid life expectancy data found.")

    // 2. Best Country in Health & Education (with indicator averages)
    val healthScores = data.groupMapReduce(_.country)(List(_))(_ ++ _).map { (country, rows) =>
      val valid = rows.filter(row =>
        row.lifeExpectancy.isDefined &&
        row.childMortality.isDefined &&
        row.schoolEnrollment.isDefined &&
        row.healthcareCapacity.isDefined &&
        row.healthDevRatio.isDefined
      )

      if valid.nonEmpty then
        val avgLife = valid.map(_.lifeExpectancy.get).sum / valid.size
        val avgMortality = valid.map(_.childMortality.get).sum / valid.size
        val avgSchool = valid.map(_.schoolEnrollment.get).sum / valid.size
        val avgHealth = valid.map(_.healthcareCapacity.get).sum / valid.size
        val avgRatio = valid.map(_.healthDevRatio.get).sum / valid.size
        val score = avgLife + avgSchool + avgHealth + avgRatio - avgMortality

        (country, score, avgLife, avgMortality, avgSchool, avgHealth, avgRatio)
      else (country, Double.MinValue, 0.0, 0.0, 0.0, 0.0, 0.0)
    }

    val bestHealthCountry = healthScores.maxByOption(_._2)
    bestHealthCountry match
      case Some((country, score, avgLife, avgMortality, avgSchool, avgHealth, avgRatio)) if score != Double.MinValue =>
        println(f"2. Best Country in Health & Education: $country")
        println(f"   Score: $score%.2f")
        println(f"   Average Life Expectancy: $avgLife%.2f")
        println(f"   Average Child Mortality: $avgMortality%.2f")
        println(f"   Average School Enrollment: $avgSchool%.2f")
        println(f"   Average Healthcare Capacity: $avgHealth%.2f")
        println(f"   Average Health Development Ratio: $avgRatio%.2f")
      case _ =>
        println("2. No valid health & education data found.")

    // 3. Highest Forest Area Loss (2000-2020)
    val forestYears = data.filter(row => row.forestArea.isDefined && (row.year == 2000 || row.year == 2020))
      .groupMapReduce(_.country)(List(_))(_ ++ _)
      .collect {
        case (country, values) if values.exists(_.year == 2000) && values.exists(_.year == 2020) =>
          val year2000 = values.find(_.year == 2000).get.forestArea.get
          val year2020 = values.find(_.year == 2020).get.forestArea.get
          (country, year2000 - year2020)
      }

    val highestLoss = forestYears.maxByOption(_._2)
    highestLoss match
      case Some((country, loss)) =>
        println(f"3. Highest Forest Area Loss: $country with $loss%.2f%% loss from 2000 to 2020")
      case None =>
        println("3. No valid forest area data found.")

  def toDouble(value: String): Option[Double] =
    Try(value.toDouble).toOption