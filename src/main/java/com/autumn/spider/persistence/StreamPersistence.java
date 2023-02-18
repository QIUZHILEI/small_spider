package com.autumn.spider.persistence;

import com.autumn.spider.Utils;
import com.autumn.spider.step.CrawlStep;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public final class StreamPersistence implements Runnable {
    private final Download download;
    private CrawlStep last;

    public StreamPersistence(Download download) {
        this.download = download;
    }

    public void setLastCrawlStep(CrawlStep last) {
        this.last = last;
    }

    @Override
    public void run() {
        while (last.notComplete() || last.hasData()) {
            HttpResponse<String> response = last.getResponse();
            if (response == null) continue;
            String[] urls = download.resolveResources(response);
            String[] names = download.resolveFileName(response);
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                String url = urls[i];
                try {
                    final CompletableFuture<HttpResponse<InputStream>> future = Utils.sendWithStreamResponse(url);
                    Utils.executor.execute(() -> {
                        try {
                            HttpResponse<InputStream> resp = future.get(Utils.getResponseTimeOut(), TimeUnit.MILLISECONDS);
                            download.download(resp, name);
                        } catch (InterruptedException | ExecutionException | TimeoutException e) {
                            e.printStackTrace();
                            System.out.println(url + " response has some error!");
                        } catch (IOException e) {
                            System.out.println(url + "\t" + name + " download failed!");
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    System.out.println("Illegal Interrupt!");
                    Utils.executor.shutdownNow();
                    throw new RuntimeException(e);
                }
            }
        }
        System.out.println("Has submit all download resource.");
        while (Utils.executor.getActiveCount()!=0) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Utils.executor.shutdownNow();
        System.out.println("persistence thread terminate!");
    }

}
