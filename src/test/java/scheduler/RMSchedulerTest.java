package scheduler;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import helper.ReflectionUtils;

import static org.assertj.core.api.Assertions.*;
import taskSet.Chunk;
import taskSet.Task;
import taskSet.TaskSet;

public class RMSchedulerTest {

    private Chunk chunk = new Chunk(0, Duration.ofSeconds(1));

    @Test
    public void assignPriority() {
        Task task0 = new Task(
            Duration.ofSeconds(10),
            Duration.ofSeconds(10),
            List.of(this.chunk));
        Task task1 = new Task(
            Duration.ofSeconds(5),
            Duration.ofSeconds(5),
            List.of(this.chunk));
        Task task2 = new Task(
            Duration.ofSeconds(15),
            Duration.ofSeconds(15),
            List.of(this.chunk));
        assertThat(task0.getNominalPriority())
            .isEqualTo((int) ReflectionUtils.getField(task0, "dinamicPriority"))
            .isZero();
        assertThat(task1.getNominalPriority())
            .isEqualTo((int) ReflectionUtils.getField(task1, "dinamicPriority"))
            .isZero();
        assertThat(task2.getNominalPriority())
            .isEqualTo((int) ReflectionUtils.getField(task2, "dinamicPriority"))
            .isZero();
        new RMScheduler(new TaskSet(Set.of(task0, task1, task2)));
        assertThat(task1.getNominalPriority())
            .isEqualTo((int) ReflectionUtils.getField(task1, "dinamicPriority"))
            .isEqualTo(5);
        assertThat(task0.getNominalPriority())
            .isEqualTo((int) ReflectionUtils.getField(task0, "dinamicPriority"))
            .isEqualTo(7);
        assertThat(task2.getNominalPriority())
            .isEqualTo((int) ReflectionUtils.getField(task2, "dinamicPriority"))
            .isEqualTo(9);
    }
    
}