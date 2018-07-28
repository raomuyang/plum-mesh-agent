package cn.suniper.mesh.discovery.exception;

import org.junit.Test;


/**
 * @author Rao Mengnan
 *         on 2018/7/28.
 */
public class NodeNotEmptyExceptionTest {

    @Test
    public void testCreate() {
        new NodeNotEmptyException(null);
    }

}