/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.umranium.longmark.model;

import com.umranium.longmark.common.AsyncResultsReceiver;
import com.umranium.longmark.common.BackgroundProcessor;
import java.awt.Image;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 *
 * @author umran
 */
public abstract class ImageCache<ImageType extends Image> {
    
    private int imageCount;
    private AtomicReferenceArray<SoftReference<ImageType>> cachedImages;
    private Object[] mutexs;

    public ImageCache(int imageCount) {
        this.imageCount = imageCount;
        this.cachedImages = new AtomicReferenceArray<SoftReference<ImageType>>(imageCount);
        this.mutexs = new Object[imageCount];
        
        for (int i=0; i<imageCount; ++i) {
            this.mutexs[i] = new Object();
        }
    }
    
    public void clear() {
        for (int index=0; index<imageCount; ++index) {
            synchronized (mutexs[index]) {
                cachedImages.set(index, null);
            }
        }
    }
    
    private ImageType internGetImage(int index) {
        SoftReference<ImageType> imgRef = cachedImages.get(index);
        ImageType img = null;
        if (imgRef!=null) {
            img = imgRef.get();
        }
        return img;
    }
    
    public ImageType getImage(int index) {
        ImageType img = internGetImage(index);
        if (img!=null) {
            return img;
        }
        
        //  make sure that no image is loaded by two different threads
        //      simultaneously 
        synchronized (mutexs[index]) {
            //  check again in the mutex, whether the image is in the cache
            img = internGetImage(index);
            if (img!=null) {
                return img;
            }
            
            //  load image, and store in the cache
            img = loadImage(index);
            cachedImages.set(index, new SoftReference<ImageType>(img));
            return img;
        }
    }
    
    public Future getImageAsync(int index,
            BackgroundProcessor processor, AsyncResultsReceiver<ImageType> results) {
        ImageType scaledImg = internGetImage(index);
        if (scaledImg!=null) {
            //System.out.println("\t\t\t\timmediate return.");
            results.onResults(scaledImg);
            return null;
        }
        return processor.submit(new AsyncImageLoader(index), results);
    }
    
    public Map<Integer,Image> getImages(boolean load) {
        Map<Integer,Image> map = new HashMap<Integer,Image>(imageCount);
        for (int i=0; i<imageCount; ++i) {
            ImageType img;
            if (load) {
                img = getImage(i);
            } else {
                img = internGetImage(i);
            }
            if (img!=null) {
                map.put(i, img);
            }
        }
        return map;
    }
    
    protected abstract ImageType loadImage(int index);
    
    
    private class AsyncImageLoader implements Callable<ImageType> {
        
        private int index;

        public AsyncImageLoader(int index) {
            this.index = index;
        }
        
        @Override
        public ImageType call() throws Exception {
            if (!Thread.currentThread().getName().startsWith("pool-")) {
                throw new Exception("Loader called from a non-pooled thread.");
            }
            //System.out.println("scaledImageLoader called");
            return getImage(index);
        }
    }
    
    
}
