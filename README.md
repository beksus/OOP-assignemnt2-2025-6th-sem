# Global Development Indicators Analyzer

A Scala 3 console application for analyzing global development indicators from **2000 to 2020**, created for the 2025 sixth-semester OOP Assignment 2.

The project combines object-oriented design with functional collection operations to answer three questions:

1. Which country-year record has the highest life expectancy?
2. Which country performs best across the selected health and education indicators?
3. Which country has the largest decrease in forest-area share between 2000 and 2020?

> **Status:** academic analysis project. The source and dataset are included, but the dataset path needs adjustment before running. CSV parsing and interpretation limitations are documented below.

## Analyses

### Life expectancy

`LifeExpectancyAnalyzer` selects the record with the greatest available life-expectancy value **strictly below 100** and prints its country, year, and value.

The cutoff is specific to this analyzer; it is not applied to the health and education analysis.

### Health and education

`HealthEducationAnalyzer` uses records with values for all five indicators:

- Life expectancy
- Child mortality
- Secondary-school enrollment
- Healthcare capacity index
- Health development ratio

Each indicator is normalized using the minimum and maximum across all eligible records. Higher values receive higher scores, except child mortality, where lower values receive higher scores.

```text
Normalized score = (value - minimum) / (maximum - minimum)
Child mortality score = (maximum - value) / (maximum - minimum)
```

An indicator with no variation receives a score of zero. The five scores are equally weighted to produce a score per record, then averaged across each country's eligible records. The analyzer prints the country with the highest average, rounded to four decimal places.

Countries do not need complete coverage of every year to qualify. This is a project-defined composite score, not an official development index.

### Forest-area change

`ForestLossAnalyzer` compares countries with available forest-area values in both endpoint years:

```text
Change = forest_area_pct in 2000 - forest_area_pct in 2020
```

It selects the largest decrease. Because the source field is a percentage, the result is a difference in **percentage points**, not a relative percentage loss or an area in square kilometers. The current console output labels the result with `%`.

## Object-oriented design

| Component | Responsibility |
| --- | --- |
| `DataRow` | Case class representing one country-year record; optional measurements use `Option[Double]`. |
| `Analyzer` | Shared trait defining `analyze(data: List[DataRow]): Unit`. |
| `LifeExpectancyAnalyzer` | Finds the highest eligible life-expectancy record. |
| `HealthEducationAnalyzer` | Calculates and ranks composite health and education scores. |
| `ForestLossAnalyzer` | Compares forest-area share across the endpoint years. |
| `DevelopmentIndicators` | Application entry point, data loading, and execution of all analyzers. |

Each analyzer implements the same interface and is invoked through a `List[Analyzer]`, demonstrating abstraction and polymorphism. Filtering, grouping, mapping, and aggregation use Scala collections. `Using.resource` closes the input file after reading.

## Getting started

### Requirements

Install **Scala 3** using the [official Scala setup guide](https://docs.scala-lang.org/getting-started/install-scala.html). The commands below use the Scala CLI-based runner.

The source uses Scala 3 syntax and the standard library. There is no `build.sbt`, pinned Scala version, or third-party dependency declaration in the repository.

### Clone

```sh
git clone https://github.com/beksus/OOP-assignemnt2-2025-6th-sem.git
cd OOP-assignemnt2-2025-6th-sem
```

### Set the dataset path

The CSV is stored in the repository root, but `main.scala` currently looks for it under `src/main/resources/`.

For the existing repository layout, change the filename in `DevelopmentIndicators.main` to:

```scala
val filename = "Global_Development_Indicators_2000_2020.csv"
```

Alternatively, create `src/main/resources/` and copy the CSV there, leaving the source unchanged.

### Run

From the repository root:

```sh
scala run main.scala
```

If your installation exposes the runner as `scala-cli`, use:

```sh
scala-cli run main.scala
```

The program prints the three questions and calculated answers to the terminal. It does not accept a dataset path through command-line arguments or write results to `answer.txt`.

## Dataset

The included CSV contains **5,556 records** spanning **2000–2020**, with **265 distinct country or group names**. It includes regional and economic aggregates as well as individual countries.

The application reads these eight columns:

```text
year
country_name
life_expectancy
child_mortality
school_enrollment_secondary
healthcare_capacity_index
health_development_ratio
forest_area_pct
```

Other columns are ignored. Numeric values that cannot be parsed become `None`; rows that fail during record construction are silently skipped.

## Repository structure

| File | Purpose |
| --- | --- |
| [main.scala](main.scala) | Data model, analyzer trait, three analyses, and entry point. |
| [Global_Development_Indicators_2000_2020.csv](Global_Development_Indicators_2000_2020.csv) | Input dataset. |
| [answer.txt](answer.txt) | Previously saved answers and a dataset-path note. |
| [.gitattributes](.gitattributes) | Git text-file handling settings. |

## Limitations and reproducibility

- **CSV parsing:** the loader uses `split(",")`, which does not handle quoted commas. The dataset contains names such as `"Hong Kong SAR, China"`; these rows can have shifted columns, incorrect values, or be skipped. Use a CSV-aware parser before treating the rankings as reliable.
- **Aggregate entries:** the analyzers do not exclude regional or economic groupings from country comparisons.
- **Missing data:** the composite analysis uses only complete records for its five indicators. Countries may therefore be compared over different sets of years.
- **Empty inputs:** calls to `min`, `max`, and `maxBy` assume eligible records exist and can fail otherwise.
- **Saved answers:** `answer.txt` is not a verified baseline for the current source. It reports life expectancy of 100.0, which the current first analyzer excludes, and a composite score of 649.34, which does not match the current normalized scoring scale.
- **Validation:** no automated tests or build configuration are included. These instructions were checked against the source; the Scala application was not executed during this documentation review.

## License and data attribution

No license file or dataset-source citation is included in the repository.

