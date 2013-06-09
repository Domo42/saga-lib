package com.codebullets.sagalib.startup;

/**
 * Creates a new instance of an {@link com.codebullets.sagalib.MessageStream} to run the saga lib.
 */
public final class EventStreamBuilder implements StreamBuilder {
    /**
     * Prevent instantiation from outside. Use {@link #configure()} instead.
     */
    private EventStreamBuilder() {
    }


    /**
     * Start configuration and creation of the saga lib event stream.
     */
    public static StreamBuilder configure() {
        return new EventStreamBuilder();
    }
}
