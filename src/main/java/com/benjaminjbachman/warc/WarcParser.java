package com.benjaminjbachman.warc;

import io.reactivex.Observable;

import java.io.InputStream;

public interface WarcParser {
    Observable<WarcEntry> parseWarc(InputStream is);
}
