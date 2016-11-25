#Volley支持Https及自签名证书

##1.https是什么？
简单讲，**https 是在http的基础上增加了SSL/TLS协议**。
详细参见[HTTPS传输加密原理](http://www.iops.cc/principle-of-encrypting-transmission-with-https/)

##2.Android支持的证书类型有哪些？
1）**受信证书**（由安卓认可的证书颁发机构, 或这些机构的下属机构颁发的证书）详细参见[受信任的证书颁发机构](https://www.sslshopper.com/certificate-authority-reviews.html)
2）**不受信证书**（没有得到安卓认可的证书颁发机构颁发的证书）
3）**自签名证书**（自己颁发的证书, 分临时性的(在开发阶段使用)或在发布的产品中永久性使用的两种）

## 3.为什么使用自签名证书？
1）**免费**（ 购买受信任机构颁发的证书每年要交 100 到 500 美元不等的费用. 自签名证书不花一分钱）
2）**普及率高**（自签名证书在手机应用中的普及率较高 ，跟用电脑浏览网页不同, 手机的应用一般就固定连一台服务器）
3）**方便**（在开发阶段写的代码,  测试跟发布的时候也可以用）

##4.Volley如何支持https？
1）受信证书，不需要修改代码，直接使用，就像SSL/TLS协议透明
2）不受信证书和自签名证书，需要修改Volley库代码（Volley底层支持，但是没有暴露出来方法）


##5.如何修改Volley库代码？
1）clone volley库
a.从[Google Repository](https://android.googlesource.com/platform/frameworks/volley)  clone
b.从[清华镜像](https://mirrors.tuna.tsinghua.edu.cn/help/AOSP/)  clone
2）代码修改
![volley_https_ssl.png](http://upload-images.jianshu.io/upload_images/2709478-5ef2d35c058b74b4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
3）SSLSocketHelper.java

    import android.content.Context;
    import android.util.Log;

    import java.io.IOException;
    import java.io.InputStream;
    import java.security.KeyManagementException;
    import java.security.KeyStore;
    import java.security.KeyStoreException;
    import java.security.NoSuchAlgorithmException;
    import java.security.cert.Certificate;
    import java.security.cert.CertificateException;
    import java.security.cert.CertificateFactory;

    import javax.net.ssl.SSLContext;
    import javax.net.ssl.SSLSocketFactory;
    import javax.net.ssl.TrustManager;
    import javax.net.ssl.TrustManagerFactory;
    import javax.net.ssl.X509TrustManager;
    import java.security.cert.X509Certificate;


    public class SSLSocketHelper {
        private static TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
            final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
            return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkClientTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkClientTrusted", e.toString());
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkServerTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkServerTrusted", e.toString());
                        }
                    }
                }
        };
    }

    public static SSLSocketFactory getSSLSocketFactoryByCertificate(Context context,String keyStoreType, int keystoreResId)
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = context.getResources().openRawResource(keystoreResId);

        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();

        if (keyStoreType == null || keyStoreType.length() == 0) {
            keyStoreType = KeyStore.getDefaultType();
        }
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext.getSocketFactory();
    }

    public static SSLSocketFactory getSSLSocketFactoryByKeyStore(Context context,String keyStoreType, int keystoreResId, String keyPassword)
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {

        InputStream caInput = context.getResources().openRawResource(keystoreResId);

        // creating a KeyStore containing trusted CAs

        if (keyStoreType == null || keyStoreType.length() == 0) {
            keyStoreType = KeyStore.getDefaultType();
        }
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);

        keyStore.load(caInput, keyPassword.toCharArray());

        // creating a TrustManager that trusts the CAs in the KeyStore

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext.getSocketFactory();
        }
    }

4）详细可参看[GitHub工程](https://github.com/hanqingzhen/HttpsVolley)
[GitHub工程](https://github.com/hanqingzhen/HttpsVolley)与原始的volley对比：
a.增加了cache包，com.android.volley.ssl包，com.android.volley.utils包
b.在com.android.volley.toolbox包中，新增ByteRequest.java,GsonRequest.java,JsonArrayPostRequest.java,JsonObjectPostRequest.java,修改了Volley.java
c.只有com.android.volley.ssl包和Volley.java与支持https自签名证书有关系

##6.如何自签名证书？
一般是运维搞，可参考如下链接：
[使用 OpenSSL 生成自签名证书](http://www.ibm.com/support/knowledgecenter/zh/SSWHYP_4.0.0/com.ibm.apimgmt.cmc.doc/task_apionprem_gernerate_self_signed_openSSL.html)
[基于OpenSSL自建CA和颁发SSL证书](http://seanlook.com/2015/01/18/openssl-self-sign-ca/)
[使用openssl生成自签名证书以及nginx ssl双向验证](https://my.oschina.net/u/2457218/blog/637866)
[创建并部署自签名的 SSL 证书到 Nginx](https://hinine.com/create-and-deploy-a-self-signed-ssl-certificate-to-nginx/)

##7.本工程在[CFCA](http://www.cfca.com.cn/)证书中验证通过,也可直接使用jar包：httpsVolley/build/intermediates/bundles/release/classes.jar

##8.信任所有证书，实现简单，但有风险，不能在产品中使用。可参考[【第六篇】Volley之https相关](http://www.cnblogs.com/androidsuperman/p/4811695.html)
##9.WebView 支持https

    import android.webkit.WebView;
    import android.webkit.WebViewClient;
    import android.webkit.SslErrorHandler;
    import android.net.http.SslError;

    private WebView webView;

    webView = (WebView) findViewById(R.id.my_webview);
    webView.setWebViewClient(new WebViewClient() {

    @Override
    public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    });
##10.参考链接
[国内镜像加速Android源码下载](http://sunjiajia.com/2015/08/14/download-android-open-source-projects/)
[通过 HTTPS 和 SSL 确保安全](https://developer.android.com/training/articles/security-ssl.html)
[Certificate authority](https://en.wikipedia.org/wiki/Certificate_authority)
[ 清华大学开源软件镜像站](https://mirrors.tuna.tsinghua.edu.cn/)
[Does the Web View on Android support SSL?](http://stackoverflow.com/questions/5977977/does-the-web-view-on-android-support-ssl)
[Android _实现SSL解决不受信任的证书问题](http://blog.csdn.net/zimo2013/article/details/45190585)
[Using Android Volley With Self-Signed SSL Certificate](http://ogrelab.ikratko.com/using-android-volley-with-self-signed-certificate/)
[Android volley self signed HTTPS trust anchor for certification path not found](http://stackoverflow.com/questions/32154115/android-volley-self-signed-https-trust-anchor-for-certification-path-not-found)
[Android 网络--我是怎么做的: Volley+OkHttp+Https](http://www.jianshu.com/p/e58161cbc3a4)
[Making a HTTPS request using Android Volley](http://stackoverflow.com/questions/17045795/making-a-https-request-using-android-volley)

