package com.zxs.common;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.SSLInitializationException;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;
import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 基于HttpClient实现的Http请求工具
 *
 * @author corbett
 * @description 支持POST和GET请求, 支持SSL
 * @description HttpClient 4.5.2
 * @description fastjson 1.2.31
 * <p>
 * <dependency>
 * <groupId>org.apache.httpcomponents</groupId>
 * <artifactId>httpclient</artifactId>
 * <version>4.5.2</version>
 * </dependency>
 * <p>
 * <dependency>
 * <groupId>com.alibaba</groupId>
 * <artifactId>fastjson</artifactId>
 * <version>1.2.31</version>
 * </dependency>
 */
public class HttpsRequestUtils {

    /**
     * 连接池
     */
    private static PoolingHttpClientConnectionManager connManager;

    /**
     * 编码
     */
    private static final String ENCODING = "UTF-8";

    /**
     * 出错返回结果
     */
    private static final String RESULT = "-1";

    /**
     * 初始化连接池管理器,配置SSL
     */
    static {
        if (connManager == null) {

            try {
                // 创建ssl安全访问连接
                // 获取创建ssl上下文对象
                SSLContext sslContext = getSSLContext(true, null, null);

                // 注册
                Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", new SSLConnectionSocketFactory(sslContext))
                        .build();

                // ssl注册到连接池
                connManager = new PoolingHttpClientConnectionManager(registry);
                connManager.setMaxTotal(1000);  // 连接池最大连接数
                connManager.setDefaultMaxPerRoute(20);  // 每个路由最大连接数

            } catch (SSLInitializationException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 获取客户端连接对象
     *
     * @param timeOut 超时时间
     * @return
     */
    private static CloseableHttpClient getHttpClient(Integer timeOut) {
        return getHttpClient(timeOut, null, null);
    }

    /**
     * 获取客户端连接对象
     *
     * @param timeOut 超时时间
     * @return
     */
    private static CloseableHttpClient getHttpClient(Integer timeOut, CookieStore cookieStore) {
        return getHttpClient(timeOut, cookieStore, null);
    }

    /**
     * 获取客户端连接对象
     *
     * @param timeOut 超时时间
     * @return
     */
    private static CloseableHttpClient getHttpClient(Integer timeOut, HttpHost proxy) {
        return getHttpClient(timeOut, null, proxy);
    }

    /**
     * 获取客户端连接对象
     *
     * @param timeOut 超时时间
     * @return
     */
    private static CloseableHttpClient getHttpClient(Integer timeOut, CookieStore cookieStore, HttpHost proxy) {

        // 配置请求参数
        RequestConfig.Builder builder = RequestConfig.custom()
                .setConnectionRequestTimeout(timeOut).
                        setConnectTimeout(timeOut).
                        setSocketTimeout(timeOut);

        if (proxy != null) {
            builder.setProxy(proxy);
        }

        RequestConfig requestConfig = builder.build();
        // 配置超时回调机制
        HttpRequestRetryHandler retryHandler = (exception, executionCount, context) -> {
            if (executionCount >= 3) {// 如果已经重试了3次，就放弃
                return false;
            }
            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                return false;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return true;
            }
            if (exception instanceof UnknownHostException) {// 目标服务器不可达
                return false;
            }
            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                return false;
            }
            if (exception instanceof SSLException) {// ssl握手异常
                return false;
            }
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        };

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(retryHandler);
        if (cookieStore != null) {
            httpClientBuilder.setDefaultCookieStore(cookieStore);
        }
        return httpClientBuilder.build();

    }

    /**
     * 获取SSL上下文对象,用来构建SSL Socket连接
     *
     * @param isDeceive 是否绕过SSL
     * @param creFile   整数文件,isDeceive为true 可传null
     * @param crePwd    整数密码,isDeceive为true 可传null, 空字符为没有密码
     * @return SSL上下文对象
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws CertificateException
     */
    private static SSLContext getSSLContext(boolean isDeceive, File creFile, String crePwd) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, FileNotFoundException, IOException {

        SSLContext sslContext = null;

        if (isDeceive) {
            sslContext = SSLContext.getInstance("SSLv3");
            // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
            X509TrustManager x509TrustManager = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }
            };
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
        } else {
            if (null != creFile && creFile.length() > 0) {
                if (null != crePwd) {
                    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                    keyStore.load(new FileInputStream(creFile), crePwd.toCharArray());
                    sslContext = SSLContexts.custom().loadTrustMaterial(keyStore, new TrustSelfSignedStrategy()).build();
                } else {
                    throw new SSLHandshakeException("整数密码为空");
                }
            }
        }

        return sslContext;

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, Map<String, Object> params, Integer timeOut) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut);

    }


    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, Map<String, Object> params, Integer timeOut, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, proxy);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, Map<String, Object> params, Integer timeOut, CookieStore cookieStore) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore);

    }


    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, Map<String, Object> params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore, proxy);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> params, Integer timeOut) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> params, Integer timeOut, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, proxy);

    }


    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> params, Integer timeOut, CookieStore cookieStore) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore, proxy);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, JSONObject params, Integer timeOut) throws UnsupportedEncodingException {
        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut);
    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, JSONObject params, Integer timeOut, HttpHost proxy) throws UnsupportedEncodingException {
        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, proxy);
    }

    /**
     * post请求,支持SSL
     *
     * @param url         请求地址
     * @param headers     请求头信息
     * @param params      请求参数
     * @param timeOut     超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @param cookieStore 是否以流的方式获取响应信息
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, JSONObject params, Integer timeOut, CookieStore cookieStore) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        if (cookieStore == null) {
            return getResult(httpPost, timeOut);
        } else {
            return getResult(httpPost, timeOut, cookieStore);
        }
    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, JSONObject params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws UnsupportedEncodingException {
        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore, proxy);
    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, JSONObject params, Integer timeOut) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, JSONObject params, Integer timeOut, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, proxy);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, JSONObject params, Integer timeOut, CookieStore cookieStore) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, JSONObject params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new UrlEncodedFormEntity(covertParams2NVPS(params), ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore, proxy);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数json zifuchuan
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, String params, Integer timeOut) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new StringEntity(params, ENCODING));
        }

        return getResult(httpPost, timeOut);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数json zifuchuan
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, String params, Integer timeOut, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new StringEntity(params, ENCODING));
        }

        return getResult(httpPost, timeOut, proxy);

    }


    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数json zifuchuan
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, String params, Integer timeOut, CookieStore cookieStore) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new StringEntity(params, ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数json zifuchuan
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, String params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new StringEntity(params, ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore, proxy);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数json zifuchuan
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, String params, Integer timeOut) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new StringEntity(params, ENCODING));
        }

        return getResult(httpPost, timeOut);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数json zifuchuan
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, String params, Integer timeOut, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new StringEntity(params, ENCODING));
        }

        return getResult(httpPost, timeOut, proxy);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数json zifuchuan
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, String params, Integer timeOut, CookieStore cookieStore) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new StringEntity(params, ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore);

    }

    /**
     * post请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数json zifuchuan
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws UnsupportedEncodingException
     */
    public static HttpResponse post(String url, Map<String, Object> headers, String params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws UnsupportedEncodingException {

        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        // 添加请求参数信息
        if (null != params) {
            httpPost.setEntity(new StringEntity(params, ENCODING));
        }

        return getResult(httpPost, timeOut, cookieStore, proxy);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> headers, Map<String, Object> params, Integer timeOut) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        return getResult(httpGet, timeOut);

    }


    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> headers, Map<String, Object> params, Integer timeOut, HttpHost proxy) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        return getResult(httpGet, timeOut, proxy);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> headers, Map<String, Object> params, Integer timeOut, CookieStore cookieStore) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        return getResult(httpGet, timeOut, cookieStore);

    }


    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> headers, Map<String, Object> params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        return getResult(httpGet, timeOut, cookieStore, proxy);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> params, Integer timeOut) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return getResult(httpGet, timeOut);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> params, Integer timeOut, HttpHost proxy) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return getResult(httpGet, timeOut, proxy);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> params, Integer timeOut, CookieStore cookieStore) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return getResult(httpGet, timeOut, cookieStore);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return getResult(httpGet, timeOut, cookieStore, proxy);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> headers, JSONObject params, Integer timeOut) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        return getResult(httpGet, timeOut);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> headers, JSONObject params, Integer timeOut, HttpHost proxy) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        return getResult(httpGet, timeOut, proxy);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> headers, JSONObject params, Integer timeOut, CookieStore cookieStore) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        if (cookieStore == null) {
            return getResult(httpGet, timeOut);
        } else {
            return getResult(httpGet, timeOut, cookieStore);
        }
    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param headers 请求头信息
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, Map<String, Object> headers, JSONObject params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws URISyntaxException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }

        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpGet.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }

        return getResult(httpGet, timeOut, cookieStore, proxy);

    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, JSONObject params, Integer timeOut) throws URISyntaxException, IOException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }
        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return getResult(httpGet, timeOut);
    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, JSONObject params, Integer timeOut, HttpHost proxy) throws URISyntaxException, IOException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }
        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return getResult(httpGet, timeOut, proxy);
    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, JSONObject params, Integer timeOut, CookieStore CookieStore) throws URISyntaxException, IOException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }
        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return getResult(httpGet, timeOut, CookieStore);
    }

    /**
     * get请求,支持SSL
     *
     * @param url     请求地址
     * @param params  请求参数
     * @param timeOut 超时时间(毫秒):从连接池获取连接的时间,请求时间,响应时间
     * @return 响应信息
     * @throws URISyntaxException
     */
    public static HttpResponse get(String url, JSONObject params, Integer timeOut, CookieStore cookieStore, HttpHost proxy) throws URISyntaxException, IOException {

        // 构建url
        URIBuilder uriBuilder = new URIBuilder(url);
        // 添加请求参数信息
        if (null != params) {
            uriBuilder.setParameters(covertParams2NVPS(params));
        }
        // 创建post请求
        HttpGet httpGet = new HttpGet(uriBuilder.build());

        return getResult(httpGet, timeOut, cookieStore, proxy);
    }

    public static HttpResponse postFrom(String url, Map<String, Object> headers, JSONObject params, int timeOut) {
        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }


        HttpParams paramss = new BasicHttpParams();

        // 添加请求参数信息
        if (null != params) {
            params.forEach((k, v) -> paramss.setParameter(k, v));
        }

        httpPost.setParams(paramss);

        return getResult(httpPost, timeOut);
    }

    public static HttpResponse postFrom(String url, Map<String, Object> headers, JSONObject params, int timeOut, HttpHost proxy) {
        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }


        HttpParams paramss = new BasicHttpParams();

        // 添加请求参数信息
        if (null != params) {
            params.forEach((k, v) -> paramss.setParameter(k, v));
        }

        httpPost.setParams(paramss);

        return getResult(httpPost, timeOut, proxy);
    }

    public static HttpResponse postFrom(String url, Map<String, Object> headers, JSONObject params, int timeOut, CookieStore cookieStore) {
        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }


        HttpParams paramss = new BasicHttpParams();

        // 添加请求参数信息
        if (null != params) {
            params.forEach((k, v) -> paramss.setParameter(k, v));
        }

        httpPost.setParams(paramss);

        return getResult(httpPost, timeOut, cookieStore);
    }

    public static HttpResponse postFrom(String url, Map<String, Object> headers, JSONObject params, int timeOut, CookieStore cookieStore, HttpHost proxy) {
        // 创建post请求
        HttpPost httpPost = new HttpPost(url);

        // 添加请求头信息
        if (null != headers) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                httpPost.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }


        HttpParams paramss = new BasicHttpParams();

        // 添加请求参数信息
        if (null != params) {
            params.forEach((k, v) -> paramss.setParameter(k, v));
        }

        httpPost.setParams(paramss);

        return getResult(httpPost, timeOut, cookieStore, proxy);
    }

    private static HttpResponse getResult(HttpRequestBase httpRequest, Integer timeOut) {
        return getResult(httpRequest, timeOut, null, null);
    }

    private static HttpResponse getResult(HttpRequestBase httpRequest, Integer timeOut, CookieStore cookieStore) {
        return getResult(httpRequest, timeOut, cookieStore, null);
    }

    private static HttpResponse getResult(HttpRequestBase httpRequest, Integer timeOut, HttpHost proxy) {
        return getResult(httpRequest, timeOut, null, proxy);
    }

    private static HttpResponse getResult(HttpRequestBase httpRequest, Integer timeOut, CookieStore cookieStore, HttpHost proxy) {

        // 响应结果
        CloseableHttpResponse response = null;
        try {
            // 获取连接客户端
            CloseableHttpClient httpClient;
            if (proxy == null) {
                if (cookieStore == null) {
                    httpClient = getHttpClient(timeOut);
                } else {
                    httpClient = getHttpClient(timeOut, cookieStore);
                }
            } else {
                if (cookieStore == null) {
                    httpClient = getHttpClient(timeOut, proxy);
                } else {
                    httpClient = getHttpClient(timeOut, cookieStore, proxy);
                }
            }
            // 发起请求
            response = httpClient.execute(httpRequest);

            int respCode = response.getStatusLine().getStatusCode();
            // 如果是重定向
            if (302 == respCode) {
                String locationUrl = response.getLastHeader("Location").getValue();
                return getResult(new HttpPost(locationUrl), timeOut);
            }
            // 正确响应
            if (200 == respCode) {
                // 获得响应实体
                return response;
            }
        } catch (ConnectionPoolTimeoutException e) {
            System.err.println("从连接池获取连接超时!!!");
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
            System.err.println("响应超时");
            e.printStackTrace();
        } catch (ConnectTimeoutException e) {
            System.err.println("请求超时");
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            System.err.println("http协议错误");
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            System.err.println("不支持的字符编码");
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            System.err.println("不支持的请求操作");
            e.printStackTrace();
        } catch (ParseException e) {
            System.err.println("解析错误");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO错误");
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Map转换成NameValuePair List集合
     *
     * @param params map
     * @return NameValuePair List集合
     */
    public static List<NameValuePair> covertParams2NVPS(Map<String, Object> params) {

        List<NameValuePair> paramList = new LinkedList<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
        }

        return paramList;

    }

    public static String getStringResult(HttpResponse httpResponse, boolean isStream) throws IOException {
        CloseableHttpResponse c = (CloseableHttpResponse) httpResponse;
        StringBuilder sb = null;
        try {
            HttpEntity entity = c.getEntity();
            sb = new StringBuilder();
            // 如果是以流的形式获取
            if (isStream) {
                BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), ENCODING));
                String len = "";
                while ((len = br.readLine()) != null) {
                    sb.append(len);
                }
            } else {
                sb.append(EntityUtils.toString(entity, ENCODING));
                if (sb.length() < 1) {
                    sb.append("-1");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } finally {
            if (null != c) {
                try {
                    c.close();
                } catch (IOException e) {
                    System.err.println("关闭响应连接出错");
                    e.printStackTrace();
                }
            }

        }
        return sb == null ? RESULT : ("".equals(sb.toString().trim()) ? "-1" : sb.toString());
    }

    public static void main(String[] args) throws Exception {

        /*JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("kkk", "djsklfj");
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("sds", "324324");*/

        System.out.println(post("https://kyfw.12306.cn/otn/login/init", null, (JSONObject) null, 6000));
        System.out.println(get("http://pub.alimama.com/items/search.json?q=%E6%89%8B%E6%9C%BA&perPageSize=5000&toPage=2", null, null, 6000));

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Proxy {
        private String ip;
        private int port;
        private String proxyType = "http";

        public HttpHost getHttpHost() {
            return new HttpHost(ip, port, proxyType);
        }
    }
}