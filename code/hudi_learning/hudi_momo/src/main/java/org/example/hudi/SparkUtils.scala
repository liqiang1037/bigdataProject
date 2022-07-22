package org.example.hudi

import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.sql.SparkSession

import java.util.{Calendar, Date}

object SparkUtils {
  def createSparkSession(clazz: Class[_], master: String = "local", partitions: Int = 4
                        ): SparkSession = {
    SparkSession.builder()
      .appName(clazz.getSimpleName.stripSuffix("$"))
      .master(master)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.sql.shuffle.partitions", partitions)
      .getOrCreate()
  }

  def main(args: Array[String]): Unit = {
    val dateStr: String = "2022-05-26"
    val format: FastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd")
    val calendar: Calendar = Calendar.getInstance()
    val date: Date = format.parse(dateStr)
    calendar.setTime(date)
    val dayweek: String = calendar.get(Calendar.DAY_OF_WEEK) match {
      case 1 => "星期日"
      case 2 => "星期一"
      case 3 => "星期二"
      case 4 => "星期三"
      case 5 => "星期四"
      case 6 => "星期五"
      case 7 => "星期六"
    }
    println(dayweek)

  }

}
