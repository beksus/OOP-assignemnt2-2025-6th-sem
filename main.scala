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
    val filename = "Global_Development_Indicators_2000_2020.csv"

    val data = Using.resource(Source.fromFile(filename)) { source =>
      val lines = source.getLines().toList
      val header = lines.head.split(",").map(_.trim.toLowerCase)
      val index = header.zipWithIndex.toMap

      lines.tail.flatMap { line =>
        val cols = line.split(",").map(_.trim)
        try
          Some(DataRow(
            year = cols(index("year")).toInt,
            country = cols(index("country_name")),
            lifeExpectancy = toDouble(cols(index("life_expectancy"))),
            childMortality = toDouble(cols(index("child_mortality"))),
            schoolEnrollment = toDouble(cols(index("school_enrollment_secondary"))),
            healthcareCapacity = toDouble(cols(index("healthcare_capacity_index"))),
            healthDevRatio = toDouble(cols(index("health_development_ratio"))),
            forestArea = toDouble(cols(index("forest_area_pct")))
          ))
        catch case _ => None
      }
    }

    // Question 1: Highest Life Expectancy
    val highestLife = data
      .filter(_.lifeExpectancy.isDefined)
      .maxBy(_.lifeExpectancy.get)
    println("""
1. Which country had achieved the highest life expectancy in the dataset and in which year?
-------------------------------------------------------------------------------------------""")
    println(s"Answer: ${highestLife.country} in ${highestLife.year} with ${highestLife.lifeExpectancy.get} years")

    // Question 2: Best Country in Health & Education
    val healthScores = data.groupMapReduce(_.country)(List(_))(_ ++ _).map: (country, rows) =>
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

        (country, avgLife + avgSchool + avgHealth + avgRatio - avgMortality)
      else (country, Double.MinValue)

    val bestHealthCountry = healthScores.maxBy(_._2)
    println("""
2. Which country did well in Health & Education over the entire duration?
   Judged by: Life Expectancy, Child Mortality, School Enrollment, Healthcare Capacity, Health Development Ratio
-------------------------------------------------------------------------------------------""")
    println(f"Answer: ${bestHealthCountry._1} with composite score ${bestHealthCountry._2}%.2f")

    // Question 3: Forest Area Loss
    val forestYears = data.filter(row => row.forestArea.isDefined && (row.year == 2000 || row.year == 2020))
      .groupMapReduce(_.country)(List(_))(_ ++ _)
      .collect:
        case (country, values) if values.exists(_.year == 2000) && values.exists(_.year == 2020) =>
          val year2000 = values.find(_.year == 2000).get.forestArea.get
          val year2020 = values.find(_.year == 2020).get.forestArea.get
          (country, year2000 - year2020)

    val highestLoss = forestYears.maxBy(_._2)
    println("""
3. Which country had the highest loss of forest area from 2000 to 2020, and how much is the loss?
-------------------------------------------------------------------------------------------""")
    println(f"Answer: ${highestLoss._1} with ${highestLoss._2}%.2f%% forest loss from 2000 to 2020")

  def toDouble(value: String): Option[Double] =
    try Some(value.toDouble) catch case _ => None
