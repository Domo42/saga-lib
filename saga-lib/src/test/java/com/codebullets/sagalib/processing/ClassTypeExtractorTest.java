package com.codebullets.sagalib.processing;

import com.codebullets.sagalib.handling.BaseMessage;
import com.codebullets.sagalib.handling.ConcreteMessage;
import com.codebullets.sagalib.handling.MarkerInterface;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

/**
 * Tests for {@link com.codebullets.sagalib.processing.ClassTypeExtractor} class.
 */
public class ClassTypeExtractorTest {
    /**
     * <pre>
     * Given => {@link com.codebullets.sagalib.handling.ConcreteMessage} is used.
     * When  => super classes are queried.
     * Then  => returns list of super classes
     * </pre>
     */
    @Test
    public void superClasses_ConcreteMessage_returnsSuperClassTypes() {
        // given
        ClassTypeExtractor sut = new ClassTypeExtractor(ConcreteMessage.class);

        // when
        Iterable<Class<?>> classes = sut.allClasses();

        // then
        assertThat("Expected list of super classes.", classes, hasItems(ConcreteMessage.class, BaseMessage.class, Object.class));
    }

    /**
     * <pre>
     * Given => {@link com.codebullets.sagalib.handling.ConcreteMessage} is used.
     * When  => interfaces are queried
     * Then  => returns list of interfaces
     * </pre>
     */
    @Test
    public void interfaces_ConcreteMessage_returnsMarkerInterface() {
        // given
        ClassTypeExtractor sut = new ClassTypeExtractor(ConcreteMessage.class);

        // when
        Iterable<Class<?>> interfaces = sut.interfaces();

        // then
        assertThat("Expected interface of message class.", interfaces, hasItem(MarkerInterface.class));
    }

    /**
     * <pre>
     * Given => Array list used for extraction
     * When  => interfaces are queried
     * Then  => returns base interface of direct implementation
     * </pre>
     */
    @Test
    public void interfaces_ArrayList_returnsSuperInterfacesOfImplementedInterfaces() {
        // given
        ClassTypeExtractor sut = new ClassTypeExtractor(ArrayList.class);

        // when
        Iterable<Class<?>> interfaces = sut.interfaces();

        // then
        assertThat("Expected iterable base interface.", interfaces, hasItem(Iterable.class));
    }
}