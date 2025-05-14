package resource;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import scheduler.RMScheduler;
import taskSet.Chunk;
import taskSet.Task;
import taskSet.TaskSet;

public final class PriorityCeilingProtocol {

    private Map<Resource, Integer> ceiling;
    private List<Resource> busyResources;

    // CONSTRUCTOR
    public PriorityCeilingProtocol (TaskSet taskSet) {
        this.ceiling = new HashMap<>();
        this.busyResources = new LinkedList<>();
        for (Task task : taskSet.getTasks())
            for (Chunk chunk : task.getChunks())
                for (Resource resource : chunk.getResources())
                    this.ceiling.merge(resource, task.getNominalPriority(), Math::max);
    }

    // GETTER AND SETTER
    public int getCeilingValue (Resource resource) {
        return this.ceiling.get(resource);
    }

    // METHOD
    public boolean access(Task task, RMScheduler scheduler, Chunk chunk) {
        int maxCeiling = this.busyResources.stream()
            .filter(res -> !task.getResourcesAcquired().contains(res))
            .mapToInt(res -> this.ceiling.get(res))
            .max()
            .orElse(Integer.MIN_VALUE);
        if (task.getNominalPriority() <= maxCeiling) {
            scheduler.getBlockedTask().add(task);
            chunk.getResources().forEach(res -> res.getBlockedTasks().add(task));
            return false;
        } else {
            return true;
        }
    }

    public void progress (Chunk chunk, Task task) {
        List<Resource> resources = chunk.getResources();
        int MaxDinamicPriorityBlockedtask = resources.stream()
            .flatMap(res -> res.getBlockedTasks().stream())
            .mapToInt(Task::getNominalPriority)
            .max()
            .orElse(Integer.MIN_VALUE);
        task.setDinamicPriority(Math.max(
            task.getNominalPriority(),
            MaxDinamicPriorityBlockedtask));
        this.busyResources.addAll(resources);
        task.getResourcesAcquired().addAll(resources);
    }

    public void release(Chunk chunk, RMScheduler scheduler, TreeSet<Task> orderedTasks, Task task) {
        for (Resource resource : chunk.getResources()) {
            List<Task> tasksBlockedOnResource = resource.getBlockedTasks();
            if (tasksBlockedOnResource.isEmpty())
                continue;
            Task taskMaxPriority = tasksBlockedOnResource.stream()
                .max(Comparator.comparingInt(Task::getNominalPriority))
                .get();
            scheduler.getBlockedTask().remove(taskMaxPriority);
            orderedTasks.add(taskMaxPriority);
            tasksBlockedOnResource.remove(taskMaxPriority);
            task.getResourcesAcquired().remove(resource);
        }
        task.getResourcesAcquired().stream()
            .flatMap(res -> res.getBlockedTasks().stream())
            .mapToInt(Task::getNominalPriority)
            .max()
            .ifPresentOrElse(
                task::setDinamicPriority,
                () -> task.setDinamicPriority(task.getNominalPriority()));
    }

}