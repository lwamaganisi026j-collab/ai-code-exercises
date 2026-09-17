package za.co.wethinkcode.taskmanager.util;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TaskMergeServiceEdgeCaseTest {
    @Test
    void localOnlyTaskIsScheduledForRemoteCreation() {
        Task local = new Task("Local task");

        TaskMergeService.MergeResult merged = new TaskMergeService().mergeTaskLists(
                Map.of(local.getId(), local), Map.of());

        assertSame(local, merged.getMergedTasks().get(local.getId()));
        assertTrue(merged.getToCreateRemote().containsKey(local.getId()));
    }

    @Test
    void remoteOnlyTaskIsScheduledForLocalCreation() {
        Task remote = new Task("Remote task");

        TaskMergeService.MergeResult merged = new TaskMergeService().mergeTaskLists(
                Map.of(), Map.of(remote.getId(), remote));

        assertSame(remote, merged.getMergedTasks().get(remote.getId()));
        assertTrue(merged.getToCreateLocal().containsKey(remote.getId()));
    }

    @Test
    void completedStatusWinsDuringConflict() {
        Task local = new Task("Task");
        Task remote = new Task("Task");
        remote.markAsDone();

        TaskMergeService.MergeResult merged = new TaskMergeService().mergeTaskLists(
                Map.of(local.getId(), local), Map.of(remote.getId(), remote));

        assertEquals(TaskStatus.DONE, merged.getMergedTasks().get(remote.getId()).getStatus());
    }
}
