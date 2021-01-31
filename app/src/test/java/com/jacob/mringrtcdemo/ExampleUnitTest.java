package com.jacob.mringrtcdemo;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {

        byte[] bytes = DataUtil.obtainBigend8Bytes(Long.parseLong("2f4859f33667c23d", 16));
        StringBuilder builder = new StringBuilder();
        for (byte bb: bytes) {
            builder.append(String.format("%02x", bb));
        }
        System.out.println(builder.toString());
        System.out.println(Long.parseLong(builder.toString(), 16));
    }
}