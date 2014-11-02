/*
 * Copyright 2013 Stefan Domnanovits
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.codebullets.sagalib.perftest;

import com.codebullets.sagalib.MessageStream;
import com.codebullets.sagalib.guice.SagaModuleBuilder;
import com.codebullets.sagalib.perftest.messages.AbstractTestMessage;
import com.codebullets.sagalib.perftest.messages.TestMessage1;
import com.google.common.base.Stopwatch;
import com.google.inject.Guice;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PerformanceTest {
    private MessageStream msgStream;

    @Before
    public void init() {
        msgStream = Guice.createInjector(SagaModuleBuilder.configure().build()).getInstance(MessageStream.class);
    }

    @Test
    public void executeWithSingleSaga() throws Exception {
        warmup();

        Histogram histogram = new Histogram(TimeUnit.MILLISECONDS.toMicros(1000), 3);
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        int repeats = 100_000;

        for (int i = 0; i < repeats; ++i) {
            AbstractTestMessage message = createTestMessage(TestMessage1.class);

            stopwatch.start();
            handleMessage(message);
            stopwatch.stop();

            histogram.recordValue(stopwatch.elapsed(TimeUnit.MICROSECONDS));

            stopwatch.reset();
        }

        printResults("Single saga " + repeats + " times handled", histogram);
    }

    private void warmup() throws Exception {
        for (int i = 0; i < 100; ++i) {
            AbstractTestMessage message = createTestMessage(TestMessage1.class);
            handleMessage(message);
        }

        Thread.sleep(500);
    }

    private void printResults(final String title, final Histogram histogram) {
        System.out.println("***************");
        System.out.println(title);
        System.out.println("Recorded " + histogram.getHistogramData().getTotalCount() + " records");
        System.out.println("Max            : " + histogram.getHistogramData().getMaxValue() + "µs");
        System.out.println("Min            : " + histogram.getHistogramData().getMinValue() + "µs");
        System.out.println("Mean           : " + histogram.getHistogramData().getMean() + "µs");
        System.out.println("Std. Deviation : " + histogram.getHistogramData().getStdDeviation());
        System.out.println();

        histogram.getHistogramData().outputPercentileDistribution(System.out, 2, 1.0);
        printLogarithmic(histogram);
    }

    private void printLogarithmic(Histogram histogram) {
        System.out.println();
        System.out.println("= logarithmic histogram =");
        System.out.println();
        System.out.println("value\tpercentile\ttotal count");

        for (HistogramIterationValue val : histogram.getHistogramData().logarithmicBucketValues(1, 10)) {
            long valueIteratedTo = val.getValueIteratedTo();
            long countIteratedTo = val.getTotalCountToThisValue();
            double percentile = val.getPercentile();
            System.out.println(valueIteratedTo + "\t" + percentile + "\t" + countIteratedTo);
        }
    }

    private <T extends AbstractTestMessage> T createTestMessage(Class<T> msgClass) throws Exception {
        T msg = msgClass.newInstance();
        msg.setCorrelationId(UUID.randomUUID().toString());
        msg.setSagaFinished(true);

        return msg;
    }

    private void handleMessage(final AbstractTestMessage message) {
        try {
            msgStream.handle(message);
        } catch (Exception ex) {
            // will not happen
            System.out.println("Unexpected exception. " + ex.getMessage());
        }
    }
}