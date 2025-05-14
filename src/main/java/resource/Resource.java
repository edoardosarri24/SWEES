package resource;

import java.util.LinkedList;
import java.util.List;

import taskSet.Task;

public final class Resource {

    private List<Task> blockedTasks = new LinkedList<>();

    public List<Task> getBlockedTasks() {
        return this.blockedTasks;
    }
    
}