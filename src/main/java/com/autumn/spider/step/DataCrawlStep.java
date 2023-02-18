package com.autumn.spider.step;

import com.autumn.spider.Utils;
import com.autumn.spider.resolver.ToUriResolver;

import java.net.http.HttpResponse;
import java.util.ArrayList;

public final class DataCrawlStep extends CrawlStep {

    private final ToUriResolver resolver;

    public DataCrawlStep(CrawlStep previous, ToUriResolver resolver) {
        super(previous, resolver);
        this.resolver = resolver;
    }

    @Override
    public void run() {
        while (!previous.complete || previous.hasData()) {
            HttpResponse<String> response = previous.getResponse();
            if (response == null) continue;
            ArrayList<String> uris = resolver.resolver(response);
            for (String uri : uris) {
                try {
                    data.add(Utils.sendWithStringResponse(uri));
                } catch (InterruptedException e) {
                    Utils.executor.shutdownNow();
                    System.out.println("Network may be has some problem!");
                    e.printStackTrace();
                }
            }
        }
        complete = true;
    }
}
