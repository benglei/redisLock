package com.shuguang.lua;

import com.shuguang.jedisTest.RedisManager;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class LuaDemo {

    public static void main() throws Exception{
        Jedis jedis = RedisManager.getJedis();
        /*流量限制，对某个ip的频率进行限制 1分钟访问10次
            由于该例子只是一个demo，所以lua的代码，就放在此处了，正规的是放在redis服务器上面的
            通过jedis.scriptLoad()去加载到redis中，将通过jedis.evalsha()它去执行
         */
        String lua = "local num = redis.call('incr',KEYS[1]);\n" +
                "if tonumber(num) == 1 then\n" +
                "     redis.call('expire',KEYS[1],ARGV[1]);\n" +
                "     return 1;\n" +
                "elseif tonumber(num) > tonumber(ARGV[2]) then\n" +
                "     return 0;\n" +
                "else\n" +
                "     return 1;\n" +
                "end";

        List<String> keys = new ArrayList<String>();
        keys.add("ip:limit:127.0.0.1");
        List<String> args = new ArrayList<String>();
        args.add("6000");
        args.add("10");
        Object obj = jedis.evalsha(jedis.scriptLoad(lua),keys,args);
        System.out.println(obj);
    }

}
