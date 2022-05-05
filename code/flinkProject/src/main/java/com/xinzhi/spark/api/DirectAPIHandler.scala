package com.xinzhi.spark.api

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.{
  HasOffsetRanges, KafkaUtils,
  OffsetRange
}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object DirectAPIHandler {
  def main(args: Array[String]): Unit = {
    //1.创建 SparkConf
    val sparkConf: SparkConf = new
        SparkConf().setAppName("ReceiverWordCount").setMaster("local[*]")
    //2.创建 StreamingContext
    val ssc = new StreamingContext(sparkConf, Seconds(3))
    //3.Kafka 参数
    val kafkaPara: Map[String, String] = Map[String, String](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG ->
        "localhost:9092,localhost:9093,localhost:9094",
      ConsumerConfig.GROUP_ID_CONFIG -> "atguigu"
    )
    //4.获取上一次启动最后保留的 Offset=>getOffset(MySQL)
    val fromOffsets: Map[TopicAndPartition, Long] = Map[TopicAndPartition,
      Long](TopicAndPartition("testHandler", 0) -> 20)
    //5.读取 Kafka 数据创建 DStream
    val kafkaDStream: InputDStream[String] = KafkaUtils.createDirectStream[String,
      String, StringDecoder, StringDecoder, String](ssc,
      kafkaPara,
      fromOffsets,
      (m: MessageAndMetadata[String, String]) => m.message())
    //6.创建一个数组用于存放当前消费数据的 offset 信息
    var offsetRanges = Array.empty[OffsetRange]
    //7.获取当前消费数据的 offset 信息
    val wordToCountDStream: DStream[(String, Int)] = kafkaDStream.transform { rdd
    =>
      offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      rdd
    }.flatMap(_.split(" "))
      .map((_, 1))
      //.reduceByKey(_ + _)
    //8.打印 Offset 信息
    wordToCountDStream.foreachRDD(rdd => {
      for (o <- offsetRanges) {
       // println("---->"+o.fromOffset)
        println(s"${o.topic}--->${o.partition}--->${o.fromOffset}--->${o.untilOffset}")
      }
      rdd.foreach(println)
    })
    //9.开启任务
    ssc.start()
    ssc.awaitTermination()
  }
}