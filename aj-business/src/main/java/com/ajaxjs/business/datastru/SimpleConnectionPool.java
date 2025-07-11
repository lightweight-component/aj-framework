package com.ajaxjs.business.datastru;


import java.sql.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;

/**
 * 1 连接池的缓存/分配策略。需要考虑最小连接数目，最大连接数目， 管理正在被使用的连接和未使用的连接， 处理因为网络超使等特别原因而无效的连接，
 * 什么时候再次申请连接等等，这些问题实现起来不难，就是繁琐。
 * 2 Connection关闭问题。当程序调用Connection.close()之后，则这个连接对应的tcp/ip 连接就消失了，所以必须替换掉这种行为。这个问题解决起来也不难，
 * jdk 动态代理就可以。pool里面不要放原生的Connection，而放被代理之后的, close方法被替换之后的Connection包装类

 * author:<a href="https://blog.csdn.net/sunxing007/article/details/5833085">...</a>
 **/
public class SimpleConnectionPool {

    //未用的连接
    private static final LinkedList notUsedConnections = new LinkedList();
    //正在被使用的连接
    private static final HashSet usedConnections = new HashSet();
    //driver name 怕ssword
    private static String url = "jdbc:mysql://localhost:3306/test";
    private static String user = "root";
    private static String password = "123456";
    //initial pool size
    private static int POOL_SIZE = 10;
    //上次检查已关闭连接的时间
    static private long lastCheckClosedConnectionTime = System.currentTimeMillis();
    //检查已关闭连接的时间间隔
    public static long CHECK_CLOSED_CONNECTION_TIME = 4 * 60 * 60 * 1000; // 4 hours

    // 测试用例
    public static void main(String[] args) {
        SimpleConnectionPool.init();
        Connection con = SimpleConnectionPool.getConnection();
        Connection con1 = SimpleConnectionPool.getConnection();
        Connection con2 = SimpleConnectionPool.getConnection();
        SimpleConnectionPool.printDebugMsg();
        try {
            con.close();
        } catch (Exception e) {
        }

        try {
            con1.close();
        } catch (Exception e) {
        }
        try {
            con2.close();
        } catch (Exception e) {
        }
        con = SimpleConnectionPool.getConnection();
        con1 = SimpleConnectionPool.getConnection();
        try {
            con1.close();
        } catch (Exception e) {
        }
        con2 = SimpleConnectionPool.getConnection();
        SimpleConnectionPool.printDebugMsg();

    }

    //静态初始化快，初始数据库驱动
    static {
        initDriver();
    }

    //pool初始化，申请 POOL_SIZE个连接
    public static void init() {
        int i = 0;

        while (i < POOL_SIZE) {
            notUsedConnections.add(getNewConnection());
            i++;
        }
    }

    private static void initDriver() {
        Driver driver = null;
        try {
            driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
            installDriver(driver);
        } catch (Exception e) {
        }
    }

    public static void installDriver(Driver driver) {
        try {
            DriverManager.registerDriver(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //分配连接
    public static synchronized Connection getConnection() {
        checkClosedConnection();
        while (notUsedConnections.size() > 0) {
            try {
                ConnectionWrapper wrapper = (ConnectionWrapper) notUsedConnections.removeFirst();
                if (wrapper.connection.isClosed()) {
                    continue;
                }
                usedConnections.add(wrapper);
                System.out.println("Allocate new connection succeed.");
                return wrapper.connection;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int newCount = getIncreasingConnectionCount();
        LinkedList list = new LinkedList();
        ConnectionWrapper wrapper = null;
        for (int i = 0; i < newCount; i++) {
            wrapper = getNewConnection();
            if (wrapper != null) {
                list.add(wrapper);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        wrapper = (ConnectionWrapper) list.removeFirst();
        usedConnections.add(wrapper);

        notUsedConnections.addAll(list);
        list.clear();
        return wrapper.connection;
    }

    //申请新的数据库连接
    private static ConnectionWrapper getNewConnection() {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            ConnectionWrapper wrapper = new ConnectionWrapper(con);
            return wrapper;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //将用完毕的连接还回pool
    static synchronized void pushConnectionBackToPool(ConnectionWrapper con) {
        if (usedConnections.remove(con)) notUsedConnections.addLast(con);
    }

    //pool清理， 关闭所有连接
    public static int close() {
        int count = 0;
        Iterator iterator = notUsedConnections.iterator();

        while (iterator.hasNext()) {
            try {
                ((ConnectionWrapper) iterator.next()).close();
                count++;
            } catch (Exception e) {
            }
        }
        notUsedConnections.clear();

        iterator = usedConnections.iterator();
        while (iterator.hasNext()) {
            try {
                ConnectionWrapper wrapper = (ConnectionWrapper) iterator.next();
                wrapper.close();
                count++;
            } catch (Exception e) {
            }
        }

        usedConnections.clear();

        return count;
    }

    //检查因超时等其他原因导致的被关闭的连接
    private static void checkClosedConnection() {
        long time = System.currentTimeMillis();
        // sometimes user change system time,just return

        if (time < lastCheckClosedConnectionTime) {
            time = lastCheckClosedConnectionTime;
            return;
        }

        // no need check very often
        if (time - lastCheckClosedConnectionTime < CHECK_CLOSED_CONNECTION_TIME)
            return;

        lastCheckClosedConnectionTime = time;

        // begin check
        Iterator iterator = notUsedConnections.iterator();
        while (iterator.hasNext()) {
            ConnectionWrapper wrapper = (ConnectionWrapper) iterator.next();
            try {
                if (wrapper.connection.isClosed()) {
                    iterator.remove();
                }
            } catch (Exception e) {
                iterator.remove();
            }
        }

        // make connection pool size smaller if too big
        int decrease = getDecreasingConnectionCount();

        if (notUsedConnections.size() < decrease)
            return;

        while (decrease-- > 0) {
            ConnectionWrapper wrapper = (ConnectionWrapper) notUsedConnections.removeFirst();
            try {
                wrapper.connection.close();
            } catch (Exception e) {
            }
        }
    }

    //增开连接
    public static int getIncreasingConnectionCount() {
        int current = getConnectionCount();
        int count = current / 4;

        if (count < 1)
            count = 1;

        return count;
    }

    //缩减连接
    public static int getDecreasingConnectionCount() {
        int current = getConnectionCount();
        if (current < 10) {
            return 0;
        }
        return current / 3;
    }

    //调试信息
    public synchronized static void printDebugMsg() {
        printDebugMsg(System.out);
    }

    public synchronized static void printDebugMsg(PrintStream out) {
        StringBuffer msg = new StringBuffer();
        msg.append("debug message in " + SimpleConnectionPool.class.getName());
        msg.append("/r/n");
        msg.append("total count is connection pool: " + getConnectionCount());
        msg.append("/r/n");
        msg.append("not used connection count: " + getNotUsedConnectionCount());
        msg.append("/r/n");
        msg.append("used connection, count: " + getUsedConnectionCount());
        out.println("/nStatictics information/n--------------------------------------");
        out.println(msg);
    }

    public static synchronized int getNotUsedConnectionCount() {
        return notUsedConnections.size();
    }

    public static synchronized int getUsedConnectionCount() {
        return usedConnections.size();
    }

    public static synchronized int getConnectionCount() {
        return notUsedConnections.size() + usedConnections.size();
    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String _url) {
        if (_url == null)
            return;

        url = _url.trim();
    }

    public static String getUser() {
        return user;
    }

    public static void setUser(String _user) {
        if (_user == null)
            return;

        user = _user.trim();
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String _password) {
        if (_password == null)
            return;

        password = _password.trim();
    }

}

//连接包装类，通过jdk动态代理，替换close方法。
class ConnectionWrapper implements InvocationHandler {
    private final static String CLOSE_METHOD_NAME = "close";
    public Connection connection;
    private Connection originConnection;
    public long lastAccessTime = System.currentTimeMillis();

    ConnectionWrapper(Connection con) {
        Class<?>[] interfaces = {java.sql.Connection.class};
        this.connection = (Connection) Proxy.newProxyInstance(con.getClass().getClassLoader(), interfaces, this);
        originConnection = con;
    }

    void close() throws SQLException {
        originConnection.close();
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        Object obj = null;
        if (CLOSE_METHOD_NAME.equals(m.getName()))
            SimpleConnectionPool.pushConnectionBackToPool(this);
        else
            obj = m.invoke(originConnection, args);

        lastAccessTime = System.currentTimeMillis();
        return obj;
    }
}

