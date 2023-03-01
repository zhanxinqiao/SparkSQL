package sql

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

//  hive -metastore &
object SparkSQL_HSQL {
  def main(args: Array[String]): Unit = {
    //创建上下文环境配置对象
    System.setProperty("HADOOP_USER_NAME", "root")
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkHSQL")


    //创建 SparkSession 对象 添加hive支持
    val spark: SparkSession = SparkSession.builder().enableHiveSupport().config(conf)
      //.config("spark.sql.warehouse.dir", "hdfs://linux101:9870/user/hive/warehouse")
      .getOrCreate()
    //使用SparkSQL连接外置hive
    /**
     * 1、创建新的数据库
     * spark
     */
    spark.sql("create  database spark").show()

    /**
     * 2、查看当前所有的数据库，引用spark数据库
     */
    // spark.sql("show databases").show()
    spark.sql("use spark")

    /**
     * 3、创建两张表
     * 表1   最近七天流量
     * ods_sale_data_in_last_7days
     * -- 表2  京东自营-全品牌库存大表（原版）-2015.7.20
     * ods_JD_self_list_all_brands_da
     */
    spark.sql(
      """
        |create table if not exists ods_sale_data_in_last_7days
        |(
        |    goods_id              string comment '商品编号',
        |    goods_name            string comment '商品名称',
        |    goods_views_num       int comment '商品浏览量',
        |    buy_nums              int comment '下单量',
        |    views_people_nums     string comment '访客数',
        |    Conversion_rate       string comment '下单转换率',
        |    add_to_cart_nums      int comment '加入购物车量',
        |    cart_buy_rate         string comment '购物车转换率',
        |    evaluate_nums         int comment '评价数量',
        |    good_evaluate_nums    int comment '好评数量',
        |    good_evaluate_rate    string comment '好评率',
        |    In_station_flow_rate  string comment '站内流量比',
        |    out_station_flow_rate string comment '站外流量比'
        |)
        |row format delimited fields terminated by '\t'
        |""".stripMargin)
    spark.sql(
      """
        |CREATE TABLE  IF NOT exists ods_jd_self_list_all_brands_da (
        |  time_code string  COMMENT '日期',
        |  goods_id string  COMMENT '商品编号',
        |  goods_name string COMMENT '商品名称',
        |  First_in_time string  COMMENT '首次入库时间',
        |  Primary_classification string  COMMENT '一级分类',
        |  Secondary_classification string  COMMENT '二级分类',
        |  Three_classification string  COMMENT '三级分类',
        |  buyer string  COMMENT '采购员',
        |  salesperson string  COMMENT '销售员',
        |  sxg_type string  COMMENT '上下柜状态',
        |  zkkc int COMMENT '在库库存',
        |  bjzkkc int COMMENT '北京在库库存',
        |  shzkkc int COMMENT '上海在库库存',
        |  gzzkkc int COMMENT '广州在库库存',
        |  cdzkkc int COMMENT '成都在库库存',
        |  whzkkc int COMMENT '武汉在库库存',
        |  syzkkc int COMMENT '沈阳在库库存',
        |  xazkkc int COMMENT '西安在库库存',
        |  gazkkc int COMMENT '固安在库库存',
        |  qtjgzkkc int COMMENT '其他机构在库库存',
        |  zt_orders string COMMENT '暂停订单',
        |  jinjia float COMMENT '进价',
        |  shichangjia float COMMENT '市场价',
        |  JD_PRICE float COMMENT '京东价格',
        |  1day_sale_nums int COMMENT '一日销售量',
        |  7days_sale_nums int COMMENT '7日销量',
        |  14days_sale_nums int COMMENT '14天销量',
        |  28days_sale_nums int COMMENT '28天销量',
        |  90days_sale_nums int COMMENT '90天销量',
        |  last_4weeks_rkl int COMMENT '过去四周入库量',
        |  sxg_time string  COMMENT '上下柜时间',
        |  SKU_create_time string  COMMENT '上下柜时间',
        |  brand string  COMMENT '品牌',
        |  gys_code string  COMMENT '供应商简码',
        |  gys_id string  COMMENT '供应商ID',
        |  gys_name string  COMMENT '供应商名称',
        |  order_BAND string  COMMENT '订单BAND',
        |  click_band string  COMMENT '点击band',
        |  click_num_7days int  COMMENT '一周点击量',
        |  is_cjzs string  COMMENT '是否厂家直送',
        |  is_overtime string  COMMENT '是否临期',
        |  is_send_all string  COMMENT '是否全国发货',
        |  is_swjz string  COMMENT '是否售完即止'
        |)  row format delimited fields terminated by '\t'
        |""".stripMargin)


    //数据写入
    spark.sql(
      """
        |load data local inpath './input/1.txt' into table spark.ods_sale_data_in_last_7days
        |""".stripMargin).show()

    spark.sql(
      """
        |load data local inpath './input/kucun.txt' into table spark.ods_jd_self_list_all_brands_da
        |""".stripMargin).show()

    spark.sql("select * from  spark.ods_sale_data_in_last_7days limit 10").show(

    )

    /**
     * 4、查看表创建是否成功
     */
    spark.sql("show tables").show()

    // 近七天点击量前十的商品
    spark.sql(
      """
        | create table table_01  as
        | select  goods_name1 ,round(goods_views_num*rand()*1000,0)  as goods_value1
        | from   ods_sale_data_in_last_7days
        | order by  round(goods_views_num*rand()*1000,0)
        | desc limit 10
        |
        |""".stripMargin)
    // 近七天下单量前十的商品
    spark.sql(
      """
        | create table table_02 as
        | select  goods_name2 ,round(buy_nums*rand()*1000,0) as goods_value2
        | from   ods_sale_data_in_last_7days
        | order by  round(buy_nums*rand()*1000,0) desc limit 10
        |""".stripMargin)

    // 近七天站内流量比前十的商品

    spark.sql(
      """
        | create table table_03 as
        |  select  goods_name3,In_station_flow_rate*100 as goods_value3
        |    from   ods_sale_data_in_last_7days
        |     order by  In_station_flow_rate desc limit 10
        |""".stripMargin)

    // 近七天站外流量比前十的商品
    spark.sql(
      """
        |create table table_04 as
        | select  goods_name4,out_station_flow_rate*100 as goods_value4
        |  from   ods_sale_data_in_last_7days
        |   order by  out_station_flow_rate desc limit 10
        |
        |""".stripMargin)

    // 近七天好评前十的商品
    spark.sql(
      """
        |create table table_05  as
        | select  goods_name5 ,good_evaluate_rate*100 as goods_value5
        |  from   ods_sale_data_in_last_7days
        |  order by  good_evaluate_rate desc limit 10
        |
        |""".stripMargin)


    // 页面一： 库存：分地区库存量用地图展示
    spark.sql(
      """
        |    create table table_06 as
        |    select
        |      sum(zkkc) as zkc   // 总在库库存
        |    ,sum(bjzkkc) as bjkc // 北京库存
        |    ,sum(shzkkc) as shkc // 上海库存
        |    , sum(gzzkkc) as gzkc // 广州库存
        |    ,sum(cdzkkc) as cdkc // 成都库存
        |    , sum(whzkkc) as whkc // 武汉库存
        |    , sum(syzkkc) as sykc // 沈阳库存
        |    , sum(xazkkc) as xakc // 西安库存
        |    ,sum(gazkkc) as gakc // 固安库存
        |    ,sum(qtjgzkkc) as qtkc // 其他库存
        |    from  ods_jd_self_list_all_brands_da
        |""".stripMargin)


    spark.close()
  }

}
