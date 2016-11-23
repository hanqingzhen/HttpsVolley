package com.android.volley.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
/**
 * Volley的工具 
 * 可直接通过getInstance()实例化
 * 简化创建RequestQueue和ImageLoader
 * @author dyh
 *
 */
public class VolleyTool {
	private static VolleyTool mInstance = null;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;
    
    private VolleyTool(Context context) {
    	requestQueue = Volley.newRequestQueue(context);
    	imageLoader = new ImageLoader(requestQueue, new BitmapCache());
    }
    
    public static VolleyTool getInstance(Context context){
        if(mInstance == null){
    		mInstance = new VolleyTool(context);
        }
        return mInstance;
    }
    
	public RequestQueue getRequestQueue() {
		return requestQueue;
	}

	public ImageLoader getImageLoader() {
		return imageLoader;
	}

	public void release() {
		this.imageLoader = null;
		this.requestQueue = null;
		mInstance = null;
	}
}
