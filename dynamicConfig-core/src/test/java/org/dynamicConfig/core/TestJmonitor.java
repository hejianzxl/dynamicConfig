package org.dynamicConfig.core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestJmonitor {
    

    public static void main(String[] args) throws InterruptedException{
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        // 堆内存信息
        service.scheduleAtFixedRate(() -> {
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = mem.getHeapMemoryUsage();
            
            /**
             * used表示当前已经使用的内存量
             * committed表示保证可以由 Java 虚拟机使用的内存量（以字节为单位）
             * max表示可以用于内存管理的最大内存量
             */
            System.out.println("committed是当前可使用的内存大小（包括已使用的）:" + (heap.getCommitted() / 1024.0) / 1024 + "堆初始化大小 init:" + (heap.getInit() / 1024.0) / 1024 + " 最大堆大小 max:" + (heap.getMax() / 1024.0) / 1024 + "已使用堆大小 used:" + (heap.getUsed() / 1024.0) / 1024);
            List<MemoryPoolMXBean> mbs = ManagementFactory.getMemoryPoolMXBeans();
            mbs.forEach((m) -> {
                if (m.getName().contains("Old") || m.getName().contains("Survivor") || m.getName().contains("Eden")) {
                    //返回 Java 虚拟机最近回收了此内存池中的不使用的对象之后的内存使用量。
                    System.out.println("回收后：" + m.getName() + ": Committed=" + m.getCollectionUsage().getCommitted() / 1024d / 1024 + " Init=" + m.getCollectionUsage().getInit() / 1024d / 1024 + " Max=" + m.getCollectionUsage().getMax() / 1024d / 1024 + " Used=" + m.getCollectionUsage().getUsed() / 1024d / 1024);

                    //内存使用量  返回此内存池的内存使用量的估计数。
                    System.out.println("内存使用" + m.getName() + " Committed=" + m.getUsage().getCommitted() / 1024d / 1024 + " Init=" + m.getUsage().getInit() / 1024d / 1024 + " Max=" + m.getUsage().getMax() / 1024d / 1024 + " Used=" + m.getUsage().getUsed() / 1024d / 1024);
                }
            });
            
            List<GarbageCollectorMXBean> garbages = ManagementFactory.getGarbageCollectorMXBeans();  
            for(GarbageCollectorMXBean garbage : garbages){  
                System.out.println("垃圾收集器：名称="+garbage.getName()+",收集="+garbage.getCollectionCount()+",总花费时间="  
            +garbage.getCollectionTime()+"毫秒,内存区名称="+Arrays.deepToString(garbage.getMemoryPoolNames()));  
            }  
            
            // class load info
            ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();
            System.out.println("JVM load class数量：" + classLoadingMXBean.getLoadedClassCount());
            System.out.println("JVM 装载class总数：" + classLoadingMXBean.getTotalLoadedClassCount());
            System.out.println("JVM 卸载class数量：" + classLoadingMXBean.getUnloadedClassCount());
            
            //=====================线程信息==========================//
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            System.out.println("当前线程数：" + threadMXBean.getThreadCount());
            System.out.println("守护线程数:" + threadMXBean.getDaemonThreadCount());
            System.out.println("最高线程数：" + threadMXBean.getPeakThreadCount());
            
            OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();
            System.out.println("可用处理器数量：" + system.getAvailableProcessors());
            System.out.println("系统CPU平均负载：" + system.getSystemLoadAverage() * 100d);
            
            long totalPhysicalMemory = getLongFromOperatingSystem(system,"getTotalPhysicalMemorySize");  
            long freePhysicalMemory = getLongFromOperatingSystem(system, "getFreePhysicalMemorySize");  
            long usedPhysicalMemorySize =totalPhysicalMemory - freePhysicalMemory;  
            //long processCpuTime = getLongFromOperatingSystem(system,"getProcessCpuTime");
            
            System.out.println("总物理内存(M):"+totalPhysicalMemory/1024/1024);  
            System.out.println("已用物理内存(M):"+usedPhysicalMemorySize/1024/1024);  
            System.out.println("剩余物理内存(M):"+freePhysicalMemory/1024/1024); 
            System.out.println("=======================================================");
            /*
            long  totalSwapSpaceSize = getLongFromOperatingSystem(system, "getTotalSwapSpaceSize");  
            long freeSwapSpaceSize = getLongFromOperatingSystem(system, "getFreeSwapSpaceSize");  
            long usedSwapSpaceSize = totalSwapSpaceSize - freeSwapSpaceSize;  
              
            System.out.println("总交换空间(M):"+totalSwapSpaceSize/1024/1024);  
            System.out.println("已用交换空间(M):"+usedSwapSpaceSize/1024/1024);  
            System.out.println("剩余交换空间(M):"+freeSwapSpaceSize/1024/1024);*/
            
            
            //计算CPU占用率
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            
        }, 1, 5, TimeUnit.SECONDS);

        for (int i = 0; i < 10000; i++) {
            byte[] bytes = new byte[1024 * 1024 * 50];
            Thread.sleep(500);
        }

        // JVM close 钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            service.shutdown();
        }));
    }
    
    private static long getLongFromOperatingSystem(OperatingSystemMXBean operatingSystem, String methodName) {  
        try {  
            final Method method = operatingSystem.getClass().getMethod(methodName,  
                    (Class<?>[]) null);  
            method.setAccessible(true);  
            return (Long) method.invoke(operatingSystem, (Object[]) null);  
        } catch (final InvocationTargetException e) {  
            if (e.getCause() instanceof Error) {  
                throw (Error) e.getCause();  
            } else if (e.getCause() instanceof RuntimeException) {  
                throw (RuntimeException) e.getCause();  
            }  
            throw new IllegalStateException(e.getCause());  
        } catch (final NoSuchMethodException e) {  
            throw new IllegalArgumentException(e);  
        } catch (final IllegalAccessException e) {  
            throw new IllegalStateException(e);  
        }  
    }  
    
    
    private static double getCpuRateForLinux(){   
        InputStream is = null;   
        InputStreamReader isr = null;   
        BufferedReader brStat = null;   
        StringTokenizer tokenStat = null;   
        try{   
  
            Process process = Runtime.getRuntime().exec("top -b -n 1");   
            is = process.getInputStream();                     
            isr = new InputStreamReader(is);   
            brStat = new BufferedReader(isr);   
             
            if("2.4".equals("2.4")){   
                brStat.readLine();   
                brStat.readLine();   
                brStat.readLine();   
                brStat.readLine();   
                 
                tokenStat = new StringTokenizer(brStat.readLine());   
                tokenStat.nextToken();   
                tokenStat.nextToken();   
                String user = tokenStat.nextToken();   
                tokenStat.nextToken();   
                String system = tokenStat.nextToken();   
                tokenStat.nextToken();   
                String nice = tokenStat.nextToken();   
                 
                System.out.println(user+" , "+system+" , "+nice);   
                 
                user = user.substring(0,user.indexOf("%"));   
                system = system.substring(0,system.indexOf("%"));   
                nice = nice.substring(0,nice.indexOf("%"));   
                 
                float userUsage = new Float(user).floatValue();   
                float systemUsage = new Float(system).floatValue();   
                float niceUsage = new Float(nice).floatValue();   
                 
                return (userUsage+systemUsage+niceUsage)/100;   
            }else{   
                brStat.readLine();   
                brStat.readLine();   
                     
                tokenStat = new StringTokenizer(brStat.readLine());   
                tokenStat.nextToken();   
                tokenStat.nextToken();   
                tokenStat.nextToken();   
                tokenStat.nextToken();   
                tokenStat.nextToken();   
                tokenStat.nextToken();   
                tokenStat.nextToken();   
                String cpuUsage = tokenStat.nextToken();   
                     
                 
                System.out.println("CPU idle : "+cpuUsage);   
                Float usage = new Float(cpuUsage.substring(0,cpuUsage.indexOf("%")));   
                 
                return (1-usage.floatValue()/100);   
            }   
  
              
        } catch(IOException ioe){   
            System.out.println(ioe.getMessage());   
            return 1;   
        } finally{   
        }   
    }
}
