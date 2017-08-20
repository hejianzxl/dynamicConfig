package org.dynamicConfig.core.service;

/**
 * <p>
 * Twitter Snowflake 算法精简版
 * </p>
 * Created by Dotions on 2017-06-29.
 */
public class SimpleSnowflakeIdGenServiceImpl implements IdGenService {
	private long workerId;

	// 不用datacenter Id
	// private long datacenterId;

	private long sequence = 0L;

	// 日期起点时间：2017-07-05 14:20:00
	private long twepoch = 1499235600000L; // 起始标记点，作为基准

	// private long workerIdBits = 5L;
	// private long datacenterIdBits = 5L;
	// 不需要datacenterId，只用workerId，扩展到10位
	private long workerIdBits = 10L; // 只允许workid的范围为：0-1023

	private long maxWorkerId = -1L ^ (-1L << workerIdBits);

	// private long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);

	private long sequenceBits = 12L;
	private long workerIdShift = sequenceBits;
	// private long datacenterIdShift = sequenceBits + workerIdBits;

	// private long timestampLeftShift = sequenceBits + workerIdBits +
	// datacenterIdBits;

	private long timestampLeftShift = sequenceBits + workerIdBits;

	private long sequenceMask = -1L ^ (-1L << sequenceBits);
	private long lastTimestamp = -1L;

	public SimpleSnowflakeIdGenServiceImpl(long workerId) {
		super();

		// sanity check for workerId
		// 只允许workId的范围为：0-1023
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(
					String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
		}
		this.workerId = workerId;
	}

	@Override
	public synchronized Long nextId() throws Exception {
		long timestamp = timeGen();
		if (timestamp < lastTimestamp) {
			throw new Exception(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds",
					lastTimestamp - timestamp));
		}

		if (lastTimestamp == timestamp) {
			sequence = (sequence + 1) & sequenceMask;
			if (sequence == 0) {
				timestamp = tilNextMillis(lastTimestamp);
			}
		} else {
			sequence = 0;
		}

		lastTimestamp = timestamp;

		return ((timestamp - twepoch) << timestampLeftShift) |
		// (datacenterId << datacenterIdShift) |
				(workerId << workerIdShift) | sequence;
	}

	/**
	 * 保证返回的毫秒数在参数之后
	 * 
	 * @param lastTimestamp
	 * @return
	 */
	private static long tilNextMillis(long lastTimestamp) {
		long timestamp = timeGen();
		while (timestamp <= lastTimestamp) {
			timestamp = timeGen();
		}
		return timestamp;
	}

	/**
	 * 获得系统当前毫秒数
	 * 
	 * @return
	 */
	private static long timeGen() {
		return System.currentTimeMillis();
	}
	public static void main(String[] args) throws Exception {
		IdGenService idGenService = new SimpleSnowflakeIdGenServiceImpl(10);
		for(int i=0;i < 2 ;i++) {
			System.out.println(idGenService.nextId().toString());
		}
		
		idGenService = new SimpleSnowflakeIdGenServiceImpl(20);
		for(int i=0;i < 2 ;i++) {
			System.out.println(idGenService.nextId().toString());
		}
	}
}
