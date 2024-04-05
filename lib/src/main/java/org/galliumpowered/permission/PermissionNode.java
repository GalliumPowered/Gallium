package org.galliumpowered.permission;


import javax.annotation.Nullable;
import java.util.ArrayList;

public class PermissionNode {

    private ArrayList<PermissionNode> children;
    private String name;
    private PermissionNode parent;

    public PermissionNode(String name) { // This will create a node which is a parent itself.
        this.children = new ArrayList<>();
        this.name = name;
    }

    public PermissionNode(String name, PermissionNode parent) {
        this.children = new ArrayList<>();
        this.name = name;
        this.parent = parent;
    }

    /**
     * Get all children of this permission node.
     * These are separated as such: parent.child1.child2 ... etc.
     * @return Permission node children
     */
    public ArrayList<PermissionNode> getChildren() {
        return children;
    }
    /**
     * Return the parent of this permission node
     * @return Permission node parent, null if none is present.
     */
    public @Nullable PermissionNode getParent() {
        return parent;
    }

    /**
     * Returns a path for the permission node
     * i.e: parent.child.child1
     * @return Node path
     */
    public String getPath() {
        if (parent == null) {
            // this is itself a parent, so just return the name.
            return name;
        } else {
            // this has a parent so we should prepend that path
            return parent.getPath() + name;
        }
    }

}
