package com.benjaminjbachman;

import com.benjaminjbachman.warc.Response;
import com.benjaminjbachman.warc.WarcEntry;
import com.benjaminjbachman.warc.WarcParser;
import com.google.inject.Guice;
import com.google.inject.Inject;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class NERTest {
    private final Observable<File> files;
    private final WarcParser parser;

    @Inject
    public NERTest(Observable<File> files,
                   WarcParser parser) {
        this.files = files;
        this.parser = parser;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Guice.createInjector(new NERTestModule()).getInstance(NERTest.class).run();

    }

    private void run() throws IOException, ClassNotFoundException {
        String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
        AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
        long starttime = System.currentTimeMillis();
        files.map(FileInputStream::new)
                .flatMap(parser::parseWarc)
                .map(WarcEntry::getResponse)
                .map(Response::getPayload)
                .map(Jsoup::parse)
                .map(document -> Optional.ofNullable(document.body()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Element::text)
                .map(classifier::classify)
                .flatMap(Observable::fromIterable)
                .map(list -> {
                    List<String> outputNers = new ArrayList<>();
                    StringBuilder sb = new StringBuilder();

                    for (CoreLabel item : list) {
                        if (item.get(CoreAnnotations.AnswerAnnotation.class).equals("ORGANIZATION")) {
                            sb.append(item.value()).append(" ");
                        } else {
                            if (sb.length() > 0) {
                                outputNers.add(sb.toString().trim());
                                sb = new StringBuilder();
                            }
                        }
                    }

                    if(sb.length() > 0) {
                        outputNers.add(sb.toString().trim());
                    }

                    return outputNers;
                })
                .map(list -> Observable.fromIterable(list).distinct().toList().blockingGet())
                .filter(list -> list.size() > 1)
                .filter(list -> list.size() <= 10)
                .map(list -> {
                    List<Pair<String, String>> mylist = new ArrayList<>();
                    for (int x = 0; x < list.size() - 1; x++) {
                        for (int y = x + 1; y < list.size(); y++) {
                            String a = list.get(x);
                            String b = list.get(y);
                            if (a.compareTo(b) < 0)
                                mylist.add(new Pair<>(a, b));
                            else
                                mylist.add(new Pair<>(b, a));
                        }
                    }
                    return mylist;
                })
                .flatMap(Observable::fromIterable)
                .filter(pair -> pair.getValue0().length() < 50)
                .filter(pair -> pair.getValue1().length() < 50)
                .filter(pair -> !pair.getValue1().contains(pair.getValue0()))
                .filter(pair -> !pair.getValue0().contains(pair.getValue1()))
                .groupBy(pair -> pair)
                .flatMap(pairGroupedObs -> pairGroupedObs.count()
                        .flatMapObservable(count -> Observable.just(new Triplet<>(pairGroupedObs.getKey().getValue0(),
                                pairGroupedObs.getKey().getValue1(),
                                count)))
                )
                .filter(triplet -> triplet.getValue2() >= 20)
                .toSortedList(Comparator.comparingLong(Triplet::getValue2))
                .flatMapObservable(Observable::fromIterable)
                .doOnError(Throwable::printStackTrace)
                .subscribe(label -> {
                    System.out.println(label.toString());
                });
        System.out.println("Time taken: " + (System.currentTimeMillis() - starttime) / 1000 / 60 + " minutes");
    }
}
