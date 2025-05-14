package scheduler;

import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import exeptions.DeadlineMissedException;
import exeptions.PurelyPeriodicException;
import resource.PriorityCeilingProtocol;
import taskSet.Task;
import taskSet.TaskSet;
import utils.Multiple;
import utils.logger.LoggingConfig;

public final class RMScheduler {

    private TaskSet taskSet;
    private PriorityCeilingProtocol resProtocol;
    private List<Task> blockedTask = new LinkedList<>();
    private static final Logger logger = LoggingConfig.getLogger();

    // CONSTRUCTOR
    public RMScheduler(TaskSet taskSet) {
        this(taskSet, null);
    }

    public RMScheduler(TaskSet taskSet, PriorityCeilingProtocol resProtocol) {
        try {
            taskSet.purelyPeriodicCheck();
        } catch (PurelyPeriodicException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        this.taskSet = taskSet;
        this.assignPriority();
        this.resProtocol = resProtocol;
    }

    // GETTER AND SETTER
    public PriorityCeilingProtocol getResProtocol() {
        return this.resProtocol;
    }

    public List<Task> getBlockedTask() {
        return this.blockedTask;
    }

    // METHOD
    public void schedule() throws DeadlineMissedException {
        // Structures
        TreeSet<Task> orderedTasks = new TreeSet<>(Comparator.comparingInt(Task::getDinamicPriority));
        this.taskSet.getTasks().forEach(orderedTasks::add);
        List<Duration> periods = orderedTasks.stream()
            .map(Task::getPeriod)
            .collect(Collectors.toList());
        List<Duration> events = new LinkedList<>(Multiple.generateMultiplesUpToLCM(periods));
        Duration currentTime = Duration.ZERO;

        // Execution
        while (!events.isEmpty()) {
            // prossimo evento dove fare i controllli
            Duration nextEvent = events.removeFirst();
            Duration availableTime = nextEvent.minus(currentTime);
            logger.info("- Il tempo disponibile è: " + availableTime);

            // eseguo per al più il tempo a disposzione
            this.executeTasksForMaxAvailableTime(orderedTasks, availableTime);

            // per ogni task il cui periodo è scaduto controllo se ha superato la deadline
            currentTime = nextEvent;
            this.checkDeadlinesAndResetTasks(orderedTasks, currentTime, this.taskSet);
        }
        logger.info("La generazione di tracce è avvenuta con successo!");
    }

    // HELPER
    private void assignPriority() {
        List<Task> sortedByPeriod = this.taskSet.getTasks().stream()
            .sorted(Comparator.comparing(Task::getPeriod))
            .collect(Collectors.toList());
        IntStream.range(0, sortedByPeriod.size())
            .forEach(i -> {
                int priority = 5 + i * 2;
                Task task = sortedByPeriod.get(i);
                task.setDinamicPriority(priority);
                task.setNominalPriority(priority);
            });
    }

    private void executeTasksForMaxAvailableTime(TreeSet<Task> orderedTasks, Duration availableTime) {
        Duration executedTime;
        while (availableTime.isPositive() && !orderedTasks.isEmpty()) {
            Task currentTask = orderedTasks.pollFirst();
            executedTime = currentTask.execute(availableTime, orderedTasks, this);
            availableTime = availableTime.minus(executedTime);
            if (!currentTask.getIsExecuted() && !this.blockedTask.contains(currentTask))
                orderedTasks.add(currentTask);
        }
    }

    private void checkDeadlinesAndResetTasks(TreeSet<Task> orderedTasks, Duration currentTime, TaskSet taskSet) throws DeadlineMissedException {
        for (Task task : this.taskSet.getTasks()) {
            if (currentTime.toMillis() % task.getPeriod().toMillis() == 0) {
                logger.info("- Al tempo " + currentTime + " il task controllato e resettato: " + task.getId());
                task.checkAndReset();
                orderedTasks.add(task);
                logger.info("I task nella coda sono: " + 
                    orderedTasks.stream()
                        .map(Task::getId)
                        .collect(Collectors.toList()));
            }
        }
    }

}