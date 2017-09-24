package com.benjaminjbachman;

import com.benjaminjbachman.warc.WarcParser;
import com.benjaminjbachman.warc.WarcParserImpl;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import io.reactivex.Observable;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NERTestModule extends PrivateModule {
    protected void configure() {
        bind(WarcParser.class).to(WarcParserImpl.class);
        bind(NERTest.class);
        expose(NERTest.class);
    }

    @Provides
    Observable<File> getFile() {
        return Observable.just(new File("data/CC-MAIN-20151124205404-00000-ip-10-71-132-137.ec2.internal.warc"));
    }

    @Provides
    @Named("warc.parsedPropeties")
    Set<String> getWarcProperties() {
        return new HashSet<String>(Arrays.<String>asList(
                "WARC-Truncated",
                "Expires",
                "Vary",
                "Content-Encoding",
                "Cache-Control",
                "Connection",
                "Server",
                "Date",
                "fetchTimeMs",
                "Accept-Encoding",
                "Accept-Language",
                "User-Agent",
                "WARC-Block-Digest",
                "WARC-Payload-Digest",
                "Host",
                "Accept",
                "Set-Cookie",
                "WARC-Concurrent-To",
                "WARC-IP-Address",
                "WARC-Target-URI",
                "WARC-Warcinfo-ID",
                "WARC-Date",
                "WARC-Record-ID",
                "WARC-Type",
                "Content-Length",
                "Content-Type",
                "Last-Modified"
        ));
    }
}
