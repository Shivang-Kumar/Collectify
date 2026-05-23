package edu.tcu.cs.hogwarts_artifacts_online.rediscache;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.tcu.cs.hogwarts_artifacts_online.observability.logging.Logged;
import edu.tcu.cs.hogwarts_artifacts_online.observability.tracing.Traced;

@Service
public class RedisLeaderboardCacheClient {
	
	private final RedisTemplate  redisTemplate;
	
	public RedisLeaderboardCacheClient(RedisTemplate redisTemplate) {
		super();
		this.redisTemplate = redisTemplate;
	}

	//Generate key to look for correct Sorted Set
	@Traced("redisLeaderBoardCacheClient.getLeaderboardKey")
	@Logged
	private static  String getLeaderboardKey(String entityType, String property)
	{
		return (entityType+"-leaderboard-"+property);
	}
	@Traced("redisLeaderBoardCacheClient.getEntityKey")
	@Logged
	private static  String getEntityKey(String entityType)
	{
		return (entityType+"-entity");
	}
	
	
	@Traced("redisLeaderBoardCacheClient.setScore")
	@Logged
	public void setScore(String entityType, String property,String entityId,double score)
	{
		redisTemplate.opsForZSet().add(getLeaderboardKey(entityType,property),entityId,score);
	}
	
	@Traced("redisLeaderBoardCacheClient.hasKey")
	@Logged
	public boolean hasKey(String entityType,String property)
	{
		
		return redisTemplate.hasKey(getLeaderboardKey(entityType,property));
	}
	
	@Traced("redisLeaderBoardCacheClient.getTop")
	@Logged
	public Set<ZSetOperations.TypedTuple<String>> getTop(String entityType, String property,int limit)
	{
		return redisTemplate.opsForZSet().reverseRangeWithScores(getLeaderboardKey(entityType,property), 0, limit-1);
	}
	
	
	

	@Traced("redisLeaderBoardCacheClient.saveEntityOfLeaderBoard")
	@Logged
	//For storing entity in redis set for efficient leaderboard fetching
	public void saveEntityOfLeaderBoard(String entityType,String entityId,String obj) 
	{
		redisTemplate.opsForHash().put(getEntityKey(entityType), entityId,obj);
	}

	@Traced("redisLeaderBoardCacheClient.getEntityOfLeaderboard")
	@Logged
	public Object getEntityOfLeaderboard(String entityType, String id) {
		return this.redisTemplate.opsForHash().get(getEntityKey(entityType), id);
		
	}
	
	@Traced("redisLeaderBoardCacheClient.getEntityRank")
	@Logged
	public long getEntityRank(String entityType,String property,String id)
	{
		return this.redisTemplate.opsForZSet().rank(getLeaderboardKey(entityType, property), id);
	}


	
	

}
