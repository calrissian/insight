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
package org.calrissian.insight.junit;

import org.calrissian.insight.AssertBeanBuilder;
import org.calrissian.insight.AssertBuilder;
import org.calrissian.insight.AssertBuilderCollector;
import org.junit.rules.Verifier;

import java.util.concurrent.Callable;

/**
 * This is an adapter to a Junit4 Rule.  This rule will keep track of all AssertBuilders and will run the
 * assertions at the end of the test.  This rule needs to follow the JUnit rule using the @Rule or @ClassRule
 * annotation.
 */
public class AssertBuilderAdapterRule extends Verifier {

    /**
     * The Collection of AssertBuilders
     */
    private final AssertBuilderCollector assertBuilderCollector = new AssertBuilderCollector();

    /**
     * Adds an AssertBuilder
     *
     * @param assertBuilder
     */
    public void add(final AssertBuilder assertBuilder) {
        assertBuilderCollector.add(assertBuilder);
    }

    @Override
    protected void verify() throws Throwable {
        // verify  and clear out the rules
        assertBuilderCollector.runAndReset();
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
        return assertBuilderCollector.createAssertBeanBuilder(callable);
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
        return assertBuilderCollector.createAssertBeanBuilder(bean);
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
        return assertBuilderCollector.createAssertBeanBuilder(name, callable);
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
        return assertBuilderCollector.createAssertBeanBuilder(name, bean);
    }

}
