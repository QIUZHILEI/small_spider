package com.autumn.spider.step;

import com.autumn.spider.Utils;

import java.util.ArrayList;

public final class StartPoint extends CrawlStep {
    private final ArrayList<String> startUrls;

    public StartPoint(ArrayList<String> startUrls) {
        super(null, null);
        this.startUrls = startUrls;
    }

    @Override
    public void run() {
        for (String url : startUrls) {
            try {
                data.add(Utils.sendWithStringResponse(url));
            } catch (InterruptedException e) {
                System.out.println("Illegal interrupt!");
                Utils.executor.shutdownNow();
                throw new RuntimeException(e);
            }
        }
        complete = true;
    }

}
