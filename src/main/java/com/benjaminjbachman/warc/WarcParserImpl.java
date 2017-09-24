package com.benjaminjbachman.warc;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.reactivex.Observable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WarcParserImpl implements WarcParser {
    private final Set<String> warcProperties;
    private final Gson gson;
    private final Pattern property = Pattern.compile("(.*): (.*)");

    @Inject
    public WarcParserImpl(@Named("warc.parsedPropeties") Set<String> WarcProperties,
                          Gson gson) {
        warcProperties = WarcProperties;
        this.gson = gson;
    }

    @Override
    public Observable<WarcEntry> parseWarc(InputStream is) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

        AtomicInteger atomicInteger = new AtomicInteger();
        return Observable.create(observableEmitter -> {
            String line;
            line = bufferedReader.readLine();

            Map<String, Map<String, String>> bundledObject = new HashMap<>();

            while (line != null) {
                String objectline;
                StringBuilder stringBuilder = new StringBuilder();
                Map<String, String> warcobjectproperties = new HashMap<>();
                boolean buildingPayload = false;

                while ((objectline = bufferedReader.readLine()) != null && !objectline.startsWith("WARC/1.0")) {
                    Matcher matcher = property.matcher(objectline);
                    if (matcher.matches() && warcProperties.contains(matcher.group(1))) {
                        buildingPayload = false;
                        warcobjectproperties.put(matcher.group(1).replaceAll("-", "_"), matcher.group(2));
                    } else {
                        if (!buildingPayload) {
                            if (warcobjectproperties.get("payload") == null || warcobjectproperties.get("payload").length() < stringBuilder.length()) {
                                warcobjectproperties.put("payload", stringBuilder.toString());
                                stringBuilder = new StringBuilder();
                            }
                        }
                        stringBuilder.append(objectline).append(" ");
                        buildingPayload = true;
                    }
                }

                if (warcobjectproperties.get("payload") == null || warcobjectproperties.get("payload").length() < stringBuilder.length()) {
                    warcobjectproperties.put("payload", stringBuilder.toString());
                }

                String type = warcobjectproperties.get("WARC_Type");

                if (bundledObject.containsKey(type)) {
                    WarcEntry warcEntry = gson.fromJson(gson.toJson(bundledObject), WarcEntry.class);
                    observableEmitter.onNext(warcEntry);
                    if(atomicInteger.incrementAndGet() % 100 == 0)
                        System.out.println("Websites checked: " + atomicInteger.get());
                    bundledObject = new HashMap<>();
                }

                bundledObject.put(type, warcobjectproperties);
                line = objectline;
            }

            WarcEntry warcEntry = gson.fromJson(gson.toJson(bundledObject), WarcEntry.class);
            observableEmitter.onNext(warcEntry);
            observableEmitter.onComplete();
        });

    }
}
