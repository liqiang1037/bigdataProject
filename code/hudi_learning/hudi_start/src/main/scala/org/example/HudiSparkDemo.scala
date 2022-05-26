package org.example

import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

object HudiSparkDemo {

  /**
   * 官方案例：模拟产生数据，插入Hudi表，表的类型COW
   */
  def insertData(spark: SparkSession, table: String, path: String): Unit = {
    import spark.implicits._

    // 第1步、模拟乘车数据
    import org.apache.hudi.QuickstartUtils._

    val dataGen: DataGenerator = new DataGenerator()
    val inserts = convertToStringList(dataGen.generateInserts(1000000))

    import scala.collection.JavaConverters._
    val insertDF: DataFrame = spark.read.json(
      spark.sparkContext.parallelize(inserts.asScala, 10).toDS()
    )
    	//	insertDF.printSchema()
    	//	insertDF.show(10, truncate = false)

    // TOOD: 第2步、插入数据到Hudi表
    import org.apache.hudi.DataSourceWriteOptions._
    import org.apache.hudi.config.HoodieWriteConfig._
    insertDF.write
      .mode(SaveMode.Append)
      .format("hudi")
      .option("hoodie.insert.shuffle.parallelism", "10")
      .option("hoodie.upsert.shuffle.parallelism", "10")
      // Hudi 表的属性值设置
      .option(PRECOMBINE_FIELD.key(), "ts")
      .option(RECORDKEY_FIELD.key(), "uuid")
      .option(PARTITIONPATH_FIELD.key(), "partitionpath")
      .option(TBL_NAME.key(), table)
      .save(path)


  }

  def queryDataByTime(spark: SparkSession, path: String): Unit = {
    import org.apache.spark.sql.functions._

    // 方式一：指定字符串，按照日期时间过滤获取数据
    val df1 = spark.read
      .format("hudi")
      .option("as.of.instant", "20220525001940")
      .load(path)
    df1.printSchema()
    df1.show(1000,false)

    println("==================== 分割线 ====================")


    // 方式二：指定字符串，按照日期时间过滤获取数据
    val df2 = spark.read
      .format("hudi")
      .option("as.of.instant", "2022-05-25 00:19:40")
      .load(path)
      .sort(col("_hoodie_commit_time").desc)
    df2.printSchema()
    df2.show(numRows = 5, truncate = false)





  }

  /**
   * 采用Snapshot Query快照方式查询表的数据
   */
  def queryData(spark: SparkSession, path: String): Unit = {
    import spark.implicits._
    val tripsDF: DataFrame = spark.read.format("hudi").load(path)
    tripsDF.createOrReplaceTempView("hudi02");

    spark.sql("select * " +
      " from hudi02  where  _hoodie_commit_time='20220525001940'").show(1000)  }
  def main(args: Array[String]): Unit = {
    // 创建SparkSession实例对象，设置属性
    val spark: SparkSession = {
      SparkSession.builder()
        .appName(this.getClass.getSimpleName.stripSuffix("$"))
        // 设置序列化方式：Kryo
        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .getOrCreate()
    }

    // 定义变量：表名称、保存路径
    val tableName: String = "tbl_trips_cow11"
    val tablePath: String = "/hudi-warehouse/tbl_trips_cow11"
    import org.apache.hudi.QuickstartUtils._
    for(i <- 1 to 10){

     // insertData(spark, tableName, tablePath)

    }


    // 任务二：快照方式查询（Snapshot Query）数据，采用DSL方式
    queryData(spark, tablePath)
    // queryDataByTime(spark, tablePath)

    spark.stop()
  }

}
