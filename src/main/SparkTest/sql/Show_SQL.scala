package sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object Show_SQL {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "root")
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkHSQL")


    //创建 SparkSession 对象 添加hive支持
    val spark: SparkSession = SparkSession.builder().enableHiveSupport().config(conf)
      //.config("spark.sql.warehouse.dir", "hdfs://linux101:9870/user/hive/warehouse")
      .getOrCreate()
    //使用SparkSQL连接外置hive
    /**
     * 1、查看当前所有的数据库，引用spark数据库
     */
    // spark.sql("show databases").show()
    spark.sql("use spark")

    // 近七天点击量前十的商品
    spark.sql(
      """
        |create table if not exists table_01  as
        |select *
        |from (
        |         select goods_name, round(goods_views_num * rand() * 1000, 0) as goods_value1
        |         from ods_sale_data_in_last_7days
        |     ) as a
        | order  by  a.goods_value1 desc
        |  limit 10
        |""".stripMargin)


    // 近七天下单量前十的商品
    spark.sql(
      """
        | create table if not exists table_02 as
        | select *
        | from (
        | select  goods_name,round(buy_nums*rand()*1000,0) as goods_value2
        | from   ods_sale_data_in_last_7days
        | ) as b
        | order by  b.goods_value2 desc
        | limit 10
        |""".stripMargin)

    // 近七天站内流量比前十的商品

    spark.sql(
      """
        |create table if not exists table_03 as
        | select *
        | from (
        |  select  goods_name,round(In_station_flow_rate*100,2) as goods_value3
        |  from   ods_sale_data_in_last_7days
        |  ) as c
        |  order by  c.goods_value3 desc
        |  limit 10
        |""".stripMargin)


    // 近七天站外流量比前十的商品
    spark.sql(
      """
        |create table if not exists table_04  as
        |select *
        |from (
        | select  goods_name,round(out_station_flow_rate*100,2) as goods_value4
        |  from   ods_sale_data_in_last_7days
        |  ) as d
        |  order by  d.goods_value4 desc
        |  limit 10
        |""".stripMargin)

    // 近七天好评前十的商品
    spark.sql(
      """
        |create table if not exists table_05  as
        |select *
        |from (
        | select  goods_name ,round(good_evaluate_rate*100,2) as goods_value5
        |  from   ods_sale_data_in_last_7days
        |  ) as e
        |  order by  e.goods_value5 desc
        |  limit 10
        |""".stripMargin)

      // 页面一： 库存：分地区库存量用地图展示
    spark.sql(
      """
        |create table if not exists table_06 as
        |    select
        |      sum(zkkc) as zkc    -- 总在库库存
        |    ,sum(bjzkkc) as bjkc -- 北京库存
        |    ,sum(shzkkc) as shkc -- 上海库存
        |    , sum(gzzkkc) as gzkc -- 广州库存
        |    ,sum(cdzkkc) as cdkc -- 成都库存
        |    , sum(whzkkc) as whkc -- 武汉库存
        |    , sum(syzkkc) as sykc -- 沈阳库存
        |    , sum(xazkkc) as xakc -- 西安库存
        |    ,sum(gazkkc) as gakc -- 固安库存
        |    ,sum(qtjgzkkc) as qtkc -- 其他库存
        |    from  ods_jd_self_list_all_brands_da
        |""".stripMargin)


    spark.close()

  }
}
