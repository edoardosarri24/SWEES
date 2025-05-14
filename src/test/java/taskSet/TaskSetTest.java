package taskSet;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class TaskSetTest {

    private Chunk chunk;

    @Before
    public void setUp() {
        this.chunk = new Chunk(0, Duration.ofSeconds(1));
    }
    
    @Test
    public void constructor() {
        Task task0 = new Task(
            Duration.ofSeconds(10),
            Duration.ofSeconds(10),
            List.of(this.chunk));
        Task task1 = new Task(
            Duration.ofSeconds(10),
            Duration.ofSeconds(10),
            List.of(this.chunk));
        TaskSet taskSet = new TaskSet(Set.of(task0, task1));
        assertThat(taskSet.getTasks())
            .containsExactlyInAnyOrder(task0, task1);
    }

}