package com.shuguang.jedisTest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

public class RedisLock {
    public String getlock(String key,int timeout){
        try{
            Jedis jedis = RedisManager.getJedis();
            String value = UUID.randomUUID().toString();
            long end = System.currentTimeMillis()+ timeout;
            while (System.currentTimeMillis() < end){
                if(jedis.setnx(key,value)==1){//该函数设置成功后会返回1
                    if(jedis.ttl(key)==-1){//检测过期时间，-1:表示没设置过
                        jedis.expire(key,timeout);
                    }
                    return value;
                }

                Thread.sleep(1000);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public boolean releaseLock(String key,String value){
        try {
            Jedis jedis = RedisManager.getJedis();
            while (true){
                jedis.watch(key);//watch是监听这个key，在开启与这个key相关的事物开始时，如果这个key被修改或者删除，那么这个相关的事物就不会开启，里面的代码也不会执行
                if(value.equals(jedis.get(key))){//判断获得锁的线程和当前redis中存的锁是否为同一个
                    Transaction transaction = jedis.multi();
                    transaction.del(key);
                    List<Object> list=transaction.exec();
                    if(list == null){
                        continue;
                    }
                    return true;
                }
                jedis.unwatch();
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void main(String[] args){
        RedisLock redisLock = new RedisLock();
        String lockId=redisLock.getlock("lock:ab",10000);
        if(null != lockId){
            System.out.println("获取锁成功lockId:"+lockId);
        }
        System.out.println("================:");


        String lockId2=redisLock.getlock("lock:aa",10000);
        System.out.println("获取锁lockId2:"+lockId2);
    }
}
