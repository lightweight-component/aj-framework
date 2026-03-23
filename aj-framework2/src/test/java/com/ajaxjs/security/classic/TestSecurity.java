package com.ajaxjs.security.classic;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSecurity {
    private ListCheck c;

    //	@Test
//	public void testListCheck() {
//		assertTrue(c.isInWhiteList("test_abc"));
//		assertTrue(!c.isInWhiteList("test_abc22"));
//		assertTrue(!c.isInBlackList("test_abc22"));
//	}

    @Test
    public void testXss() {
        String script = "Foo <script>alert(3)</script>";
        assertEquals("Foo ", InstallFilter.cleanXSS(script));
        assertEquals("&lt;script&gt;alert(3)&lt;/script&gt;", InstallFilter.cleanXSS(script, InstallFilter.Handle.TYPE_ESCAPE));
    }
//
//	@Autowired
//	private SqlInject sqlInject;
//
//	@Test
//	public void testSqlInject() {
//		String str = "?id=1&SELECT * FROM user";
//		assertEquals("?id=1& * FROM user", sqlInject.clean(str));
//	}

    @Test
    public void testFilter() {
        String str = "abc \rlk\n";
        assertEquals("abc lk", InstallFilter.cleanCRLF(str));

        str = "?id=1&SELECT * FROM user";
//        assertEquals("?id=1& * FROM user", Filter.cleanSqlInject(str));

        String script = "Foo <script>alert(3)</script>";
        assertEquals("Foo ", InstallFilter.cleanXSS(script));
        assertEquals("&lt;script&gt;alert(3)&lt;/script&gt;", InstallFilter.cleanXSS(script, InstallFilter.Handle.TYPE_ESCAPE));
    }
}
