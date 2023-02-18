package com.autumn.spider.engine;

import com.autumn.spider.step.CrawlStep;
import com.autumn.spider.step.DataCrawlStep;
import com.autumn.spider.step.StartPoint;
import com.autumn.spider.persistence.Download;
import com.autumn.spider.persistence.StreamPersistence;
import com.autumn.spider.resolver.ToUriResolver;

import java.util.ArrayList;

public class StreamSpiderEngine implements Engine {
    private final StreamPersistence streamPersistence;
    private final ArrayList<String> startUrls;
    private CrawlStep last;

    public StreamSpiderEngine(ArrayList<String> startUrls, Download download) {
        this.streamPersistence = new StreamPersistence(download);
        this.startUrls = startUrls;
    }

    @Override
    public void initStep(ToUriResolver[] resolvers) {
        CrawlStep pre = new StartPoint(startUrls);
        for (ToUriResolver resolver : resolvers) {
            pre = new DataCrawlStep(pre, resolver);
        }
        last = pre;
        streamPersistence.setLastCrawlStep(last);
    }

    @Override
    public void start() {
        new Thread(streamPersistence, "stream persistence thread").start();
        CrawlStep tmp = last;
        while (tmp != null) {
            new Thread(tmp, "crawler").start();
            tmp = tmp.getPrevious();
        }
    }
}
