package com.fbudassi.neddy.config;

/**
 * Immutable class to serve as a wrapper to prioritize resources.
 *
 * @author juan
 */
public final class PriorityResource implements Comparable<PriorityResource> {

    private final String resource;
    private final int priority;

    /**
     * Create a new instance of a priority resource. The load priority is given
     * by the second parameter of this constructor. The lower the number the
     * higher priority.
     *
     * @param resource
     * @param priority
     */
    public PriorityResource(String resource, int priority) {
        this.resource = resource;
        this.priority = priority;
    }

    /**
     * Get the priority for this resource.
     *
     * @return
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Get the resource path.
     *
     * @return
     */
    public String getResource() {
        return resource;
    }

    @Override
    public String toString() {
        return resource;
    }

    /**
     * Compare one priority resource with other. It conforms to the comparator
     * protocol.
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(PriorityResource other) {

        if (other == null) {
            return -1;
        }

        Integer thisP = priority;
        Integer otherP = other.priority;

        return thisP.compareTo(otherP);
    }

    /**
     * Factory method for building a priority array with the priority number
     * matching the parameter index.
     *
     * @param resources
     * @return
     */
    public static PriorityResource[] build(String... resources) {
        PriorityResource[] ret = new PriorityResource[resources.length];

        int i = 0;
        for (String resource : resources) {
            ret[i] = new PriorityResource(resource, i);
            i++;
        }

        return ret;
    }
}
