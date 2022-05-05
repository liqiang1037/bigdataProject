package com.xinzhi.spark.api

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object ReceiverAPI {
  def main(args: Array[String]): Unit = {
    //1.创建 SparkConf
    val sparkConf: SparkConf = new
        SparkConf().setAppName("ReceiverWordCount").setMaster("local[*]")
    //2.创建 StreamingContext
    val ssc = new StreamingContext(sparkConf, Seconds(3))
    //3.读取 Kafka 数据创建 DStream(基于 Receive 方式)
    val kafkaDStream: ReceiverInputDStream[(String, String)] =
      KafkaUtils.createStream(ssc,
        "localhost:2181,localhost:2182,localhost:2183",
        "atguigu",
        Map[String, Int]("test" -> 1))
    //4.计算 WordCount
    kafkaDStream.map { case (_, value) =>
      (value, 1)
    }.reduceByKey(_ + _)
      .print()
    //5.开启任务
    ssc.start()
    ssc.awaitTermination()
  }

}
