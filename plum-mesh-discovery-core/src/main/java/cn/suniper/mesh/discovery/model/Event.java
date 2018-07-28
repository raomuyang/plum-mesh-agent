package cn.suniper.mesh.discovery.model;

/**
 * Node changes are divided into two categories: update and delete.
 * <p>
 * Update: create or update node in the kv store
 * <p>
 * Delete: a node deleted by the kv store
 *
 * @author Rao Mengnan
 *         on 2018/6/10.
 */
public enum Event {
    UPDATE,
    DELETE,
    UNRECOGNIZED
}
