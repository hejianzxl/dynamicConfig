package org.dynamicConfig.client.listener;

import org.springframework.context.ApplicationContext;

import redis.clients.jedis.JedisCluster;

public class NotifyThread implements Runnable {
	
	private String groupId;
	
	private JedisCluster jedisCluster;
	
	private ApplicationContext applicationContext;

	public NotifyThread(String groupId, JedisCluster jedisCluster, ApplicationContext applicationContext) {
		super();
		this.groupId = groupId;
		this.jedisCluster = jedisCluster;
		this.applicationContext = applicationContext;
	}

	@Override
	public void run() {
		System.out.println("=========================");
		jedisCluster.subscribe(new Notifiy(applicationContext), groupId);
	}
}
