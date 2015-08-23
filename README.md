saga-lib
--------

Java saga library to organize domain events. The goal is to organize state and
activities triggered by any sort of messages. An example of what this actually
means can be found in the [wiki].

This lib makes it easy for a developer to focus on the actual state changes and
message handling. To do this the persistence of state as well as the possible
escalation using timeouts is separated from the actual business code.

### Add Reference to saga-lib

It is recommended to use Maven to reference the saga-lib binaries.

```xml
<dependency>
   <groupId>com.codebullets.saga-lib</groupId>
   <artifactId>saga-lib</artifactId>
   <version>1.1.0</version>
</dependency>
```

The binaries and code are also available for download from the [Maven Central]
repository.

### Starting the saga lib

The lib is started using the provided builder class. It will use default
implementations for things like persistence and timeouts if not specified
otherwise. The *build()* method returns an instance ready to be used.

```java
MessageStream msgStream = EventStreamBuilder.configure()
        .usingSagaProviderFactory(sagaProvider)
        .build();
```

The above code will create a *MessageStream* instance. Use this instance to
bring messages into the lib. The object *sagaProvider* from the example above
is responsible to return individual [JSR-330] providers used to request new
instances of the respective saga type. [Guice] as well as [Spring] both support
JSR-330, so it is quite easy to create a custom provider.

The above example will persist the state of the saga in memory, timeouts will
be triggered based on Java timers and available Sagas will be automatically
determined by scanning the current classpath. All of this behaviour can be 
customized by providing own implementations and calling one of the *using*
methods.

A saga may look like the example below. The messages handled are POJOs holding
only the event data. They can be of any object type. The *MySagaState* type is
expected to be a POJO as well implementing the *SagaState* interface. The
method starting the saga is indicated by the *@StartsSaga* annotation while all
further handlers need to be annotated with *@EventHandler*. There can only be
one message starting a saga. There is no limit to the number of further
event handlers.

```java
public class MySaga extends AbstractSaga<MySagaState> {

    @StartsSaga
    public void sagaStartup(StartingMessage startedByMessage) {
        // perform saga start logic
        // save id to map further messages back to the specific saga state
        state().addInstanceKey(startedByMessage.getId());
        requestTimeout(60, TimeUnit.SECONDS);
    }

    @EventHandler
    public void continueSagaWithOtherMessage(OtherMessage message) {
        // perform custom logic and mark saga as finished
        setFinished();
    }

    @EventHandler
    public void handleTimeout(Timeout timeout) {
        // perform timeout logic
        // timeout finishes saga therefore call setFinished()
        setFinished();
    }

    @Override
    public void createNewState() {
        setState(new MySagaState());
    }

    @Override
    public Collection<KeyReader> keyReaders() {
        // creates a reader used to map between the OtherMessage event
        // and the stored instance key of the existing saga state.
        // Note: Much simpler with Java 8 lambda expressions.
        KeyReader reader = KeyReaders.forMessage(
                OtherMessage.class,
                new KeyExtractFunction<OtherMessage, String>() {
                    @Override
                    public String key(final OtherMessage message) {
                        return message.getId();
                    }
                }
        );

        return Lists.newArrayList(reader);
    }
}
```

[wiki]: https://github.com/Domo42/saga-lib/wiki/Sagas
[Maven Central]: http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.codebullets.saga-lib%22
[JSR-330]: http://jcp.org/en/jsr/detail?id=330
[Guice]: https://github.com/google/guice
[Spring]: http://www.springsource.org/spring-framework