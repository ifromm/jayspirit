package hyspirit.engines.knowledgeBase;
import static org.junit.Assert.fail;

import org.junit.Test;

import hyspirit.knowledgeBase.HyTuple;

/**
 * @author Ingo Frommholz &lt;ingo@frommholz.org&gt;
 *
 */
public class HyTupleTestCase {

    /**
     * Test method for
     * {@link hyspirit.knowledgeBase.HyTuple#equals(java.lang.Object)}.
     */
    @Test
    public final void testEqualsObject() {
	HyTuple tuple1 = new HyTuple(0.8, "test1", "test1.1");
	HyTuple tuple2 = new HyTuple(0.8, "test1", "test1.1");
	HyTuple tuple3 = new HyTuple(0.6, "test1", "test1.1");
	HyTuple tuple4 = new HyTuple(0.8, "test2", "test2.1");

	if (!tuple1.equals(tuple1)) fail("!tuple1.equals(tuple1)");
	if (!tuple1.equals(tuple2)) fail("!tuple1.equals(tuple2)");
	if (tuple1.equals(tuple3)) fail("tuple1.equals(tuple3)");
	if (tuple1.equals(tuple4)) fail("tuple1.equals(tuple4)");
    }

}
