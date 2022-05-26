package org.example.didi

import org.apache.spark.sql.functions.{col, concat_ws, unix_timestamp}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.example.utils.SparkUtils

object DidiStorageSpark {



  def main(args: Array[String]): Unit = {
    // step1.构建SparkSession实例对象（集成Hudi和HDFS）
    val spark: SparkSession = SparkUtils.createSparkSession(this.getClass)
    // step2.加载本地CSV文件格式滴滴出行数据
    val datasPath = "/datas/dwv_order_make_haikou_1.txt"
    val didiDF: DataFrame = readCsvFile(spark, datasPath)

    //step3. 滴滴出行数据ETL处理并保存至Hudi表
    val etLDF: DataFrame = process(didiDF)

    val hudiTableName="tbl_didi_haikou"
    val hudiTablePath="/hudi-warehouse/tbl_didi_haikou"
    //stpe4.保存转换后数据至Hudi表
      saveToHudi(etLDF,hudiTableName,hudiTablePath)

    // stpe5.应用结束，关闭资源
    spark.stop()

  }

  /**
   * 读取CSV格式文本文件数据，封装到DataFrame数据集
   * */
  def readCsvFile(spark: SparkSession, datasPath: String): DataFrame = {
    spark.read
      // 设置分隔符为逗号
      .option("sep", "\\t")
      //文件首行为列名称
      .option("header", "true")
      // 依据数值自动推断数据类型
      .option("inferSchema", "true").csv(datasPath)

  }

  /**
   *
   * 对滴滴出行海口数据进行ETL转换操作∶
   * 指定ts和partitionpath列
   *
   * */

  def process(didiDF: DataFrame): DataFrame = {
    didiDF
      .withColumn(
        "partitionpath",
        concat_ws("/",
          col("year")
          , col("month")
          , col("day"))
      ).drop("year", "month", "day")
      .withColumn(
        "ts",
        unix_timestamp(col("departure_time"), "yyyy-MM-dd HH:mm:ss")
      )
  }

  /**
   * 将ETL转换后数据，保存到Hudi表中，采用COW模式
   * @param etlDF
   * @param hudiTableName
   * @param hudiTablePath
   */
  def saveToHudi(etlDF: DataFrame, hudiTableName: String, hudiTablePath: String)= {
    import org.apache.hudi.DataSourceWriteOptions._
    import org.apache.hudi.config.HoodieWriteConfig._

    etlDF.write
      .mode(SaveMode.Overwrite)
      .format("hudi")
      .option("hoodie.insert.shuffle.parallelism","2")
      .option("hoodie.upsert.shuffle.parallelism","2")
      .option(RECORDKEY_FIELD.key(),"order_id")
      .option(PRECOMBINE_FIELD.key(),"ts")
      .option(PARTITIONPATH_FIELD.key(),"partitionpath")
      .option(TBL_NAME.key(),hudiTableName)
      . save(hudiTablePath)




  }



}
