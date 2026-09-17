package za.co.wethinkcode.taskmanager.util;

import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskStatus;

import java.util.*;

public class TaskMergeService {

    /**
     * Merge two task lists with conflict resolution.
     */
    public MergeResult mergeTaskLists(Map<String, Task> localTasks, Map<String, Task> remoteTasks) {
        Objects.requireNonNull(localTasks, "localTasks");
        Objects.requireNonNull(remoteTasks, "remoteTasks");

        Map<String, Task> mergedTasks = new LinkedHashMap<>();
        Map<String, Task> toCreateRemote = new LinkedHashMap<>();
        Map<String, Task> toUpdateRemote = new LinkedHashMap<>();
        Map<String, Task> toCreateLocal = new LinkedHashMap<>();
        Map<String, Task> toUpdateLocal = new LinkedHashMap<>();

        Set<String> allTaskIds = new LinkedHashSet<>();
        allTaskIds.addAll(localTasks.keySet());
        allTaskIds.addAll(remoteTasks.keySet());

        for (String taskId : allTaskIds) {
            Task localTask = localTasks.get(taskId);
            Task remoteTask = remoteTasks.get(taskId);

            if (localTask != null && remoteTask == null) {
                mergedTasks.put(taskId, localTask);
                toCreateRemote.put(taskId, localTask);
            } else if (localTask == null && remoteTask != null) {
                mergedTasks.put(taskId, remoteTask);
                toCreateLocal.put(taskId, remoteTask);
            } else {
                ConflictResolution resolution = resolveTaskConflict(localTask, remoteTask);
                Task mergedTask = resolution.getMergedTask();
                mergedTasks.put(taskId, mergedTask);

                if (resolution.isShouldUpdateLocal()) {
                    toUpdateLocal.put(taskId, mergedTask);
                }
                if (resolution.isShouldUpdateRemote()) {
                    toUpdateRemote.put(taskId, mergedTask);
                }
            }
        }

        return new MergeResult(mergedTasks, toCreateRemote, toUpdateRemote, toCreateLocal, toUpdateLocal);
    }

    private ConflictResolution resolveTaskConflict(Task localTask, Task remoteTask) {
        Task mergedTask = copyTask(localTask);
        boolean shouldUpdateLocal = false;
        boolean shouldUpdateRemote = false;

        if (remoteTask.getUpdatedAt().isAfter(localTask.getUpdatedAt())) {
            mergedTask.setTitle(remoteTask.getTitle());
            mergedTask.setDescription(remoteTask.getDescription());
            mergedTask.setPriority(remoteTask.getPriority());
            mergedTask.setDueDate(remoteTask.getDueDate());
            shouldUpdateLocal = true;
        } else {
            shouldUpdateRemote = true;
        }

        if (remoteTask.getStatus() == TaskStatus.DONE && localTask.getStatus() != TaskStatus.DONE) {
            mergedTask.setStatus(TaskStatus.DONE);
            mergedTask.setCompletedAt(remoteTask.getCompletedAt());
            shouldUpdateLocal = true;
        } else if (localTask.getStatus() == TaskStatus.DONE && remoteTask.getStatus() != TaskStatus.DONE) {
            shouldUpdateRemote = true;
        } else if (remoteTask.getStatus() != localTask.getStatus()) {
            if (remoteTask.getUpdatedAt().isAfter(localTask.getUpdatedAt())) {
                mergedTask.setStatus(remoteTask.getStatus());
                shouldUpdateLocal = true;
            } else {
                shouldUpdateRemote = true;
            }
        }

        Set<String> allTags = new LinkedHashSet<>(localTask.getTags());
        allTags.addAll(remoteTask.getTags());
        mergedTask.setTags(new ArrayList<>(allTags));

        if (!new HashSet<>(mergedTask.getTags()).equals(new HashSet<>(localTask.getTags()))) {
            shouldUpdateLocal = true;
        }
        if (!new HashSet<>(mergedTask.getTags()).equals(new HashSet<>(remoteTask.getTags()))) {
            shouldUpdateRemote = true;
        }

        mergedTask.setUpdatedAt(
                localTask.getUpdatedAt().isAfter(remoteTask.getUpdatedAt())
                        ? localTask.getUpdatedAt() : remoteTask.getUpdatedAt());

        return new ConflictResolution(mergedTask, shouldUpdateLocal, shouldUpdateRemote);
    }

    private Task copyTask(Task original) {
        Task copy = new Task(original.getTitle(), original.getDescription());
        copy.setId(original.getId());
        copy.setPriority(original.getPriority());
        copy.setStatus(original.getStatus());
        copy.setCreatedAt(original.getCreatedAt());
        copy.setUpdatedAt(original.getUpdatedAt());
        copy.setDueDate(original.getDueDate());
        copy.setCompletedAt(original.getCompletedAt());
        copy.setTags(new ArrayList<>(original.getTags()));
        return copy;
    }

    private static class ConflictResolution {
        private final Task mergedTask;
        private final boolean shouldUpdateLocal;
        private final boolean shouldUpdateRemote;

        ConflictResolution(Task mergedTask, boolean shouldUpdateLocal, boolean shouldUpdateRemote) {
            this.mergedTask = mergedTask;
            this.shouldUpdateLocal = shouldUpdateLocal;
            this.shouldUpdateRemote = shouldUpdateRemote;
        }

        Task getMergedTask() { return mergedTask; }
        boolean isShouldUpdateLocal() { return shouldUpdateLocal; }
        boolean isShouldUpdateRemote() { return shouldUpdateRemote; }
    }

    public static class MergeResult {
        private final Map<String, Task> mergedTasks;
        private final Map<String, Task> toCreateRemote;
        private final Map<String, Task> toUpdateRemote;
        private final Map<String, Task> toCreateLocal;
        private final Map<String, Task> toUpdateLocal;

        public MergeResult(Map<String, Task> mergedTasks, Map<String, Task> toCreateRemote,
                           Map<String, Task> toUpdateRemote, Map<String, Task> toCreateLocal,
                           Map<String, Task> toUpdateLocal) {
            this.mergedTasks = mergedTasks;
            this.toCreateRemote = toCreateRemote;
            this.toUpdateRemote = toUpdateRemote;
            this.toCreateLocal = toCreateLocal;
            this.toUpdateLocal = toUpdateLocal;
        }

        public Map<String, Task> getMergedTasks() { return mergedTasks; }
        public Map<String, Task> getToCreateRemote() { return toCreateRemote; }
        public Map<String, Task> getToUpdateRemote() { return toUpdateRemote; }
        public Map<String, Task> getToCreateLocal() { return toCreateLocal; }
        public Map<String, Task> getToUpdateLocal() { return toUpdateLocal; }
    }
}
