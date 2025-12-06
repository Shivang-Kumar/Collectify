package edu.tcu.cs.hogwarts_artifacts_online.rediscache;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisLeaderboardCacheClient {
	
	private final RedisTemplate  redisTemplate;
	
	public RedisLeaderboardCacheClient(RedisTemplate redisTemplate) {
		super();
		this.redisTemplate = redisTemplate;
	}

	//Generate key to look for correct Sorted Set
	private static  String getKey(String entityType, String property)
	{
		return (entityType+"-leaderboard-"+property);
	}
	
	
	public void setScore(String entityType, String property,String entity,double score)
	{
		redisTemplate.opsForZSet().add(getKey(entityType,property),entity,score);
	}
	
	
	public boolean hasKey(String entityType,String property)
	{
		return redisTemplate.hasKey(getKey(entityType,property));
	}
	
	public Set<ZSetOperations.TypedTuple<String>> getTop(String entityType, String property,int limit)
	{
		return redisTemplate.opsForZSet().reverseRangeByScore(getKey(entityType,property), 0, limit-1);
	}
	
	
	
	
	
	

}
