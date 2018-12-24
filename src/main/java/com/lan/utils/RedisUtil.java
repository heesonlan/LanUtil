package com.lan.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
	private static String redisUrl = "127.0.0.1";
	private static int redisPort = 6379;
	private static String redisPassword = null;
	private static int database = 1;// 可选0-15

	private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

	private static volatile JedisPool jedisPool = null;

	private RedisUtil() {
	}
	/**
	 * 服务器整个应用关闭后（不是单个方法结束后），可考虑调用此方法销毁连接池
	 * @author LAN
	 * @date 2018年11月14日
	 */
	public static void destroy() {
		if(jedisPool==null) return;
		if(!jedisPool.isClosed()) jedisPool.close();
		jedisPool.destroy();
	}

	private static Jedis getConnection() {
		if (jedisPool == null) {
			synchronized (RedisUtil.class) {// 线程安全
				if (jedisPool == null) {
					logger.debug("=================创建jedisPool Start=================");
					JedisPoolConfig config = new JedisPoolConfig();
					config.setMaxTotal(200);//最大连接数, 默认8个
					config.setMaxIdle(8);//最大空闲连接数, 默认8个
					config.setMaxWaitMillis(1000 * 100);//获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
					config.setTestOnBorrow(true);
					
					jedisPool = new JedisPool(config, redisUrl, redisPort, 100000, redisPassword, database);
					logger.debug("=================创建jedisPool End=================");
				}
			}
		}
		return jedisPool.getResource();
	}

	/**
	 * 
	 * @author LAN
	 * @date 2018年11月14日
	 * @param key     存储的键
	 * @param o       存储的java对象
	 * @param expire  设置过期时间,单位：秒，小于0时为永不过期
	 */
	public static void set(String key, Object o, int expire) {
		Jedis jedis = null;
		try {
			jedis = getConnection();
			if(o==null) {
				jedis.del(key.getBytes());
				return;
			}
			byte[] data = KryoSerializeUtil.serialize(o);
			if(expire>0) {
				jedis.setex(key.getBytes(), expire, data);
			}else {
				jedis.set(key.getBytes(), data);
			}
		}finally {
			if(jedis!=null) jedis.close();//新版本的close方法，如果是从JedisPool中取出的，则会放回到连接池中，并不会销毁。
		}
	}
	
	public static void set(String key, Object o) {
		set(key, o, -1);
	}
	
	public static <T> T get(String key, Class<T> clazz) {
		Jedis jedis = null;
		try {
			jedis = getConnection();
			byte[] data = jedis.get(key.getBytes());
			if(data==null || data.length==0){
				return null;
			}
			T t = (T) KryoSerializeUtil.unserialize(data, clazz);
			return t;
		}finally {
			if(jedis!=null) jedis.close();//新版本的close方法，如果是从JedisPool中取出的，则会放回到连接池中，并不会销毁。
		}
	}

	/**
	 * 存：setHashObject("UserTimesHash", "1001", new Integer(10));
	 * 取：getHashObject("UserTimesHash", "1001", Integer.class);
	 * 批量取全部：getHashList("UserTimesHash");
	 * 
	 * 用hash存入值，方便批量查询
	 * expire=-1表示永不失效
	 * @author LAN
	 * @date 2018年9月17日
	 * @param key
	 * @param field
	 * @param o
	 * @param expire
	 */
	public static void setHashObject(String key, String field, Object o, int expire) {
		Jedis jedis = null;
		try {
			jedis = getConnection();
			if(o==null) {
				jedis.hdel(key.getBytes(), field.getBytes());
				return;
			}
			byte[] data = KryoSerializeUtil.serialize(o);
			jedis.hset(key.getBytes(), field.getBytes(), data);
			if(expire!=-1){
				jedis.expire(key.getBytes(), expire);
			}
		}finally {
			if(jedis!=null) jedis.close();
		}
	}
	
	private static <T> T getHashObject(byte[] key, byte[] field, Class<T> clazz){
		Jedis jedis = null;
		try {
			jedis = getConnection();
			byte[] data = jedis.hget(key, field);
			if(data==null || data.length==0){
				return null;
			}
			T t = KryoSerializeUtil.unserialize(data, clazz);
			return t;
		} finally {
			if(jedis!=null) jedis.close();
		}
	}
	
	/**
	 * 存：setHashObject("UserTimesHash", "1001", new Integer(10));
	 * 取：getHashObject("UserTimesHash", "1001", Integer.class);
	 * 批量取全部：getHashList("UserTimesHash");
	 * @author LAN
	 * @date 2018年9月17日
	 * @param key
	 * @param field
	 * @param clazz
	 * @return
	 */
	public static <T> T getHashObject(String key, String field, Class<T> clazz) {
		return getHashObject(key.getBytes(), field.getBytes(), clazz);
	}
	
	/**
	 * 存：setHashObject("UserTimesHash", "1001", new Integer(10));
	 * 取：getHashObject("UserTimesHash", "1001", Integer.class);
	 * 批量取全部：getHashList("UserTimesHash");
	 * @author LAN
	 * @date 2018年9月17日
	 * @param key
	 * @param clazz
	 * @return
	 */
	public static <T> List<RedisHashObject> getHashList(String key, Class<T> clazz) {
		Jedis jedis = null;
		try {
			jedis = getConnection();
			Set<byte[]> hkeys = jedis.hkeys(key.getBytes());
			if(hkeys==null || hkeys.size()==0){
				return null;
			}
			List<RedisHashObject> list = new ArrayList<>(); 
			for(byte[] field:hkeys){
				T obj = getHashObject(key.getBytes(), field, clazz);
				list.add(new RedisHashObject(new String(field), obj));
			}
			return list;
		} finally {
			if(jedis!=null) jedis.close();
		}
	}
	
	/**
	 * 获取hashIncrease方法某个key下设置的所有值
	 * @author LAN
	 * @date 2018年11月13日
	 * @param key
	 * @return
	 */
	public static <T> List<RedisHashObject> getHashLongList(String key) {
		Jedis jedis = null;
		try {
			jedis = getConnection();
			Set<byte[]> hkeys = jedis.hkeys(key.getBytes());
			if(hkeys==null || hkeys.size()==0){
				return null;
			}
			List<RedisHashObject> list = new ArrayList<>(); 
			for(byte[] field:hkeys){
				Long value = jedis.hincrBy(key, new String(field), 0l);
				list.add(new RedisHashObject(new String(field), value));
			}
			return list;
		} finally {
			if(jedis!=null) jedis.close();
		}
	}
	
	/**
	 * 为Redis的Hash某个key中的某个域field递增某个值
	 * @author LAN
	 * @date 2018年11月13日
	 * @param key
	 * @param field
	 * @param increase
	 * @param expire
	 */
	public static Long hashIncrease(String key, String field, Long increase, int expire) {
		Jedis jedis = null;
		try {
			jedis = getConnection();
			Long n = jedis.hincrBy(key, field, increase);
			if(expire!=-1){
				jedis.expire(key, expire);
			}
			return n;
		} finally {
			if(jedis!=null) jedis.close();
		}
	}
}
