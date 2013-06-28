/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.common;

import com.umranium.longmark.common.AsyncResultsReceiver;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A centralized unit to handle background processing tasks.
 *
 * @author umran
 */
public class BackgroundProcessor {
    
    public static final BackgroundProcessor INSTANCE = new BackgroundProcessor();
    
    private final ExecutorService executor;

    public BackgroundProcessor() {
        executor = Executors.newCachedThreadPool();
        init();
    }
    
    public BackgroundProcessor(int fixedPoolSize) {
        executor = Executors.newFixedThreadPool(fixedPoolSize);
        init();
    }
    
    private void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                executor.shutdown();
            }
        });
    }
    
    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public Future<?> submit(Runnable task) {
        return executor.submit(task);
    }
    
    public <T> Future submit(final Callable<T> task, final AsyncResultsReceiver<T> asyncResult) {
        return executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (asyncResult.stillValid()) {
                        T result = task.call();
                        asyncResult.onResults(result);
                    } else {
                        asyncResult.onCancelled();
                    }
                } catch (Exception ex) {
                    asyncResult.onException(ex);
                }
            }
        });
    }
}
