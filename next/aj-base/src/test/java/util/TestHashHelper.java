package util;

import com.ajaxjs.util.HashHelper;
import com.ajaxjs.util.io.Resources;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestHashHelper {
    @Test
    void testCalcFileMD5() {
        String resourceText = Resources.getResourceText("test.txt");
        System.out.println(resourceText);
        InputStream in = Resources.getResource("test.txt");
        String md5 = HashHelper.calcFileMD5(in);
        System.out.println(md5);


        assertEquals(HashHelper.md5("你好 Hi"), md5);
    }
}
