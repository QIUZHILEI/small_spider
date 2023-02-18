package com.autumn.spider;

import com.autumn.spider.engine.Engine;
import com.autumn.spider.engine.StreamSpiderEngine;
import com.autumn.spider.engine.StructureSpiderEngine;
import com.autumn.spider.persistence.Download;
import com.autumn.spider.persistence.Persistence;
import com.autumn.spider.resolver.ToDataResolver;
import com.autumn.spider.resolver.ToUriResolver;

import java.io.InputStream;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Utils {
    public final static String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.54";
    private static String COOKIE = "";

    private static long CRAWL_INTERVAL = 300;
    private static long DOWNLOAD_INTERVAL = 500;

    private static long ALIVE_TIME = 10;
    private static int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static int MAX_POOL_SIZE = CORE_POOL_SIZE * 5;
    public final static ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, ALIVE_TIME,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());


    private static HttpClient client;
    private static ProxySelector selector;

    private static Duration connectionTimeOut =Duration.ofMillis(5000);
    private static long responseTimeOut =10_000;

    public static void setResponseTimeOut(long responseTimeOut) {
        Utils.responseTimeOut = responseTimeOut;
    }

    public static long getResponseTimeOut() {
        return responseTimeOut;
    }

    public static void setConnectionTimeOut(long timeout) {
        Utils.connectionTimeOut = Duration.ofMillis(timeout);
    }

    public static void setCookie(String cookie) {
        Utils.COOKIE = cookie;
    }

    public static void setCrawlInterval(long crawlInterval) {
        CRAWL_INTERVAL = crawlInterval;
    }

    public static void setDownloadInterval(long downloadInterval) {
        DOWNLOAD_INTERVAL = downloadInterval;
    }

    public static void setAliveTime(long aliveTime) {
        ALIVE_TIME = aliveTime;
    }

    public static void setCorePoolSize(int corePoolSize) {
        CORE_POOL_SIZE = corePoolSize;
    }

    public static void setMaxPoolSize(int maxPoolSize) {
        MAX_POOL_SIZE = maxPoolSize;
    }

    public static void setSelector(ProxySelector selector) {
        Utils.selector = selector;
    }

    private static void initClient() {
        HttpClient.Builder builder = HttpClient.newBuilder().executor(executor).connectTimeout(connectionTimeOut);
        if (selector != null) {
            builder.proxy(selector);
        }
        client = builder.build();
    }

    private static HttpRequest buildRequest(String uri) {
        HttpRequest.Builder builder = HttpRequest
                .newBuilder(URI.create(uri))
                .GET()
                .header("User-Agent", Utils.UA);
        if (!"".equals(Utils.COOKIE)) {
            builder.header("cookie", Utils.COOKIE);
        }
        return builder.build();
    }

    public static CompletableFuture<HttpResponse<String>> sendWithStringResponse(String uri) throws InterruptedException {
        HttpRequest request = buildRequest(uri);
        sleep(CRAWL_INTERVAL);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static CompletableFuture<HttpResponse<InputStream>> sendWithStreamResponse(String uri) throws InterruptedException {
        HttpRequest request = buildRequest(uri);
        sleep(DOWNLOAD_INTERVAL);
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
    }

    private static void sleep(long interval) throws InterruptedException {
        long remains=interval;
        while (remains > 0) {
            long start = System.currentTimeMillis();
            TimeUnit.MILLISECONDS.sleep(remains);
            remains= remains-(System.currentTimeMillis() - start);
        }
    }

    public static <T> void startWithStructureDataSpider(ArrayList<String> startUrls, ToUriResolver[] toUriResolvers, Persistence<T> persistence, ToDataResolver<T> toDataResolver) {
        initClient();
        Engine engine = new StructureSpiderEngine<>(persistence, toDataResolver, startUrls);
        engine.initStep(toUriResolvers);
        engine.start();
    }

    public static void startWithStreamDataSpider(ArrayList<String> startUrls, ToUriResolver[] toUriResolvers, Download download) {
        initClient();
        Engine engine = new StreamSpiderEngine(startUrls, download);
        engine.initStep(toUriResolvers);
        engine.start();
    }

}
