package cn.suniper.mesh.discovery.exception;

/**
 * This exception will throw when a user attempts to delete a note which is not empty
 * such as: /configs/suniper/app/sbu1
 * try to delete /configs/suniper/app
 * @author Rao Mengnan
 *         on 2018/6/15.
 */
public class NodeNotEmptyException extends IllegalArgumentException {
    public NodeNotEmptyException(String s) {
        super(s);
    }
}
