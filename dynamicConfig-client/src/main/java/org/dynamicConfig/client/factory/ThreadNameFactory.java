package org.dynamicConfig.client.factory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadNameFactory{

	public static ThreadFactory createNameThreadFactory(String prefix) {
		return new ThreadFactory() {
            private final AtomicLong atomicLong = new AtomicLong(0);
			
            @Override
			public Thread newThread(Runnable r) {
				return new Thread(r, prefix+ "_" + atomicLong.get());
			}
		};
	}
}
