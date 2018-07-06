package cn.suniper.mesh.transport;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Rao Mengnan
 *         on 2018/7/6.
 */
public class TransportConfigKeyTest {
    @Test
    @SuppressWarnings("unchecked")
    public void testListConvert() throws Exception {

        String[] args = {"a", "b", "c"};
        List<String> list = (List<String>) TransportConfigKey.CHANNEL_PIPELINES.convert(args);
        assertEquals(list.size(), args.length);
        for (int i = 0; i < args.length; i++) {
            assertEquals(args[i], list.get(i));
        }

        List<String> list2 = (List<String>) TransportConfigKey.CHANNEL_PIPELINES.convert(list);
        assertEquals(list, list2);
    }

}