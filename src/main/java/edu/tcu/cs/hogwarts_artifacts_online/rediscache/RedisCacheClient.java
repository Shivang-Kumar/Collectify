package edu.tcu.cs.hogwarts_artifacts_online.rediscache;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;

@Service
public class RedisCacheClient {
	
	private final StringRedisTemplate redisTemplate;
	
	public RedisCacheClient(StringRedisTemplate redisTemplate)
	{
		this.redisTemplate=redisTemplate;
	}
	
	@Traced("redisCacheClient.set")
	@Logged
	public void set(String key, String value, int timeout, TimeUnit timeUnit)
	{
		this.redisTemplate.opsForValue().set(key, value,timeout,timeUnit);
	}
	
	@Traced("redisCacheClient.get")
	@Logged
	public String get(String key)
	{
		return this.redisTemplate.opsForValue().get(key);
	}
	
	
	@Traced("redisCacheClient.delete")
	@Logged
	public void delete(String key)
	{
		this.redisTemplate.delete(key);
	}
	
	
	@Traced("redisCacheClient.isUserTokenInWhiteList")
	@Logged
	public boolean isUserTokenInWhiteList(String userId,String tokenFromRequest)
	{
		String tokenFromRedis=get("whitelist:"+userId);
		boolean ans= tokenFromRedis != null && tokenFromRedis.equals(tokenFromRequest);
		System.out.println("token in redis "+userId+" and token from request"+tokenFromRequest);

		System.out.println("checking token in redis and request"+ans);
		return ans;
	}
	
}
