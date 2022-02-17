package com.xinzhi.flink.apiTest;

import com.xinzhi.flink.utils.SensorReading;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.HashMap;
import java.util.Random;

public class SensorSource implements SourceFunction<SensorReading> {
    boolean flag=true;
    @Override
    public void run(SourceContext<SensorReading> ctx) throws Exception {
        Random random=new Random();
      HashMap<String,Double> sensorMap=new HashMap();
      for(int i=0;i<=10;i++){
          sensorMap.put("sensor_"+i,60+random.nextGaussian()*20);
        }
      while (flag){
          for (String key : sensorMap.keySet()) {
              Double temperature = sensorMap.get(key);
              Double newTemperature=temperature+random.nextDouble();
              sensorMap.put(key,newTemperature);
              ctx.collect(new SensorReading(key,newTemperature,System.currentTimeMillis()));
          }
          Thread.sleep(2000);
      }


    }

    @Override
    public void cancel() {
        flag=false;
    }
}
