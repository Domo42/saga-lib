package com.codebullets.sagalib;

import com.codebullets.sagalib.annotations.AnnotationFinishMessage;
import com.codebullets.sagalib.annotations.AnnotationHandlerMessage;
import com.codebullets.sagalib.annotations.AnnotationStartingMessage;
import com.codebullets.sagalib.description.AutoTypedMessage;
import com.codebullets.sagalib.description.DescriptionFinishMessage;
import com.codebullets.sagalib.description.DescriptionHandlerMessage;
import com.codebullets.sagalib.description.DescriptionStartingMessage;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.TimeUnit;


@State(Scope.Thread)
public class HandlersTestFixture {
    private final static SagaLibStream sagaLibStream = new SagaLibStream();

    private long key = 0;

    @Setup
    public void setup() {
        // prevent certain possible optimization, in case
        // there's only a single saga type
        annotationHandler();
        annotationSaga();
        descriptionHandler();
        descriptionSaga();
        autoTypedDescriptionHandler();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureAnnotationHandlerAverage() {
        annotationHandler();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureAnnotationHandlerThroughput() {
        annotationHandler();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureDescriptionHandlerAverage() {
        descriptionHandler();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureDescriptionHandlerThroughput() {
        descriptionHandler();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureAnnotationSagaAverage() {
        annotationSaga();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureAnnotationSagaThroughput() {
        annotationSaga();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureDescriptionSagaAverage() {
        descriptionSaga();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureDescriptionSagaThroughput() {
        descriptionSaga();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureAutoTypedHandlerThroughput() {
        autoTypedDescriptionHandler();
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    public void measureAutoTypedHandlerAverage() {
        autoTypedDescriptionHandler();
    }

    private void annotationSaga() {
        long key = nextKey();
        sagaLibStream.handle(new AnnotationStartingMessage(key));
        sagaLibStream.handle(new AnnotationFinishMessage(key));
    }

    private void descriptionSaga() {
        long key = nextKey();
        sagaLibStream.handle(new DescriptionStartingMessage(key));
        sagaLibStream.handle(new DescriptionFinishMessage(key));
    }

    private void annotationHandler() {
        sagaLibStream.handle(new AnnotationHandlerMessage());
    }

    private void descriptionHandler() {
        sagaLibStream.handle(new DescriptionHandlerMessage());
    }

    private void autoTypedDescriptionHandler() {
        sagaLibStream.handle(new AutoTypedMessage());
    }

    private long nextKey() {
        long nextKey = key;
        key++;
        return nextKey;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(HandlersTestFixture.class.getSimpleName())
                .measurementTime(TimeValue.seconds(10))
                .warmupIterations(10)
                .measurementIterations(1)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
