package resource;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import scheduler.RMScheduler;
import taskSet.Chunk;
import taskSet.Task;
import taskSet.TaskSet;

public class PriorityCeilingProtocolTest {

    private Chunk chunk;
    private Task task;
    private Resource resource;

    @Before
    public void setUp() {
        this. resource = new Resource();
        this.chunk = new Chunk(
            0,
            Duration.ofSeconds(3),
            List.of(this.resource));
        this.task = new Task(
            Duration.ofSeconds(5),
            Duration.ofSeconds(5),
            List.of(
                this.chunk,
                new Chunk(1, Duration.ofSeconds(1))));
    }

    @Test
    public void access() {
        Task task1 = new Task(
            Duration.ofSeconds(10),
            Duration.ofSeconds(10),
            List.of(
                new Chunk(2, Duration.ofSeconds(4), List.of(this.resource)),
                new Chunk(3, Duration.ofSeconds(1))));
        TaskSet taskSet = new TaskSet(Set.of(this.task, task1));
        RMScheduler scheduler = new RMScheduler(taskSet);
        PriorityCeilingProtocol protocol = new PriorityCeilingProtocol(taskSet);

        protocol.access(this.task, scheduler, this.chunk);
    }
    
}