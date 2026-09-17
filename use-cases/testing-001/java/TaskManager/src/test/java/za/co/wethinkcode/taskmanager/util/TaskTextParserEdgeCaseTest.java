package za.co.wethinkcode.taskmanager.util;

import org.junit.jupiter.api.Test;
import za.co.wethinkcode.taskmanager.model.Task;
import za.co.wethinkcode.taskmanager.model.TaskPriority;

import static org.junit.jupiter.api.Assertions.*;

class TaskTextParserEdgeCaseTest {
    @Test
    void defaultsToMediumPriorityWhenNoPriorityMarkerExists() {
        Task task = TaskTextParser.parseTaskFromText("Buy milk");
        assertEquals(TaskPriority.MEDIUM, task.getPriority());
    }

    @Test
    void removesSupportedMarkersFromTitle() {
        Task task = TaskTextParser.parseTaskFromText("Finish report @work !urgent #tomorrow");

        assertEquals("Finish report", task.getTitle());
        assertEquals(TaskPriority.URGENT, task.getPriority());
        assertEquals(1, task.getTags().size());
        assertNotNull(task.getDueDate());
    }

    @Test
    void unknownDateMarkerDoesNotCreateADueDate() {
        Task task = TaskTextParser.parseTaskFromText("Buy milk #someday");
        assertNull(task.getDueDate());
    }
}
