package com.autumn.spider.persistence;

import com.autumn.spider.Utils;
import com.autumn.spider.step.CrawlStep;
import com.autumn.spider.resolver.ToDataResolver;

import java.net.http.HttpResponse;

public final class StructurePersistence<T> implements Runnable {
    private final Persistence<T> persistence;
    private final ToDataResolver<T> resolver;
    private CrawlStep last;

    public StructurePersistence(Persistence<T> persistence, ToDataResolver<T> resolver) {
        this.persistence = persistence;
        this.resolver = resolver;
    }

    public void setLastCrawlStep(CrawlStep last) {
        this.last = last;
    }

    @Override
    public void run() {
        while (last.notComplete() || last.hasData()) {
            try {
                HttpResponse<String> response = last.getResponse();
                if (response == null) continue;
                T item = resolver.resolver(response);
                persistence.persistence(item);
            } catch (Exception e) {
                e.printStackTrace();
                Utils.executor.shutdownNow();
                try {
                    persistence.closeResources();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                System.exit(130);
            }
        }
        Utils.executor.shutdown();
        try {
            persistence.closeResources();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
