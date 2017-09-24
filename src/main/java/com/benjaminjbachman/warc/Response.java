package com.benjaminjbachman.warc;

import lombok.Data;

@Data
public class Response {
    private final String payload;
    private final String WARC_Truncated;
    private final String Expires;
    private final String Vary;
    private final String Content_Encoding;
    private final String Cache_Control;
    private final String Connection;
    private final String Server;
    private final String Date;
    private final String fetchTimeMs;
    private final String Accept_Encoding;
    private final String Accept_Language;
    private final String User_Agent;
    private final String WARC_Block_Digest;
    private final String WARC_Payload_Digest;
    private final String Host;
    private final String Accept;
    private final String Set_Cookie;
    private final String WARC_Concurrent_To;
    private final String WARC_IP_Address;
    private final String WARC_Target_URI;
    private final String WARC_Warcinfo_ID;
    private final String WARC_Date;
    private final String WARC_Record_ID;
    private final String WARC_Type;
    private final String Content_Length;
    private final String Content_Type;
    private final String Last_Modified;
}
