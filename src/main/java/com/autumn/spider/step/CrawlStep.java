package com.autumn.spider.step;

import com.autumn.spider.Utils;
import com.autumn.spider.resolver.ToUriResolver;

import java.net.http.HttpResponse;
import java.util.concurrent.*;

public abstract class CrawlStep implements Runnable {
    protected final CrawlStep previous;
    protected final ConcurrentLinkedQueue<CompletableFuture<HttpResponse<String>>> data;
    protected final ToUriResolver resolver;
    protected volatile boolean complete;

    public CrawlStep(CrawlStep previous, ToUriResolver resolver) {
        this.resolver = resolver;
        this.data = new ConcurrentLinkedQueue<>();
        this.previous = previous;
    }

    public final CrawlStep getPrevious() {
        return previous;
    }

    //获取当前Worker的HttpResponse,用于给后继节点异步获取当前Worker发送请求的响应
    public HttpResponse<String> getResponse() {
        HttpResponse<String> response = null;
        CompletableFuture<HttpResponse<String>> future = data.poll();
        if (future != null) {
            try {
                response = future.get(Utils.getResponseTimeOut(), TimeUnit.MILLISECONDS);
                if (response.statusCode() != 200) {
                    System.out.println(response.uri().toString() + "\t failed with " + response.statusCode());
                    response = null;
                }
            } catch (TimeoutException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.out.println("response has some error!");
                return null;
            }
        }
        return response;
    }

    //判断当前data是否有数据
    public boolean hasData() {
        return !data.isEmpty();
    }

    //判断当前步骤是否完成
    public boolean notComplete() {
        return !complete;
    }

}
