# Building Reactive Microservice Systems

This project is based mainly on the references below.

    ESCOFFIER, C. Building Reactive Microservices in Java Asynchronous and Event-Based Application Design. First Edition. California: O’Reilly Media, Inc., 2017.

    RedHat Developer, accessed 1 November 2019, <https://developers.redhat.com/promotions/building-reactive-microservices-in-java>

    Kubernetes Hands-On - Deploy Microservices to the AWS Cloud 2018, Udemy, accessed 1 November 2019, <https://www.udemy.com/course/kubernetes-microservices>

    <https://github.com/hazelcast/hazelcast-code-samples/>

    <https://vertx.io/docs/vertx-hazelcast>

    <http://escoffier.me/vertx-kubernetes/>



## Understanding Reactive Microservices and Vert.x

Microservices are not really a new thing.

They arose from research conducted in the 1970s and have come into the spotlight recently because microservices are a way to move faster, to deliver value more easily, and to improve agility.

However, microservices have roots in actor-based systems, service design, dynamic and autonomic systems, domain-driven design, and distributed systems.

The fine-grained modular design of microservices inevitably leads developers to create distributed systems.

As I'm sure you have noticed, distributed systems are hard.

They fail, they are slow, they are bound by the CAP and FLP theorems. 

In other words, they are very complicated to build and maintain.

That's where reactive comes in.


## Reactive

But what is reactive?

Reactive is an overloaded term these days.

The Oxford dictionary defines reactive as "showing a response to a stimulus".

So, reactive software reacts and adapts its behavior based on the stimuli it receives.

However, the responsiveness and adaptability promoted by this definition are programming challenges because the flow of computation isn't controlled by the programmer but by the stimuli.

We are going to see how Vert.x helps us to be reactive by combining:

* Reactive programming  - A development model focusing on the observation of data streams, reacting on changes, and propagating them

* Reactive system - Ah architecture style used to build responsive and robust distributed systems based on asynchronous message-passing

A reactive microservice is the building block of reactive microservice systems. Howerver, due to their asynchronous aspect, the implementation of these microservices is challenging. Reactive programming reduces this complexity. How? Let's answer this question right now.

## Reactive Programming

Reactive programming is a development model oriented around data flows and the propagation of data. In reactive programming, the stimuli are the data transiting in the flow, which are called streams.

There are many ways to implement a reactive programming model. We are going to to use Reactive Extensions (https://reactivex.io/) where streams are called observables, and consumers subscribe to these observables and react to the values.

To make these concepts less abstract, let's look at an example using RxJava, a library implementing the Reactive Extensions in Java. These examples are located in the directory reactive-programming in the code directory.


        observable.subscribe(
            data -> {   // onNext
                System.out.println(data);
            },
            error -> {  // onError
                error.printStackTrace();
            },
            () -> {     // onComplete
                System.out.println("No more data");
            }
        );

In this snippet, the code is observing (subscribe) an Observable and is notified when values transit in the flow. The subscriber can receive three types of events. onNext is called when there is a new value, while onError is called when an error is emitted in the stream or a stage throws an Exception. The onComplete callback is invoked when the end of the stream is reached, which would not occur for unbounded streams.

RxJava includes a set of operators to produce, transform, and coordinate Observables, such as map to transform a value into another value, or flatMap to produce an Observable or chain another asynchronous action:

            // sensor is an unbound observable publishing values
            sensor
                // Groups values 10 by 10, and produces an observable 
                // with these values
                .window(10)
                // Compute the average on each group
                .flatMap(MathObservable::averageInteger)
                // Produce a json representation of the average
                .map(average -> "{'average': " + average + "}")
                .subscribe(
                    data -> {
                        System.out.println(data);
                    },
                    error -> {
                        error.printStackTrace();
                    }
                );

RxJava defines different types of streams as follows:

* Observables are bounded or unbounded streams expected to contain a sequence of values

* Singles are streams with a single value, generally the deferred result of an operation, similar to futures or promises

* Completables are streams without value but with an indication of whether an operation completed or failed


## RxJava

What can we do with RxJava? For instance, we can describe sequences of asynchronous actions and orchestrate them. 

Let's imagine you want to download a document, process it, and upload it.

The download and upload operations are asynchronous.

To develope this sequence, you use something like:

            // Asynchronous task downloading a document
            Future<String> downloadTask = download();

            // Create a single completed when the document is downloaded.
            Single.from(downloadTask)

                // Process the content
                .map(content -> process(content))

                // Upload the document, this asynchronous operation just
                // indicates its successful completion or failure
                .flatMapCompletable(payload -> upload(payload))

                .subscribe(
                    () -> System.out.println("Document downloaded, updated and uploaded"),
                    t -> t.printStackTrace();
                );

You can also orchestrate asynchronous tasks. For example, to combine the results of two asynchronous operations, you use the zip operator combining values of different streams:

        // Download two documents
        Single<String> downloadTask1 = downloadFirstDocument();
        Single<String> downloadTask2 = downloadSecondDocument();

        // When both documents are downloaded, combine them
        Single.zip(downloadTask1, downloadTask2, (doc1, doc2) -> doc1 + "\n" + doc2)
            .subscribe(
                (doc) -> System.out.println("Document combined: " + doc),
                t -> t.printStackTrace()
            );

The use of these operators gives you superpowers: you can coordinate asynchronous tasks and data flow in a declarative and elegant way. How is this related to reactive microservices?

To answer this question, let's have a look at reactive systems.


### Reactive Streams

You may have heard of reactive streams (https://www.reactivestreams.org). Reactive streams is an initiative to provide a standard for asynchronous stream processing with back-pressure.

It provides a minimal set of interfaces and protocols that describe the operations and entities to achieve the asynchronous stream of data with nonblocking back-pressure. It does not define operators manipulating the streams, and is mainly used as an interoperability layer. 

## Reactive Systems

While reactive programming is a development model, reactive systems is an architectural style used to build distributed systems.

It's a set of principles used to achieve responsiveness and build systems that respond to requests in a timely fashion even with failures or under load.

To build such a system, reactive systems embrace a message-driven approach.

All the components interact using messages sent and received asynchronously.

To decouple senders and receivers, components send messages to virtual addresses.

They also register to the virtual addresses to receive messages.

An address is a destination identifier such as an opaque string or a URL. Several receivers can be registered on the same address - the delivery semantic depends on the underlying technology.

Senders do not block and wait for a response.

The sender may receive a response later, but in the meantime, he can receive and send other messages.

This asynchronous aspect is particularly important and impacts how your application is developed.

Using asynchronous message-passing interactions provides reactive systems with two critical properties:

        * Elasticity - The ability to scale horizontally (scale out/in)
        * Resilience - The ability to handle failure and recover


Elasticity comes from the decoupling provided by message interactions.

Messages sent to an address can be consumed by a set of consumers using a load-balancing strategy. 

When a reactive system faces a spike in load, it can spawn new instances of consumers and dispose of them aferward.

This resilience characteristic is provided by the ability to handle failure without blocking as well as the ability to replicate components.

First, message interactions allow components to deal with failure locally.

Thanks to the asynchronous aspect, components do not actively wait for responses, so a failure happening in one component would not impact other components. Replication is also a key ability to handle resilience. When one node-processing message fails, the message can be processed by another node registered on the same address.

Thanks to these two characteristics, the system becomes responsive.

It can adapt to higher or lower load and continue to serve requests in the face of high loads or failures.

This set of principles is primordial when building microservice systems that are highly distributed, and when dealing with services beyond the control of the caller.

It is necessary to run several instances of your services to balance the load and handle failures without breaking the availibility.


## Reactive Microservices

When building a microservice (and thus distributed) system, each service can change, evolve, fail, exhibit slowness, or be withdrawn at any time.

Such issues must not impact the behavior of the whole system.

Your system must embrace changes and be able to handle failures. You may run in a degraded mode, but your system should still be able to handle the requests.

To ensure such behavior, reactive microservice systems are comprised of reactive microservices.

These microservices have four characteristics:

        * Autonomy

        * Asynchronisity

        * Resilience

        * Elasticity

Reactive microservices are autonomous. They can adapt to the availability or unavailability of the services surrounding them. However, autonomous comes paired with isolation. Reactive microservices can handle failure locally, act independently, and cooperate with others as needed. A reactive microservice uses asynchronous message-passing to interact with its peers. It also receives messages and has the ability to produce responses to these messages.

Thanks to the asynchronous message-passing, reactive microservices can face failures and adapt their behavior accordingly. Failures should not be propagated but handled close to the root cause.

When a microservice blows up, the consumer microservice must handle the failure and not propagate it. This isolation principle is a key characteristic to prevent failures from bubbling up and breaking the whole system. Resilience is not only about managing failure, it's also about self-healing. A reactive microservice should implement recovery or compensation strategies when failures occur.

Finally, a reactive microservice must be elastic, so the system can adapt to the number of instances to manage the load. This implies a set of constraints such as avoiding in-memory state, sharing state between instances if required, or being able to route messages to the same instances for stateful services.


## Asynchronous Development Model

All applications built with Vert.x are asynchronous. Vert.x applications are event-driven and non-blocking. Your application is notified when something interesting happens. Let's look at a concrete example. 

Vert.x provides an easy way to create an Http server. This Http server is notified every time an Http request is received:

            vertx.createHttpServer()
                .requestHandler(request -> {
                        // This handler will be called every time an Http
                        // request is received at the server
                        request.response().end("hello vert.x");
                })
                .listen(8080);

In this example, we set a requestHandler to receive the Http request (event) and send hello vert.x back (reaction). A Handler is a function called when an event occurs. In our example, the code of the handler is executed with each incoming request. Notice that a Handler does not return a resutl. However, a Handler can provide a result. How this result is provided depends on the type of interaction. In the last snippet, it just writes the result into the Http response. The Handler is chained to a listen request on the socket. Invoking this Http endpoint produces a simple Http response:

    HTTP/1.1 200 OK
    Content-Length: 12

    hello vert.x

With very few exceptions, none of the APIs in Vert.x block the calling thread.

If a result can be provided immediately, it will be returned; otherwise, a Handler is used to receive events at a later time. The Handler is notified when an event is ready to be processed or when the result of an asynchronous operation has been computed.

In traditional imperative programming, you would write something like:

int res = compute(1,2);

In this code, you wait for the result of the method. When switching to an asynchronous nonblocking development model, you pass a Handler invoked when the result is ready:

    compute(1, 2, res -> {
        // Called with the result
    });

In the last snippet, compute does not resurn a result anymore, so you don't wait until this result is computed and returned. You pass a Handler that is called when the result is ready.

Thanks to this nonblocking development model, you can handle a highly concurrent workload using a small number of threads. In most cases, Vert.x calls your handlers using a thread called an event loop. It consumes a queue of events and dispatches each event to the interested Handlers.

The threading model proposed by the event loop has a huge benefit: it simplifies concurrency. As there is only one thread, you are always called by the same thread and never concurrently. However, it also has a very important rule that you must obey:

            Don't block the event loop.
                - Vert.x golden rule

Because nothing blocks, an event loop can deliver a huge number of events in a short amount of time. This is called the reactor pattern.

Let's imagine, for a moment, that you break the rule. In the previous code snippet, the request handler is always called from the same event loop. So if the Http request processing blocks instead of replying to the user immediately, the other requests would not be handled in a timely fashion and would be queued, waiting for the thread to be released.

You would loose the scalability and efficiency benefit of Vert.x. So what can be blocking? The first obvious example is JDBC database access. They are blocking by nature. Long computations are also blocking. For example, a code calculating Pi to the 200,000th decimal point is definitely blocking. Don't worry - Vert.x also provides constructs to deal with blocking code.

In a standard reactor implementation, there is a single event loop thread that runs around a loop delivering all events to all handlers as they arrive. The issue with a single thread is simple: it can only run on a single CPU core at one time. Vert.x works differently here. Instead of a single event loop, each Vert.x insance maintains several event loops, which is called a multireactor pattern.

The events are disptached by the different event loops. However, once a Handler is executed by an event loop, it will always be invoked by this event loop, enforcing the concurrency benefits of the reactor pattern.

If you have several event loops, it can balance the load on different CPU cores. How does that work with our Http example? Vert.x registers the socket listener once and dispatches the requests to the different event loops.

## Verticles - the Building Blocks

Vert.x gives you a lot of freedom in how you can shape your application and code.

But it also provides bricks to easily start writing Vert.x applications and comes with a simple, scalable, actor-like deployment and concurrency model out of the box.

Verticles are chuncks of code that get deployed and run by Vert.x. An application, such as a microservice, would typically be comprised of many verticle instances running in the same Vert.x instance at the same time.

A verticle typically creates servers or clients, registers a set of Handlers, and encapsulates a part of the business logic of the system.

Regular verticles are executed on the Vert.x event loop and can never block.

Vert.x ensures that each verticle is always executed by the same thread and never concurrently, hence avoiding synchronization constructs.

In Java, a verticle class is a class extending the AbstractVerticle class.


            import io.vertx.core.AbstractVerticle;

            public class MyVerticle extends AbstractVerticle {

                @Override
                public void start() throws Exception {
                        // Executed when the verticle is deployed
                }

                @Override
                public void stop() throws Exception {
                        // Executed when the verticle is undeployed
                }
            }

Verticles have access to the vertx member (provided by the AbstractVerticle class) to create servers and clients and to interact with the other verticles.

Verticles can also deploy other verticles, configure them, and set the number of instances to create. The instances are associated with the different event loops (implementing the multireactor pattern), and Vert.x balances the load among these instances.

## From Callbacks to Observables

As seen in the previous sections, the Vert.x development model uses callbacks.

When orchestrating several asynchronous actions, this callback-based development model tends to produce complex code. For example, let's look at how we would retrieve data from a database. First, we need a connection to the database, then we send a query to the database, process the results, and release the connection. All these opeartions are asynchronous. Using callbacks, you would write the following code using the Vert.x Jdbc client.

            client.getConnection(conn -> {
                if (conn.failed()) {
                    /* failure handling */
                } else {
                    SQLConnection connection = conn.result();
                    connection.query("SELECT * from PRODUCTS", rs -> {
                        if (rs.failed()) {
                            /* failure handling */
                        } else {
                            List<JsonArray> lines = rs.result().getResults();
                            for (JsonArray l : lines) {
                                System.out.println(new Product(l));
                            }
                            connection.close(done -> {
                                if (done.failed()) {
                                    /* failure handling */
                                }
                            });
                        }
                    });
                }
            });

While still manageable, the example shows that callbacks can quickly lead to unreadable code. You can also use Vert.x Futures to handle asynchronous actions. Unlike Java Futures, Vert.x Futures are nonblocking. Futures provide higher-level composition operators to build sequences of actions or to execute actions in parallel.

Typically, as demonstrated in the next snippet, we compose futures to build the sequence of asynchronous actions:


            Future<SQLConnection> future = getConnection();
            future 
                .compose(conn -> {
                    connection.set(conn);
                    // Return a future of ResultSet
                    return selectProduct(conn);
                })
                // Return a collection of products by mapping
                // each row to a Product
                .map(result -> toProducts(result.getResults()))
                .setHandler(ar -> {
                    if (ar.failed()) {
                            // failure handling
                    } else {
                        ar.result().forEach(System.out::println);
                    }
                    connection.get()close(done -> {
                        if (done.failed()) {
                            /* failure handling */
                        }
                    });
                })

However, while Futures make the code a bit more declarative, we are retrieving all the rows in one batch and processing them. This result can be huge and take a lot of time to be retrieved. At the same time, you don't need the whole result to start processing it. We can process each row one by one as soon as you have them. Fortunately, Vert.x provides an answer to this development model challenge and offers you a way to implement reactive microservices using a reactive programming development model. Vert.x provides RxJava APIs to:

        * Combine and coordinate asynchronous tasks
        * React to incoming messages as a stream of input

Let's rewrite the previous code using the RxJava APIs:


            // We retrieve a connection and cache it,
            // so we can retrieve the value later
            Single<SQLConnection> connection = client.rxGetConnection();

            connection
                .flatMapObservable(conn -> 
                    conn
                        // Execute the query
                        .rxQueryStream("SELECT * from PRODUCTS")
                        // Publish the rows one by one in a new Observable
                        .flatMapObservable(SQLRowStream::toObservable)
                        // Don't forget to close the connection
                        .doAfterTerminate(conn::close)
                )
                // Map every row to a Product
                .map(Product::new)
                // Display the result one by one
                .subscribe(System.out::println);

In addition to improving readability, reactive programming allows you to subscribe to a stream of results and process items as soon as they are available. With Vert.x you can choose the development model you prefer. 



## Eclipse Vert.x

This execution model impacts how you write your code, instead of the traditional model of blocking code, your code is going to be asynchronous and non-blocking.

As an example, if we wanted to retrieve a resource from a URL, we would do something like this:

            URL site = new URL("https://vertx.io");
            BufferedReader in = new BufferedReader(new InputStreamReader(site.openStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
            in.close();

But with Vert.x, we are more likely to do:

            vertx.createHttpClient().getNow(80, "vertx.io", "", response -> {
                response.bodyHandler(System.out::println);
            });

The main differences between these 2 code samples are:

    * The first one is synchronous and potentially blocking: the instructions are executed in order, and may block the thread for a long time (because the web site may be slower or whatever).

    * The Vert.x is asynchronous and non-blocking: the thread (event loop) is released while the connection with the Http server is established and is free to do something else. When the response has been received, the same event loop calls the callback. Most of the Vert.x components are single-threaded (accessed by a single thread), so no concurrency burden anymore. By the way, with Vert.x, even the DNS resolution is asynchronous and non-blocking (while Java DNS resolution is blocking).

Finally, Vert.x applications run "on the JVM", the Java Virtual Machine (8+). This means Vert.x applications can be developed using any language that runs on the JVM. Including Java (of course), Groovy, Ceylon, Ruby, JavaScript, Kotlin and Scala. We can even mix and match any combination of all these languages. The polyglot nature of a Vert.x application allows you use the most appropriate language for the task.

Vert.x lets you implement distributed applications either by using the built-in TCP and Http server and client, or by using the Vert.x event bus, a lightway mechanism to send and receive messages. With the event bus, you send messages to addresses. It supports three modes of distributions:

    1. point to point: the message is sent to a single consumer listening on the address

    2. publish/subscribe: the message is received by all the consumers listening on the address

    3. request/reply: the message is sent to a single consumer and lets it reply to the message by sending another message to the initial sender


## From callbacks to Reactive Programming


Vert.x uses a simple callback based asynchrony and its Future object is a helper tool useful for callback coordination (more about Future will be covered in the compulsive trader chapter).

RxJava implements the Reactive Extensions for the JVM and is a library for composing asynchronous and event-based programs.

With RxJava, you model your code around data flow (called Flowable or Observable).

These data flow are pipes in which data transits. There are several types of pipes:

    1. Flowable and Observable can represent finite or infinite streams. Flowable support back-pressure

    2. Single are streams with a single element.

    3. Maybe are streams with either 0 or 1 element.

    4. Finally a Completable represents a stream with no elements, i.e., it can only complete without a value or fail.

To use such reactive type, a subscription operation is necessary.


            // Subscribe to the stream
            stream.subscribe(
                item -> {
                    // Received a String item
                },
                error -> {
                    // Error termination => no more items
                },
                () -> {
                    // Normal termination => no more items
                }
            );


Singles are simpler to work with as they hold exactly one element, they have some similarities with future/promises although they have noticeable differences:

    * a future/promise is the result of an asynchronous operation, e.g., you start a server and you get a promise of the server bind result

    * a single result usually has a side effect at subscription time, e.g., you subscribe to the single, as side effect it starts the server and the single notifies you of the bind result


        Single<String> single = getStringSingle();

        // Subscribe to the single
        single.subscribe(
            item -> {
                // Completion with the string item
            },
            error -> {
                // Completion with an error
            }
        );


## Composition and transformation

RxJava provides a very useful set of operators for composing and transforming asynchronous flows.

We will use the main ones in this lab: map, flatMap and zip.

The map operator transforms synchronously the result of an operation.

            // Transform the stream of strings into a stream of Buffer
            Flowable<Buffer> stream = getStringFlowable()
                .map(s -> vertx.fileSystem().readFileBlocking(s));

            // Transform the string single int a Buffer single
            Single<Buffer> single = getStringSingle()
                .map(s -> vertx.fileSystem().readFileBlocking(s));

The drawback of the map operator is the imposed synchrony, in order to retrieve the content of a file we have to use the blocking version of the filesystem, and thus we break the Vert.x golden rule.

Fortunately there is an asynchronous version called flatMap.

            // Transform the stream of strings into a stream of Buffer
            Flowable<Buffer> stream = getStringFlowable()
                .flatMap(s -> {
                    Single<Buffer> single = vertx.fileSystem().rxReadFile();
                    return single.toFloable();
                });

            // Transform the string single into a Buffer single
            Single<Buffer> single = getStringSingle()
                .flatMap(s -> {
                    Single<Buffer> single = vertx.fileSystem().rxReadFile();
                    return single;
                })

The zip operator combines the results of several Flowable/Single in a single result, let's see with Single:

            Single<String> single1 = getStringSingle();
            Single<String> single2 = getStringSingle();
            Single<String> single3 = getStringSingle();                

            Single<String> combinedSingle = Single
                .zip(single1, single2, single3, (s1, s2, s3) -> s1 + s2 + s3);

            combinedSingle
                .subscribe(
                    s -> {
                        // Got the three concatenated strings
                    },
                    error -> {
                        // At least one of single1, single2 or single3 failed
                    }
                );



It works similarly for Flowable, but for the sake of the conciseness we will not study it here.


## Vert.x + RX Java

Vert.x has an RX version of its asynchronous API packaged with the io.vertx.reactivex prefix, e.g., io.vertx.reactivex.core.Vertx is the RX-ified version of io.vertx.core.Vertx. The rxified version of Vert.x exposes the asychronous methods as Single and the stream types as Flowable.


### Vert.x streams => Flowables

The type ReadStream<T> models a reactive sequence of T items, for instance an HttpServerRequest is a ReadStream<Buffer>.

The rxified version exposes a toFlowable() method to turn the stream into an Flowable<T>:

            import io.vertx.reactivex.core.Vertx;
            import io.vertx.reactivex.core.http.HttpServer;

            ...

            Vertx vertx = Vertx.vertx();
            HttpServer server = vertx.createHttpServer();
            server.requestHandler(request -> {
                if (request.path().equals("/upload")) {
                    Flowable<Buffer> stream = request.toFlowable();
                    stream.subscribe(buffer -> {
                        // Got an upload buffer
                    },
                    error -> {
                        // Got an error => no more buffers
                    },
                    () -> {
                        // Done => no more buffers
                    });
                }
            });


## Vert.x Handler/Future => Singles


Each asynchronous method, i.e., a method having a last parameter Handler<AsyncResult<T>>, has an rxified version, named after the original method name prefixed by rx, with the same parameters minus the last and ruturning a Single of the asynchronous type.

Unlike the original method, calling the rx version does not make an actual call. Instead you get a single that will call the actual method at subscription time.

            import io.vertx.reactivex.core.Vertx;
            import io.vertx.reactivex.core.http.HttpServer;

            ...

            Vertx vertx = Vertx.vertx();
            HttpServer server = vertx.createHttpServer();
            server.requestHandler(request -> ...);

            // The single has been created but the server is actually not starting at this point
            Single<HttpServer> listenSingle = server.rxListen(8080);

            // Triggers the actual start
            listenSingle
                .subscribe(
                    server -> {
                        // Ther server is started and bound on 8080
                    },
                    error -> {
                        // The server could not start
                    }
                );



### Exercise 1 - Vert.x applications are Java applications

In this first exercise, let's start from the very beginning.

    1. Create an instance of Vert.x.

    2. Start an Http server sending greetings

