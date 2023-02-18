package com.autumn.spider.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

public interface Download {
    void download(HttpResponse<InputStream> response, String file) throws IOException;

    String[] resolveFileName(HttpResponse<String> response);

    String[] resolveResources(HttpResponse<String> response);
}
