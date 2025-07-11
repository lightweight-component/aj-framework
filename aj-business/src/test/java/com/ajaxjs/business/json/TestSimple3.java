package com.ajaxjs.business.json;

import com.ajaxjs.business.json.simple3.JSON;
import com.ajaxjs.business.json.simple3.Lexical.JsonLex;
import com.ajaxjs.business.json.simple3.Lexical.LeptType;
import org.junit.jupiter.api.Test;


public class TestSimple3 {
    @Test
    public void testJsonGrammar() {
        String[] str = {
                "\"Hello\" ",
                " 0 ",
                "-0  ",
                "-0.0 ",
                "1    ",
                "-1 ",
                "1.5 ",
                "-1.5 ",
                "3.1416   ",
                "1E10 ",
                "1e10 ",
                "1E+10 ",
                "1E-10 ",
                "-1E10 ",
                "-1e10 ",
                "-1E+10 ",
                "-1E-10 ",
                "1.234E+10 ",
                "1.234E-10 ",
                "1e-10000 ",
                "1.0000000000000002 ",
                "4.9406564584124654e-324 ",
                "-4.9406564584124654e-324 ",
                "2.2250738585072009e-308 ",
                "-2.2250738585072009e-308 ",
                "2.2250738585072014e-308 ",
                "-2.2250738585072014e-308 ",
                "1.7976931348623157e+308 ",
                "-1.7976931348623157e+308 ",
                "\"Hello\\nWorld\"",
                "\"\\\\ \\/ \\b \\f \\n \\r \\t\"",
                "\"Hello\\u0000World\"",
                "\"\\u0024\"",
                "\"\\u00A2\"",
                "\"\\u20AC\"",
                "\"\\uD834\\uDD1E\"",
                "\"\\ud834\\udd1e\"",
                "[ ]",
                "[ null , false , true , 123 , \"abc\" ]",
                "[ [ ] , [ 0 ] , [ 0 , 1 ] , [ 0 , 1 , 2 ] ]",
                " { } ",
                "{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\",\"key4\":[{\"a1\":\"1\",\"a2\":\"2\",\"a3\":\"3\",\"subChildA\":[{\"suba1\":\"3040\",\"suba2\":\"brebb\",\"suba3\":\"fbre\"},{\"suba1\":\"erbrrt\",\"suba2\":\"be4\",\"suba3\":\"5yh5\"},{\"suba1\":\"g445h\",\"suba2\":\"43th\",\"suba3\":\"r5yj4\"}],\"subChildB\":{\"suY1\":\"30L40\",\"suY2\":\"bre00bb\",\"suY3\":\"fbFGFre\",\"subChildA\":[{\"suba1\":\"3040\",\"suba2\":\"brebb\",\"suba3\":\"fbre\"},{\"suba1\":\"erbrrt\",\"suba2\":\"be4\",\"suba3\":\"5yh5\"},{\"suba1\":\"g445h\",\"suba2\":\"43th\",\"suba3\":\"r5yj4\"}]}},{\"a1\":\"s\",\"a2\":\"D\",\"a3\":\"F\"},{\"a1\":\"Q\",\"a2\":\"R\",\"a3\":\"T\"}],\"key5\":[{\"b1\":\"11\",\"b2\":\"21\",\"b3\":\"31\"},{\"b1\":\"3er\",\"b2\":\"3gt\",\"b3\":\"y7u\"},{\"b1\":\"H\",\"b2\":\"Y\",\"b3\":\"R\"}],\"key6\":\"uuid\",\"key7\":{\"vx1\":\"HwH\",\"vx2\":\"YrY\",\"vx3\":\"ReR\"}}",
                "nul ",
                "? ",
                "+0 ",
                "+1 ",
                ".123 ",
                "1. ",
                "INF ",
                "inf ",
                "NAN ",
                "nan ",
                "[1,] ",
                "[\"a\", nul] ",
                "null x",
                "0123",
                "0x0",
                "0x123",
                "1e309",
                "-1e309",
                "\"",
                "\"abc",
                "\"\\v\"",
                "\"\\'\"",
                "\"\\0\"",
                "\"\\x12\"",
                "\"\\u\"",
                "\"\\u0\"",
                "\"\\u01\"",
                "\"\\u012\"",
                "\"\\u/000\"",
                "\"\\uG000\"",
                "\"\\u0/00\"",
                "\"\\u0G00\"",
                "\"\\u0/00\"",
                "\"\\u00G0\"",
                "\"\\u000/\"",
                "\"\\u000G\"",
                "\"\\u 123\"",
                "[1",
                "[1}",
                "[1 2",
                "[[]",
                "{:1,",
                "{1:1,",
                "{true:1,",
                "{false:1,",
                "{null:1,",
                "{[]:1,",
                "{{}:1,",
                "{\"a\":1,",
                "{\"a\"}",
                "{\"a\",\"b\"}",
                "{\"a\":1",
                "{\"a\":1]",
                "{\"a\":1 \"b\"",
                "{\"a\":{}",
                "\"\"",
                "\"Hello\"",
                "\"Hello\\nWorld\"",
                "\"Hello\\u0000World\"",
                "[null,false,true,123,\"abc\",[1,2,3]]",
                "{\"n\":null,\"f\":false,\"t\":true,\"i\":123,\"s\":\"abc\",\"a\":[1,2,3],\"o\":{\"1\":1,\"2\":2,\"3\":3}}",
                "null",
                "false",
                "true",
                "{\"t\":true,\"f\":false,\"n\":null,\"d\":1.5,\"a\":[1,2,3]}"
        };

        int row = 0;

        for (String s : str) {
            row++;
            Object o = JSON.parse(s);
            System.out.println(row + ":" + o);
        }
    }

    @Test
    public void testJsonLex() {
        // String str = "{\"key1\":\"value1\",\"key2\":\"value2\",\"key3\":\"value3\",\"key4\":[{\"a1\":\"1\",\"a2\":\"2\",\"a3\":\"3\",\"subChildA\":[{\"suba1\":\"3040\",\"suba2\":\"brebb\",\"suba3\":\"fbre\"},{\"suba1\":\"erbrrt\",\"suba2\":\"be4\",\"suba3\":\"5yh5\"},{\"suba1\":\"g445h\",\"suba2\":\"43th\",\"suba3\":\"r5yj4\"}],\"subChildB\":{\"suY1\":\"30L40\",\"suY2\":\"bre00bb\",\"suY3\":\"fbFGFre\",\"subChildA\":[{\"suba1\":\"3040\",\"suba2\":\"brebb\",\"suba3\":\"fbre\"},{\"suba1\":\"erbrrt\",\"suba2\":\"be4\",\"suba3\":\"5yh5\"},{\"suba1\":\"g445h\",\"suba2\":\"43th\",\"suba3\":\"r5yj4\"}]}},{\"a1\":\"s\",\"a2\":\"D\",\"a3\":\"F\"},{\"a1\":\"Q\",\"a2\":\"R\",\"a3\":\"T\"}],\"key5\":[{\"b1\":\"11\",\"b2\":\"21\",\"b3\":\"31\"},{\"b1\":\"3er\",\"b2\":\"3gt\",\"b3\":\"y7u\"},{\"b1\":\"H\",\"b2\":\"Y\",\"b3\":\"R\"}],\"key6\":\"uuid\",\"key7\":{\"vx1\":\"HwH\",\"vx2\":\"YrY\",\"vx3\":\"ReR\"}}";
        //String str = "{\n\ta:[1,-23333,-0.3.,0.17,5.2,\"\\u82B1\\u6979~\"],\n\tb:[\"a\tbc\",\"12  3\",\"4,5\\\"6\",{\n\t\t\t\t\tx:1,\n\t\t\t\t\ty:\"cc\\ncc\"\n\t\t\t\t},4.56],\n\t\"text\":\"I'm OK~\",\n\t\"1-2\":234,\n\tmybool:false,\n\tmynull:null,\n\tmyreal:true\n}\n";
        String str = "\"\\u82B1\\u6979~\\\"";
        //  System.out.println(str);
        JsonLex jl = new JsonLex(str);
        LeptType lt = null;

        while ((lt = jl.leptParseValue()) != LeptType.EOF) {
            // tk.getRealValue();
            // System.out.println(tk.getValue());
            System.out.println(lt);
        }
    }
}
