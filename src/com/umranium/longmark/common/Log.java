/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.common;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author Umran
 */
public class Log {
    
    public static boolean LOG_TO_CONSOLE = true;
    
    public static Logger createLog(Class t) {
        return Logger.getLogger(t.getName());
    }
    
    private static long getLogKey(String className, Thread t) {
        return (long)className.hashCode() << 32 | t.hashCode();
    }
        
    private static class LogNameCache {
        final Map<Long,String> map;

        public LogNameCache() {
            this.map = new ConcurrentHashMap<Long, String>(250);
        }
        
        private String createName(String className, Thread t) {
            return className+":"+t.getName();
        }
        
        public String getLogName(String className, Thread t) {
            long hash = getLogKey(className, t);
            if (map.containsKey(hash)) {
                return map.get(hash);
            } else {
                synchronized (map) {
                    if (map.containsKey(hash)) {
                        return map.get(hash);
                    } else {
                        String name = createName(className, t);
                        map.put(hash, name);
                        return name;
                    }
                }
            }
        }
    }
    
    private static final LogNameCache LOG_NAME_CACHE = new LogNameCache();
    private static final Map<Long,PrintWriter> outWriterMap = new ConcurrentHashMap<Long, PrintWriter>(250);
    private static final Map<Long,PrintWriter> errWriterMap = new ConcurrentHashMap<Long, PrintWriter>(250);

    private static String getLogName(String className) {
        return LOG_NAME_CACHE.getLogName(className, Thread.currentThread());
    }
    
    private static PrintWriter getWriter(Map<Long,PrintWriter> map, long key,
            String callerClassName, long threadId, Level level, PrintStream printStream) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            synchronized (map) {
               if (map.containsKey(key)) {
                    return map.get(key);
               } else {
                   PrintWriter writer = null;
                   if (LOG_TO_CONSOLE) {
                       writer = new PrintWriter(printStream, true);
                   } else {
                       String loggerName = getLogName(callerClassName);
                       LoggerWrapperWriter loggerWrapper = 
                               new LoggerWrapperWriter(callerClassName, threadId,
                                       Logger.getLogger(loggerName), level);
                       writer = new PrintWriter(loggerWrapper, true);
                   }
                   map.put(key, writer);
                   return writer;
               }
            }
        }
    }
    
    private interface GetCallerClassName {
        String getCallerClassName();
    }
    
    private static class GetCallerClassByReflection implements GetCallerClassName {
        
        private Class reflectionClass;
        private Method getCallerClassMethod;
        private Integer depth;

        public GetCallerClassByReflection() throws ClassNotFoundException, NoSuchMethodException {
            reflectionClass = Class.forName("sun.reflect.Reflection");
            getCallerClassMethod = reflectionClass.getDeclaredMethod("getCallerClass", new Class[]{Integer.TYPE});
            depth = new Integer(3);
        }
        
        @Override
        public String getCallerClassName() {
            try {
                Class callerClass = (Class)getCallerClassMethod.invoke(null, depth);
                String name = callerClass.getName();
                return name;
            } catch (Exception ex) {
                Logger.getLogger(GetCallerClassByReflection.class.getName()).log(Level.SEVERE, "Error while accessing sun.reflect.Reflection.getCallerClass(int)", ex);
                return "#Unknown#Class#";
            }
        }
    }
    
    private static class GetCallerClassByStacktrace implements GetCallerClassName {
        @Override
        public String getCallerClassName() {
            String className = Thread.currentThread().getStackTrace()[3].getClassName();
            return className;
        }
    }
    
    private static GetCallerClassName callerClasNameFactory() {
        try {
            return new GetCallerClassByReflection();
        } catch (Exception ex) {
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, "Error while creating GetCallerClassByReflection class", ex);
            return new GetCallerClassByStacktrace();
        }
    }
    
    private static final GetCallerClassName GET_CALLER_CLASS_NAME = callerClasNameFactory();
    
    public static PrintWriter out() {
        String callerClassName = GET_CALLER_CLASS_NAME.getCallerClassName();
        return getWriter(callerClassName, false);
    }
    
//    public static PrintWriter out(Class callerClass) {
//        return getWriter(callerClass.getCanonicalName(), false);
//    }
    
    public static PrintWriter err() {
        String callerClassName = GET_CALLER_CLASS_NAME.getCallerClassName();
        return getWriter(callerClassName, true);
    }
    
//    public static PrintWriter err(Class callerClass) {
//        return getWriter(callerClass.getCanonicalName(), true);
//    }
    
    private static PrintWriter getWriter(String callerClassName, boolean error) {
        Thread currentThread = Thread.currentThread();
        long key = getLogKey(callerClassName, currentThread);
        if (error)
            return getWriter(errWriterMap, key, callerClassName, currentThread.getId(), Level.SEVERE, System.err);
        else
            return getWriter(outWriterMap, key, callerClassName, currentThread.getId(), Level.INFO, System.out);
    }
    
    public static void logError(Class callerClass, String msg, Throwable e) {
        PrintWriter errWriter = getWriter(callerClass.getName(), true);
        errWriter.println(msg);
        e.printStackTrace(errWriter);
        errWriter.flush();
    }
    
    public static void logError(Class callerClass, String msg) {
        PrintWriter errWriter = getWriter(callerClass.getName(), true);
        errWriter.println(msg);
        errWriter.flush();
    }
    
    public static void logInfo(Class callerClass, String msg) {
        PrintWriter errWriter = getWriter(callerClass.getName(), false);
        errWriter.println(msg);
        errWriter.flush();
    }
    
    private static class LoggerWrapperWriter extends Writer {
        
        private String callerClassName;
        private long threadId;
        private final StringBuilder builder;
        private final Logger logger;
        private Level level;

        public LoggerWrapperWriter(String callerClassName, long threadId, Logger logger, Level level) {
            this.callerClassName = callerClassName;
            this.threadId = threadId;
            this.logger = logger;
            this.level = level;
            this.builder = new StringBuilder(1024);
        }
        
        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            synchronized (this.builder) {
                this.builder.append(cbuf, off, len);
            }
        }

        @Override
        public void flush() throws IOException {
            synchronized (this.builder) {
                LogRecord record = new LogRecord(level, this.builder.toString());
                record.setSourceClassName(callerClassName);
                record.setThreadID((int)threadId);
                record.setSourceMethodName("");
                logger.log(record);
                this.builder.setLength(0);
            }
        }

        @Override
        public void close() throws IOException {
        }
        
    }
    
}
