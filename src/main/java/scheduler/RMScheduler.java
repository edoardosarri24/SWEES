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
        Context context = new Context(this.taskSet);
        while (!context.eventsIsFinished()) {
            // prossimo evento dove fare i controllli
            Duration nextEvent = context.getNextEvent();
            Duration availableTime = context.calculateAvailableTime(nextEvent);
            logger.info("- Il tempo disponibile è: " + availableTime);

            // eseguo per al più il tempo a disposzione
            this.executeTasksForMaxAvailableTime(context, availableTime);

            // per ogni task il cui periodo è scaduto controllo se ha superato la deadline
            context.setCurrentTime(nextEvent);
            this.checkDeadlinesAndResetTasks(context, this.taskSet);
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

    private void executeTasksForMaxAvailableTime(Context context, Duration availableTime) {
        Duration executedTime;
        while (availableTime.isPositive() && !context.noTasksToExecute()) {
            Task currentTask = context.getHighestPriorityTask();
            executedTime = currentTask.execute(availableTime, context.getOrderedTasks(), this);
            availableTime = availableTime.minus(executedTime);
            if (!currentTask.getIsExecuted() && !this.blockedTask.contains(currentTask))
                context.addIncompleteTasks(currentTask);
        }
    }

    private void checkDeadlinesAndResetTasks(Context context, TaskSet taskSet) throws DeadlineMissedException {
        for (Task task : this.taskSet.getTasks()) {
            if (context.isPeriodElapsed(task)) {
                logger.info("- Al tempo " + context.getCurrentTime() + " il task controllato e resettato: " + task.getId());
                task.checkAndReset();
                context.addIncompleteTasks(task);
                logger.info("I task nella coda sono: " + 
                    context.getOrderedTasks().stream()
                        .map(Task::getId)
                        .collect(Collectors.toList()));
            }
        }
    }

    private static class Context {

        final TreeSet<Task> orderedTasks;
        final List<Duration> events;
        Duration currentTime = Duration.ZERO;
        
        Context(TaskSet taskSet) {
            this.orderedTasks = new TreeSet<>(Comparator.comparingInt(Task::getDinamicPriority));
            taskSet.getTasks().forEach(orderedTasks::add);
            List<Duration> periods = this.orderedTasks.stream()
                .map(Task::getPeriod)
                .collect(Collectors.toList());
            this.events = new LinkedList<>(Multiple.generateMultiplesUpToLCM(periods));
        }
    
        boolean eventsIsFinished() {
            return this.events.isEmpty();
        }
        Duration getNextEvent() {
            return this.events.removeFirst();
        }
        Duration calculateAvailableTime(Duration nextEvent) {
            return nextEvent.minus(this.currentTime);
        }
        boolean noTasksToExecute() {
            return this.orderedTasks.isEmpty();
        }
        Task getHighestPriorityTask() {
            return this.orderedTasks.pollFirst();
        }
        TreeSet<Task> getOrderedTasks() {
            return this.orderedTasks;
        }
        void addIncompleteTasks(Task task) {
            this.orderedTasks.add(task);
        }
        void setCurrentTime(Duration time) {
            this.currentTime = time;
        }
        boolean isPeriodElapsed(Task task) {
            return this.currentTime.toMillis() % task.getPeriod().toMillis() == 0;
        }
        Duration getCurrentTime() {
            return this.currentTime;
        }
    }

}