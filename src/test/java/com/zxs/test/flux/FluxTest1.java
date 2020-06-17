package com.zxs.test.flux;

import org.reactivestreams.Subscriber;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

public class FluxTest1 {
    public static void main(String[] args) {
        Mono<Object> objectMono = MonoProcessor.create(a -> a.currentContext());

//        Mono.just(5).subscribe(objectMonoProcessor);
    }
}
