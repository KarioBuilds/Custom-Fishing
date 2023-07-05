/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.util;

import net.momirealms.customfishing.helper.Log;
import org.bukkit.configuration.file.YamlConfiguration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class JedisUtils {

    private static JedisPool jedisPool;

    public static Jedis getJedis(){
        return jedisPool.getResource();
    }

    public static void initializeRedis(YamlConfiguration configuration){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setTestWhileIdle(true);
        jedisPoolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(30000));
        jedisPoolConfig.setNumTestsPerEvictionRun(-1);
        jedisPoolConfig.setMinEvictableIdleTime(Duration.ofMillis(configuration.getInt("Redis.MinEvictableIdleTimeMillis",1800000)));
        jedisPoolConfig.setMaxTotal(configuration.getInt("Redis.MaxTotal",8));
        jedisPoolConfig.setMaxIdle(configuration.getInt("Redis.MaxIdle",8));
        jedisPoolConfig.setMinIdle(configuration.getInt("Redis.MinIdle",1));
        jedisPoolConfig.setMaxWait(Duration.ofMillis(configuration.getInt("redis.MaxWaitMillis")));
        if (configuration.getString("Redis.password") != null) {
            jedisPool = new JedisPool(jedisPoolConfig, configuration.getString("Redis.host","localhost"), configuration.getInt("Redis.port",6379), 2000, configuration.getString("Redis.password"));
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, configuration.getString("Redis.host","localhost"), configuration.getInt("Redis.port",6379));
        }

        AdventureUtils.consoleMessage("[CustomFishing] <white>Redis Server Connected!");

        List<Jedis> minIdleJedisList = new ArrayList<>(jedisPoolConfig.getMinIdle());
        for (int i = 0; i < jedisPoolConfig.getMinIdle(); i++) {
            Jedis jedis;
            try {
                jedis = jedisPool.getResource();
                minIdleJedisList.add(jedis);
                jedis.ping();
            } catch (Exception e) {
                Log.warn(e.getMessage());
            }
        }

        for (int i = 0; i < jedisPoolConfig.getMinIdle(); i++) {
            Jedis jedis;
            try {
                jedis = minIdleJedisList.get(i);
                jedis.close();
            } catch (Exception e) {
                Log.warn(e.getMessage());
            }
        }
    }

    public static void closePool() {
        if (jedisPool != null) {
            jedisPool.close();
            jedisPool = null;
        }
    }

    public static boolean isPoolEnabled() {
        return jedisPool != null && !jedisPool.isClosed();
    }
}
