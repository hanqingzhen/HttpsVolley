package com.android.volley.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
/**
 * 设置大小之后的ImageCache缓存
 * 将会缓存于data/data/应用
 * @author dyh
 *
 */
@SuppressLint("NewApi")
public class BitmapCache implements ImageCache {
	private LruCache<String, Bitmap> mCache;
	private int CacheSize = 10;// 缓存空间最大值 M

	public BitmapCache() {
		int maxSize = CacheSize * 1024 * 1024;
		mCache = new LruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}

		};
	}

	@Override
	public Bitmap getBitmap(String url) {
		return mCache.get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		mCache.put(url, bitmap);
	}
	/**
	 *  缓存空间最大值 M
	 * @param cacheSize
	 */
	public int getCacheSize() {
		return CacheSize;
	}

	/**
	 *  缓存空间最大值 M
	 * @param cacheSize
	 */
	public void setCacheSize(int cacheSize) {
		CacheSize = cacheSize;
	}

}
