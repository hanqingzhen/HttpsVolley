/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.volley.toolbox;

import android.util.Log;
import cache.DataCache;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public class StringRequest extends Request<String> {
	private final Listener<String> mListener;
	private String url;
	private final Map<String, String> headers;
	private Map<String, String> params;
	private boolean needCache;

	/**
	 * Creates a new request with the given method.
	 * 
	 * @param method
	 *            the request {@link Method} to use
	 * @param url
	 *            URL to fetch the string at
	 * @param listener
	 *            Listener to receive the String response
	 * @param errorListener
	 *            Error listener, or null to ignore errors
	 */
	public StringRequest(int method, String url, Map<String, String> headers,
			Map<String, String> params, Listener<String> listener,
			ErrorListener errorListener, boolean needCache) {
		super(method, url, errorListener);
		mListener = listener;
		this.url = url;
		this.headers = headers;
		this.params = params;
		this.needCache = needCache;
	}

	// default for POST PUT
	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return params != null ? params : super.getParams();
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		return headers != null ? headers : super.getHeaders();
	}

	/**
	 * Creates a new GET request.
	 * 
	 * @param url
	 *            URL to fetch the string at
	 * @param listener
	 *            Listener to receive the String response
	 * @param errorListener
	 *            Error listener, or null to ignore errors
	 */
	public StringRequest(String url, Listener<String> listener,
			ErrorListener errorListener, boolean needCache) {
		this(Method.GET, url, null, null, listener, errorListener, needCache);
	}

	public StringRequest(String url, Listener<String> listener,
			Map<String, String> headers, ErrorListener errorListener,
			boolean needCache) {
		this(Method.GET, url, headers, null, listener, errorListener, needCache);
	}

	@Override
	protected void deliverResponse(String response) {
		mListener.onResponse(response);
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		String parsed;
		try {

			parsed = new String(response.data,
					HttpHeaderParser.parseCharset(response.headers));
			Log.e("-----JSON----", parsed);
			if (needCache) {
				if (!parsed.equals(DataCache.getDataCache().queryCache(url))) {

					DataCache.getDataCache().saveToCache(url, parsed);
				}
			}
		} catch (UnsupportedEncodingException e) {
			parsed = new String(response.data);
		}
		return Response.success(parsed,
				HttpHeaderParser.parseCacheHeaders(response));
	}
}
