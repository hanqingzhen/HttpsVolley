package com.android.volley.toolbox;

import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

public class ByteRequest  extends Request<byte[]> {
	private final Listener<byte[]> mListener;
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
	public ByteRequest(int method, String url, Map<String, String> headers,
			Map<String, String> params, Listener<byte[]> listener,
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
	public ByteRequest(String url, Listener<byte[]> listener,
			ErrorListener errorListener, boolean needCache) {
		this(Method.GET, url, null, null, listener, errorListener, needCache);
	}

	public ByteRequest(String url, Listener<byte[]> listener,
			Map<String, String> headers, ErrorListener errorListener,
			boolean needCache) {
		this(Method.GET, url, headers, null, listener, errorListener, needCache);
	}

	@Override
	protected void deliverResponse(byte[] response) {
		mListener.onResponse(response);
	}

	@Override
	protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
		
		return Response.success(response.data,
				HttpHeaderParser.parseCacheHeaders(response));
	}

	
}
