package utils;

public class GmallConfig {
    //MYSQL 库名
    public static final String HBASE_SCHEMA = "hbasedim";
    //Phoenix 驱动
    public static final String PHOENIX_DRIVER = "com.mysql.jdbc.Driver";
    //Phoenix 连接参数
    public static final String PHOENIX_SERVER =
            "jdbc:mysql://VM-4-7-centos:3306/hbasedim";
}