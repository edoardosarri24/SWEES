package scheduler;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import helper.ReflectionUtils;
import taskSet.Chunk;
import taskSet.Task;
import taskSet.TaskSet;


import static org.assertj.core.api.Assertions.*;

public class RMSchedulerTest {

    private Chunk chunk;

    @Before
    public void setUp() {
        this.chunk = new Chunk(0, Duration.ofSeconds(1));
    }

    @Test
    public void constructorKO() {
        Task task = new Task(
            Duration.ofSeconds(10),
            Duration.ofSeconds(5),
            List.of(this.chunk));
        TaskSet taskSet = new TaskSet(Set.of(task));
        assertThatThrownBy(() -> new RMScheduler(taskSet))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Il task " + task.getId() + " non è puramente periocico: ha periodo PT10S e deadline PT5S");
    }

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



    

    @Test
    public void executeTasksForMaxAvailableTime() {

    }

    @Test
    public void checkDeadlinesAndResetTasks() {
        
    }

}