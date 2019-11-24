package io.vertx.microservice.rx;

import rx.Observable;


public class OperatorExample {

    public static void main(String[] args) {
        
        // Create a stream of integer [0..20]
        Observable<Integer> observable = Observable.range(0, 21);
        Observable<Integer> anotherObservable = Observable.range(0, 21);

        observable
                .map(i -> i + 1)
                .zipWith(anotherObservable, (a, b) -> a + b)
                // 0 + 1 = 1
                // 1 + 0 = 1

                // 1 + 1 = 2
                // 2 + 1 = 3

                // 2 + 1 = 3
                // 3 + 2 = 5

                // 3 + 1 = 4
                // 4 + 3 = 7

                // 4 + 1 = 5
                // 5 + 4 = 9

                // 5 + 1 = 6
                // 6 + 5 = 11
                .subscribe(
                    System.out::println,
                    Throwable::printStackTrace,
                    () -> {
                        // Called when we reached the end of the stream
                        System.out.println("No more data");
                    }
                );
    }
}