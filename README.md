Calrissian Insight
=======

Insight is a series of Java based utilities that have help people be more productive in testing.  The main utility is the AssertBuilder.  Here are some of the issues I am trying to solve with the AssertBuilder model:

* Disclose all failures of the test at once.

* Simplify the assertions of tests.

Here is an example of the AssertBuilder in action:
<code>

    import org.calrissian.insight.junit.AssertBuilderAdapterRule;
    import org.junit.Rule;
    import org.junit.Test;
    import static org.hamcrest.Matchers.is;
        
    public class MyTest {

        /**
        * A Junit Rule that will be applied on each test of this class.  This rule
        * will make sure all assertions are ran after the test and then clear out
        * the assertions for the next test.
        */
        @Rule
        public AssertBuilderAdapterRule assertRule = new AssertBuilderAdapterRule();

        /**
         * This example shows the AssertBeanBuilder in action.  The AssertBeanBuilder 
         * has the ability to dive inside a bean and test all the elements of the bean
         * that are important for the test.  Everyone of the assertions will be 
         * combined into one AssertionError giving you the full picture of what is 
         * wrong. Because we are using the AssertBuilderAdapterRule to build the
         * AssertBeanBuilder all AssertionErrors will be combined that were built for
         * this test into one AssertionError.
         */
        @Test
        public void test() {

            // Because the AssertBeanBuilder was created from the assertRule all  
            // the AssertBeanBuilder assertions will be ran after the test  
            // method.  There is also a means to pass a Callable to the 
            // AssertBeanBuilder to get the bean at assertion time.
            assertRule.createAssertBeanBuilder(new Object(){
                public Object getBeanProperty() {
                    return new Object() {
                        public int getChild() {
                            return 5;
                        }
                    };
                }
            // add an assertion for this bean.  This is a builder and can be chained to have
            // a multitude of assertions for this bean.
            }).that("beanProperty.child", is(5));
        }

    }
</code>


