/*
 * Copyright (C) 2014 The Calrissian Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.calrissian.insight;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A collector of AssertBuilders that can be ran together and all assertion failures will be combined. This class can
 * be extended to add other types of
 * AssertBuilders or be used to add AssertBuilders directly.
 */
public class AssertBuilderCollector implements AssertBuilder {

    /**
     * The list of assertions
     */
    private final List<AssertBuilder> assertions = new LinkedList<AssertBuilder>();

    /**
     * Adds an org.calrissian.insight.AssertBuilder to the collection of assertions to run.
     *
     * @param assertBuilder to add
     */
    public void add(final AssertBuilder assertBuilder) {
        if (assertBuilder == null) {
            throw new NullPointerException("Cannot add a null assertBuilder");
        }
        assertions.add(assertBuilder);
    }

    /**
     * Clears out all assertions to run.  This is equivalent to starting fresh with no assertions added.
     */
    public void reset() {
        assertions.clear();
    }

    /**
     * Runs all the assertions.
     */
    public void runAssert() {
        // run through all the assertions.
        final List<AssertionError> failures = new LinkedList<AssertionError>();
        for (final AssertBuilder assertBuilder : assertions) {
            try {
                assertBuilder.runAssert();
            } catch (final AssertionError e) {
                // collect all the failures
                failures.add(e);
            }
        }

        // if we have no failures we have succeeded in our assertions
        if (failures.isEmpty()) {
            return;
        }
        if (failures.size() == 1) {
            final AssertionError assertionError = failures.get(0);
            throw assertionError;
        }
        // throw the combined error
        throw new MultipleAssertionError(null, failures);
    }

    /**
     * Runs all the assertions and then clears out the assertions.
     *
     * @throws java.lang.AssertionError if the assertions failed.
     */
    public void runAndReset() {
        try {
            runAssert();
        } finally {
            reset();
        }
    }

    /**
     * Creates an AssertBeanBuilder.  The AssertBeanBuilder will be registered to this object and when this runAssert
     * method is called the AssertBeanBuilder's runAssert method will get called.
     *
     * @param callable to access the bean when it is time to run the assertion
     * @param <T>      type of bean being asserted
     * @return a new AssertBeanBuilder
     */
    public <T> AssertBeanBuilder<T> createAssertBeanBuilder(final Callable<T> callable) {
        final AssertBeanBuilder<T> builder = new AssertBeanBuilder(callable);
        add(builder);
        return builder;
    }

    /**
     * Creates an AssertBeanBuilder.  The AssertBeanBuilder will be registered to this object and when this runAssert
     * method is called the AssertBeanBuilder's runAssert method will get called.
     *
     * @param bean to run assertions
     * @param <T>  type of bean being asserted
     * @return a new AssertBeanBuilder
     */
    public <T> AssertBeanBuilder<T> createAssertBeanBuilder(final T bean) {
        return createAssertBeanBuilder(identity(bean));
    }

    /**
     * Creates an AssertBeanBuilder.  The AssertBeanBuilder will be registered to this object and when this runAssert
     * method is called the AssertBeanBuilder's runAssert method will get called.
     *
     * @param name     of the bean.  Used for identifying the bean among other AssertBuilders.
     * @param callable to access the bean when it is time to run the assertion
     * @param <T>      type of bean being asserted
     * @return a new AssertBeanBuilder
     */
    public <T> AssertBeanBuilder<T> createAssertBeanBuilder(final String name, final Callable<T> callable) {
        final AssertBeanBuilder<T> builder = new AssertBeanBuilder(name, callable);
        add(builder);
        return builder;
    }

    /**
     * Creates an AssertBeanBuilder.  The AssertBeanBuilder will be registered to this object and when this runAssert
     * method is called the AssertBeanBuilder's runAssert method will get called.
     *
     * @param name of the bean.  Used for identifying the bean among other AssertBuilders.
     * @param bean to run assertions
     * @param <T>  type of bean being asserted
     * @return a new AssertBeanBuilder
     */
    public <T> AssertBeanBuilder<T> createAssertBeanBuilder(final String name, final T bean) {
        return createAssertBeanBuilder(name, identity(bean));
    }

    /**
     * Creates the identity callable.
     *
     * @param object to return from the callable.
     * @param <T>    the type of object being returned by the callable.
     * @return a new callable that will return the object.
     */
    private <T> Callable<T> identity(final T object) {
        return new Callable<T>() {
            @Override
            public T call() throws Exception {
                return object;
            }
        };
    }
}
