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

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.beans.PropertyUtil;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

/**
 * An org.calrissian.insight.AssertBuilder tailored for accessing Java Beans.  This org.calrissian.insight
 * .AssertBuilder is good to use
 * when a Java Bean needs
 * assertions applied to it.  Methods have been made to make access Java Bean Properties easy.
 */
public class AssertBeanBuilder<T> extends AbstractMatcherAssertBuilder<T> {

    /**
     * Constructor
     *
     * @param name     that identifies the Assertions
     * @param callable that gets the bean
     */
    public AssertBeanBuilder(final String name, final Callable<T> callable) {
        super(name, callable);
    }

    /**
     * Constructor
     *
     * @param callable that gets the bean
     */
    public AssertBeanBuilder(final Callable<T> callable) {
        super(callable);
    }

    /**
     * Check that the bean property matches the matcher during the running of the assertions.
     * The bean property syntax is as follows:
     * <p>
     * Bean Properties: <code>propertyName</code>
     * List: <code>propertyName[index]</code>
     * Map:  <code>propertyName[key]</code>
     * Child Bean Properties: <code>propertyName.propertyName...</code>
     * </p>
     *
     * @param property of the object the matcher will be applied
     * @param matcher  that will be applied to the property
     * @return this
     * @throws java.lang.NullPointerException if the property or matcher is null.
     */
    public AssertBeanBuilder that(final String property, final Matcher<?> matcher) {
        if (property == null) {
            throw new NullPointerException("property cannot be null");
        }
        if (matcher == null) {
            throw new NullPointerException("Matcher cannot be null.");
        }
        addMatcher(new PropertyMatcher(matcher, property));
        return this;
    }

    /**
     * Check to see if the bean is null.
     *
     * @return this
     */
    public AssertBeanBuilder isNull() {
        addNullCheck();
        return this;
    }

    /**
     * Check to see if the bean is not null during the run assertions.
     *
     * @return not
     */
    public AssertBeanBuilder notNull() {
        addNotNullCheck();
        return this;
    }

    public AssertBeanBuilder that(final Matcher<?> matcher) {
        if (matcher == null) {
            throw new NullPointerException("Matcher cannot be null.");
        }
        addMatcher(matcher);
        return this;
    }

    /**
     * Matcher that will get the property from the object and pass to the subMatcher.
     */
    private static class PropertyMatcher extends FeatureMatcher {

        /**
         * The list of properties to go through to get the final bean.
         */
        private final List<BeanAccessor> accessors;

        /**
         * Constructor
         *
         * @param subMatcher The matcher to apply to the feature
         * @param property   the property of the object to access.
         */
        public PropertyMatcher(final Matcher subMatcher,
                               final String property) {
            super(subMatcher, property, property);
            accessors = parse(property);
        }

        /**
         * Parses the propertyString to valid BeanAccessors.
         *
         * @param propertyString that needs to be parsed
         * @return
         */
        private List<BeanAccessor> parse(final String propertyString) {
            if (propertyString.trim().isEmpty()) {
                throw new IllegalArgumentException("Property must have at least one bean property.");
            }

            // go through each property
            final List<BeanAccessor> myAccessors = new ArrayList<BeanAccessor>();
            final StringBuilder propertyBeanName = new StringBuilder(propertyString.length());
            for (final StringTokenizer tokenizer = new StringTokenizer(propertyString,
                    "."); tokenizer.hasMoreElements(); ) {
                final String property = tokenizer.nextToken();
                if (propertyBeanName.length() != 0) {
                    propertyBeanName.append('.');
                }
                propertyBeanName.append(property);

                if (property.isEmpty()) {
                    throw new IllegalArgumentException("Property (" + propertyString + ") is missing a property name " +
                            "or has two . next to each other.");
                }
                final StringBuilder propertyName = new StringBuilder(property.length());
                boolean inBracket = false;
                boolean done = false;
                final StringBuilder bracketText = new StringBuilder(property.length());
                for (final char character : property.toCharArray()) {
                    if (inBracket) {
                        if (']' == character) {
                            done = true;
                            continue;
                        } else {
                            bracketText.append(character);
                            continue;
                        }
                    } else if ('[' == character) {
                        inBracket = true;
                        continue;
                    } else if (!Character.isJavaIdentifierPart(character)) {
                        throw new IllegalArgumentException("Property (" + propertyString + ") contains an invalid " +
                                "character (" + character + ").");
                    }
                    propertyName.append(character);
                }
                // do we have a bracket
                if (inBracket) {
                    if (!done) {
                        throw new IllegalArgumentException("Property (" + propertyString + ") must end in a bracket " +
                                "(]).");
                    }
                    if (propertyName.length() != 0) {
                        myAccessors.add(new PropertyBeanAccessor(propertyName.toString(), propertyBeanName.toString()));
                    }
                    myAccessors.add(new BracketAccessor(bracketText.toString(), propertyBeanName.toString()));
                } else {
                    myAccessors.add(new PropertyBeanAccessor(propertyName.toString(), propertyBeanName.toString()));
                }
            }
            return myAccessors;
        }

        @Override
        protected Object featureValueOf(final Object actual) {
            // got through the accessor till we get the object
            // we want.
            Object currentObject = actual;
            for (final BeanAccessor accessor : accessors) {
                currentObject = accessor.get(currentObject);
            }
            return currentObject;
        }
    }

    /**
     * Interface for how we will access the bean properties
     */
    private static interface BeanAccessor {

        /**
         * Gets the bean element from the object.
         *
         * @param object to get the element from.
         * @return the object retrieved.
         */
        Object get(final Object object);
    }

    /**
     * Accesses the Property of the bean.
     */
    private static class PropertyBeanAccessor implements BeanAccessor {

        /**
         * The name of the property to get
         */
        private final String propertyName;

        /**
         * The full name of this bean.
         */
        private final String beanPropertyName;

        /**
         * Constructor
         *
         * @param propertyName     on the bean being access.
         * @param beanPropertyName the name of the property in relation to the object.
         */
        private PropertyBeanAccessor(final String propertyName, final String beanPropertyName) {
            this.propertyName = propertyName;
            this.beanPropertyName = beanPropertyName;
        }


        @Override
        public Object get(Object object) {
            PropertyDescriptor property = PropertyUtil.getPropertyDescriptor(propertyName, object);
            if (property == null) {
                final String beanName = (beanPropertyName.length() == propertyName.length()) ?
                        "root bean." : "bean " + beanPropertyName.substring(0,
                        beanPropertyName.length() - propertyName.length());
                throw new AssertionError("Property (" + propertyName + ") does not exist on " + beanName);
            }
            try {
                return property.getReadMethod().invoke(object);
            } catch (final IllegalAccessException e) {
                final AssertionError a = new AssertionError("Error accessing bean (" + beanPropertyName + ") reason: " +
                        "" + e);
                a.initCause(e);
                throw a;
            } catch (final InvocationTargetException e) {
                final AssertionError a = new AssertionError("Error accessing bean (" + beanPropertyName + ") reason: " +
                        "" + e.getCause());
                a.initCause(e.getCause());
                throw a;
            }
        }
    }

    /**
     * Bean accessor for when the user has brackets []
     */
    private static class BracketAccessor implements BeanAccessor {

        /**
         * The key to the Collection/Array/Map
         */
        private final String key;

        /**
         * The name of the beanProperty
         */
        private final String beanPropertyName;

        /**
         * Constructor
         *
         * @param key
         */
        private BracketAccessor(final String key, final String beanPropertyName) {
            this.key = key;
            this.beanPropertyName = beanPropertyName;
        }


        @Override
        public Object get(Object object) {

            if (object == null) {
                throw new AssertionError("Cannot access into Map, List, or Array of " + beanPropertyName + " because " +
                        "the bean is null.");
            }

            if (object instanceof List) {
                final List list = (List) object;
                return list.get(getIndex());
            }
            if (object.getClass().isArray()) {
                final Object[] array = (Object[]) object;
                return array[getIndex()];
            }

            // check if the object is a map
            if (object instanceof Map) {
                final Map map = (Map) object;
                // may want to do some more sophisticated things with the key
                // like if the keys are Enums convert the key.
                return map.get(key);
            }
            throw new AssertionError(beanPropertyName + " is not a Map, List or Array but a " + object.getClass()
                    .getCanonicalName());
        }

        /**
         * Gets the index into the List.
         *
         * @return the index into the list
         */
        private int getIndex() {
            try {
                return Integer.valueOf(key);
            } catch (final NumberFormatException e) {
                final AssertionError a = new AssertionError(key + " cannot index into bean (" +
                        beanPropertyName + ").  The index must be a number when accessing Lists or Arrays.");
                a.initCause(e);
                throw a;
            }
        }
    }
}
