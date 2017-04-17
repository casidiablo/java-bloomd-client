## java-bloomd-client

Java client for [armon's bloomd](https://github.com/armon/bloomd) built atop [netty](https://github.com/netty/netty).

### Usage

Interactions with a `bloomd` server are done through the `BloomdClient` interface which
can be created using the `BloomdClient.newInstance("host", port)` method. The API is
purely asynchronous so all methods provided by the interface, including `newInstance`,
return a `Future`.

```java
// get bloomd client implementation
BloomdClient client = BloomdClient.newInstance("localhost", port).get();

// create a filter
assert client.create("someFilterName").get() == CreateResult.DONE;

// set and check for an item
assert client.set("someFilterName", "nishtiman").get() == StateResult.YES;
assert client.check("someFilterName", "nishtiman").get() == StateResult.YES;
assert client.check("someFilterName", "non-extant").get() == StateResult.NO;
```

### RxJava extension

Using `Future`s, though a common practice for asynchronous APIs, is cumbersome due to the limitations of this interface. A RxJava extension is provided that provides a better way to chain computations as well as centralize error handling and timeouts, etc. Here's the same example presented above but using RxJava:

```java
RxBloomdClient client = RxBloomdClient.newInstance("localhost", 8673);

// make sure filters can be created
TestSubscriber<StateResult> subscriber = new TestSubscriber<>();
client.create("someFilterName")
      .flatMap(createResult -> {
          // filter should have been created
          assert createResult == CreateResult.DONE; 
     
          return client.set("someFilterName", "nishtiman");
      })
      .flatMap(setResult -> {
          // should be YES because the filter didn't have the item
          assert setResult == StateResult.YES;
      
          return client.check("someFilterName", "nishtiman");
      })
      .flatMap(checkResult -> {
          // should be YES because the filter had the item
          assert checkResult == StateResult.YES;
      
          return client.check("someFilterName", "non-extant");
      })
      .doOnNext(checkResult -> {
          // should be NO because the filter does not have "non-extant"
          assert checkResult == StateResult.NO;
      })
      .doOnError(throwable -> {
          // should never hit this
          assert false;
      })
      .subscribe(subscriber);
```

### Connection pooling

A pooling mechanism is provided to allow concurrent connections to a single server:

```java
int poolSize = 20;
BloomdClientPool bloomdClientPool = new BloomdClientPool("host", 8673, poolSize);

// acquire a new client from the pool
Future<BloomdClient> clientFuture = bloomdClientPool.acquire();
BloomdClient client = clientFuture.get();

// do some cool stuff...

// release client
Future<Void> releaseFuture = bloomdClientPool.release(client);
```

The RxJava extension also offers a pooling implementation atop `BloomdClientPool`:

```java
RxBloomdClientPool rxClientPool = new RxBloomdClientPool("host", 8673, 5);
Observable<RxBloomdClient> clientObservable = rxClientPool.acquire();
```

### Installation

Gradle:

```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.casidiablo.java-bloomd-client:bloomd-client:0.13'
    compile 'com.github.casidiablo.java-bloomd-client:rx-bloomd-client:0.13'
}
```

Leiningen:

```clojure
:repositories [["jitpack" "https://jitpack.io"]]
:dependencies [[com.github.casidiablo.java-bloomd-client/bloomd-client "0.13"]]
```

Maven:

```xml
<repositories>
    <repository>
	    <id>jitpack.io</id>
		<url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.casidiablo.java-bloomd-client</groupId>
        <artifactId>bloomd-client</artifactId>
        <version>0.13</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```