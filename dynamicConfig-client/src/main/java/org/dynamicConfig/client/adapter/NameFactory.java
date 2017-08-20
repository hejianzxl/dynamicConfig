package org.dynamicConfig.client.adapter;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class NameFactory {
	private AtomicInteger	atomicInteger	= new AtomicInteger();

	private static String	DEFAULT			= "_default";

	private NameFactory() {
		// ignore
	}

	public static NameFactory init() {
		return ChannelHandler.INSTANCE;
	}

	private static class ChannelHandler {
		private static final NameFactory INSTANCE = new NameFactory();
	}
	public String create() {
		return UUID.randomUUID() + DEFAULT + atomicInteger.getAndIncrement();
	}

}
