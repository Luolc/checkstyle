package com.puppycrawl.tools.checkstyle;

import org.junit.Test;

/**
 * @author LuoLiangchen
 */
public class UnitTestProcessorCheckTest extends AbstractModuleTestSupport {
    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/unittestprocessor";
    }

    @Test
    public void testFoo() throws Exception {
        final DefaultConfiguration config = createModuleConfig(UnitTestProcessorCheck.class);
        final String[] expected = new String[0];
        verify(config, getPath("InputFinalLocalVariableCheckTest.java"), expected);
    }
}
