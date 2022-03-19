package utils;

public class GmallConfig {
    //MYSQL 库名
    public static final String HBASE_SCHEMA = "gmall2021";
    //Phoenix 驱动
    public static final String PHOENIX_DRIVER = "com.mysql.jdbc.Driver";
    //Phoenix 连接参数
    public static final String PHOENIX_SERVER =
            "jdbc:mysql://localhost:3306/gmall2021?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
}