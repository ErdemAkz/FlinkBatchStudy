package com.flinklearn.batch.chapter5;

import com.flinklearn.batch.chapter3.FilterOrdersByDate;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.aggregation.Aggregations;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.api.java.tuple.Tuple8;
import scala.Int;

import java.lang.reflect.Type;

import static org.apache.flink.api.java.aggregation.Aggregations.SUM;

public class myAnswers {

    public static void main(String[] args){

        try {


            final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

            /****************************************************************************
             *                  Read CSV file into a DataSet
             ****************************************************************************/

            System.out.println("-----------------------Raw Orders-----------------------------------------------");

            DataSet<Tuple4<String, String, Double, Double>> rawOrders
                    = env.readCsvFile("src/main/resources/student_scores.csv")
                    .ignoreFirstLine()
                    .types(String.class, String.class, Double.class, Double.class);
            rawOrders.first(5).print();


            System.out.println("-----------------------Compute Total Score--------------------------------------");
            DataSet<Tuple4<String, String, Integer, Double>> computedScores = rawOrders
                    .map(new MapFunction<Tuple4<String,String,Double,Double>,
                            Tuple4<String, String, Integer, Double>>()
                    {
                        public Tuple4<String, String, Integer, Double>
                        map(Tuple4<String, String, Double, Double> summary) {
                            return new Tuple4(
                                    summary.f0, summary.f1, 1,
                                    summary.f2 + summary.f3);
                        }
                    });
            computedScores.first(5).print();


            System.out.println("-----------------------Physics Scores--------------------------------------------");
            //DataSet<Tuple2<String, Double>> filteredOrders = computedScores.filter( new FilterOrdersByPhysics() );


            DataSet<Tuple2<String, Double>> physicsScores = computedScores
                    .filter( satir -> satir.f1.equals("Physics")? true : false)
                    .project(0,3);
            physicsScores.first(5).print();

            System.out.println("-----------------------Avarage Total Score For Each Person-----------------------");

            computedScores
                    .<Tuple3<String,Integer,Double>>project(0,2,3)
                    .groupBy(0)
                    .reduce(new ReduceFunction<Tuple3<String, Integer, Double>>() {
                @Override
                public Tuple3<String, Integer, Double> reduce(Tuple3<String, Integer, Double> t1,
                                                              Tuple3<String, Integer, Double> t2)
                        throws Exception {

                        return new Tuple3(t1.f0, (t1.f1 + t2.f1) , t1.f2 + t2.f2);

                }
            }).map(i -> Tuple2.of(i.f0, i.f2/i.f1))
              .returns(Types.TUPLE(Types.STRING,Types.DOUBLE))
              .print();

            System.out.println("-----------------------Highest Score Per Subject---------------------------------");

            DataSet<Tuple3<String, String, Double>> topStudentBySubject = computedScores
                    .<Tuple3<String,String,Double>>project(0,1,3)
                    .groupBy(1)
                    .reduce(new ReduceFunction<Tuple3<String, String, Double>>() {
                @Override
                public Tuple3<String, String, Double> reduce(Tuple3<String, String, Double> t1,
                                                              Tuple3<String, String, Double> t2)
                        throws Exception {

                    if (t1.f2 > t2.f2)
                        return t1;
                    else
                        return t2;

                }
            });
            topStudentBySubject.project(1,0,2).print();






        }catch (Exception e){
            e.printStackTrace();
        }




    }
}
