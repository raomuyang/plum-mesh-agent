package cn.suniper.mesh.discovery.exception;

/**
 * This exception will throw when a user attempts to delete a note which is not empty
 * <p>
 * such as: /configs/suniper/app/sbu1
 * <p>
 * try to delete /configs/suniper/app
 * <p>
 * @author Rao Mengnan
 *         on 2018/6/15.
 */
public class NodeNotEmptyException extends IllegalArgumentException {
    public NodeNotEmptyException(String s) {
        super(s);
    }
}
