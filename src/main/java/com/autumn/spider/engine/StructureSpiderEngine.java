package com.autumn.spider.engine;

import com.autumn.spider.step.CrawlStep;
import com.autumn.spider.step.DataCrawlStep;
import com.autumn.spider.step.StartPoint;
import com.autumn.spider.persistence.Persistence;
import com.autumn.spider.persistence.StructurePersistence;
import com.autumn.spider.resolver.ToDataResolver;
import com.autumn.spider.resolver.ToUriResolver;

import java.util.ArrayList;

public class StructureSpiderEngine<T> implements Engine {
    private final StructurePersistence<T> structurePersistence;
    private final ArrayList<String> startUrls;
    private CrawlStep last;

    public StructureSpiderEngine(Persistence<T> persistence, ToDataResolver<T> resolver, ArrayList<String> startUrls) {
        this.structurePersistence = new StructurePersistence<>(persistence, resolver);
        this.startUrls = startUrls;
    }

    @Override
    public void initStep(ToUriResolver[] resolvers) {
        CrawlStep pre = new StartPoint(startUrls);
        for (ToUriResolver resolver : resolvers) {
            pre = new DataCrawlStep(pre, resolver);
        }
        last = pre;
        structurePersistence.setLastCrawlStep(last);
    }

    @Override
    public void start() {
        new Thread(structurePersistence, "structure persistence thread").start();
        CrawlStep tmp = last;
        while (tmp != null) {
            new Thread(tmp, "crawler").start();
            tmp = tmp.getPrevious();
        }
    }
}
