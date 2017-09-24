package com.benjaminjbachman.warc;

import lombok.Data;

@Data
public class WarcEntry {
    Request request;
    Response response;
    Metadata metadata;
}
