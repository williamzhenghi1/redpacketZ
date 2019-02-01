package com.beta.redis;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.beta.pojo.BigredPacket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * redis服务
 */
@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;

    /**
     * 从redis连接池获取redis实例
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //对key增加前缀，即可用于分类，也避免key重复
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
        } finally {
            returnToPool(jedis);
        }
    }

    public List getArray(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //对key增加前缀，即可用于分类，也避免key重复
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);
            List t = stringToBeanArray(str);
            return t;
        } finally {
            returnToPool(jedis);
        }
    }


    public <T> T tget(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //对key增加前缀，即可用于分类，也避免key重复
            String realKey = prefix.getPrefix() + key;
            String str = jedis.lpop(realKey);
            System.out.println("__________________________");
            T t = stringToBean(str, clazz);
            return t;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 事务写入小红包队列
     */

    public <T> Boolean tset(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
//            System.out.println("............"+jedis.ge()+"................");
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() <= 0) {
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();//获取过期时间
            if (seconds <= 0) {

                jedis.watch(realKey);
                Transaction transaction =jedis.multi();
                transaction.lpush(realKey,str);
                List<Object> result=transaction.exec();
                jedis.unwatch();

                if(result.isEmpty())
                {
                    return false;
                }else
                    return true;

            }

            return true;
        } finally {
            returnToPool(jedis);
        }

    }


    public <T> Boolean blanceset(KeyPrefix prefix, String key, double blance) {
        Jedis jedis = null;
        try {
//            System.out.println("............"+jedis.ge()+"................");
            jedis = jedisPool.getResource();

            if (blance== 0) {
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();//获取过期时间
            if (seconds <= 0) {

                jedis.watch(realKey);
                Transaction transaction =jedis.multi();
                transaction.incrByFloat(realKey,blance);
                List<Object> result=transaction.exec();
                jedis.unwatch();
                if(result.isEmpty())
                {
                    return false;
                }else
                    return true;
            }

            return true;
        } finally {
            returnToPool(jedis);
        }

    }


    /**
     * 存储对象
     */
    public <T> Boolean set(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
//            System.out.println("............"+jedis.ge()+"................");
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() <= 0) {
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();//获取过期时间
            if (seconds <= 0) {
                jedis.set(realKey, str);
            } else {
                jedis.setex(realKey, seconds, str);
            }

            return true;
        } finally {
            returnToPool(jedis);
        }

    }

    /**
     * 删除
     */
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            long ret = jedis.del(realKey);
            return ret > 0;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 判断key是否存在
     */
    public <T> boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            return jedis.exists(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 增加值
     * Redis Incr 命令将 key 中储存的数字值增一。    如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            return jedis.incr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 减少值
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    public Boolean decrbyFloatTrans(KeyPrefix prefix, String key,Double balance) {
        Jedis jedis = null;
        try {
            if(balance==0)
                return false;
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            jedis.watch(realKey);
            Transaction transaction = jedis.multi();
            transaction.incrByFloat(realKey,-balance);
            List<Object> objects = transaction.exec();
            jedis.unwatch();
            if(objects.isEmpty())
                return false;
            else
                return true;
        } finally {
            returnToPool(jedis);
        }
    }


    public <T> Double valueChange(KeyPrefix prefix, String key,double a) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            return jedis.incrByFloat(realKey,a);
        } finally {
            returnToPool(jedis);
        }
    }


    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return String.valueOf(value);
        } else if (clazz == double.class || clazz == Double.class) {
            return String.valueOf(value);
        } else if (clazz == String.class) {
            return (String) value;
        } else {
            return JSON.toJSONString(value);
        }

    }
//JSON.parseObject(str,new TypeReference<List<BigredPacket>>(){});
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == double.class || clazz == Double.class) {
            return (T) Double.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else {
                return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    public List stringToBeanArray(String str) {
            return JSON.parseObject(str,new TypeReference<List<BigredPacket>>(){});
    }

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();//不是关闭，只是返回连接池
        }
    }

}
