package com.beta.Utils;

import com.beta.aqmp.TaskSender;
import com.beta.pojo.User;
import com.beta.redis.RedisPoolFactory;
import com.beta.redis.UserKey;
import com.beta.repositry.UserRepositry;
import com.beta.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;
import rx.internal.schedulers.CachedThreadScheduler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class redisWriteUtils {
    private static final int taskCount = 50;

    private static final int batchSize = 10;

    private static final int cmdCount = 1000;

    private static final boolean usePipeline = true;

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://192.168.197.131/picInfo";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "password";
    static int count =0;



    public static void main(String[] args)
    {



        List<User> users =new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;
        try{
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT userid,balance FROM picInfo.User";
            ResultSet rs = stmt.executeQuery(sql);

            //STEP 5: Extract data from result set
            while(rs.next()){
                //Retrieve by column name
                User user =new User();
                user.setBalance(rs.getDouble("balance"));
                user.setUserid(rs.getString("userid"));
                users.add(user);

                //Display values
            }
            //STEP 6: Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }finally{
            //finally block used to close resources
            try{
                if(stmt!=null)
                    stmt.close();
            }catch(SQLException se2){
            }// nothing we can do
            try{
                if(conn!=null)
                    conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }//end finally try
        }//end try

        System.out.println(users.size());



        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(500);
        config.setMaxTotal(1000);
        config.setMaxWaitMillis(500);

        JedisPool jp = new JedisPool(config, "192.168.197.131", 6379,
                100000, null, 0);




//        CountDownLatch countDownLatch = new CountDownLatch(1000);

//        ExecutorService executorService = new Executors.newCachedThreadPool();

        ExecutorService executor = Executors.newFixedThreadPool(100);




        for(int i=0;i<50;i++)
        {
            TaskDemon taskDemon =new TaskDemon();

            for(int j=0;j<1000;j++)
            {
                taskDemon.users.add(users.get(count));
                count++;
            }

            taskDemon.init(jp);
            log.info("Start Thread");
            executor.execute(taskDemon);
        }


    }
}

@Slf4j
class TaskDemon implements Runnable{

    private JedisPool jedisPool;
    Jedis jedis;
    public List<User> users =new ArrayList<>(100);

    public void init(JedisPool jedisPool){

        this.jedisPool=jedisPool;

    }

    @Override
    public void run() {
       try {
           jedis= jedisPool.getResource();
           Pipeline pipeline = jedis.pipelined();
           for(int i=0;i<1000;i++)
           {
//            pipeline.sadd(UserKey.getById,)
               pipeline.set(""+UserKey.getById.getPrefix()+users.get(i).getUserid(),String.valueOf(users.get(i).getBalance()));
               log.info("redis add!");
           }
           pipeline.exec();
       }catch (Exception e)
       {
           log.info(e.getLocalizedMessage());
           throw e;
       }
       finally {
           jedis.close();
       }


    }
}
