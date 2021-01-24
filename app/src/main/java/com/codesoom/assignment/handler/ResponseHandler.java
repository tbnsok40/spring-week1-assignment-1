package com.codesoom.assignment.handler;

import com.codesoom.assignment.HttpMethod;
import com.codesoom.assignment.HttpStatusCode;
import com.codesoom.assignment.ResponseHandlingException;
import com.codesoom.assignment.helper.TaskParser;
import com.codesoom.assignment.model.Task;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Handle responses content by requests.
 * TODO: if possible, change switch statement to hashmap.
 */
public class ResponseHandler {
    TaskParser taskParser = new TaskParser();

    public String handle(String method, String path, List<Task> tasks, String body) throws IOException, ResponseHandlingException {
        if (path.equals("/") && HttpMethod.valueOf(method).equals(HttpMethod.HEAD)) {
            return "";
        }

        // check wrong path
        if (!path.matches("^/tasks(?:/[0-9]+)?$")) {
            throw new ResponseHandlingException(HttpStatusCode.NOT_FOUND);
        }

        Long taskId = extractTaskId(path);

        switch (HttpMethod.valueOf(method)) {
            case GET:
                // fetch task list
                if (path.equals("/tasks")) {
                    return taskParser.tasksToJSON(tasks);
                }

                // fetch a task
                Task selectedTask = taskParser.getTask(tasks, taskId);
                // TODO: getTask에서 아래의 로직을 처리할 수 있도록 함
                if (selectedTask.getId() == null) {
                    throw new ResponseHandlingException(HttpStatusCode.NOT_FOUND);
                }

                return taskParser.taskToJSON(selectedTask);

            case POST:
                if (body.isBlank()) { break; }

                Task newTask = taskParser.toTask(body, (long) (tasks.size() + 1));
                tasks.add(newTask);
                return taskParser.taskToJSON(tasks.get(tasks.size() - 1));

            case PUT:
            case PATCH:
                if (body.isBlank()) { break; }

                Task editableTask = taskParser.getTask(tasks, taskId);
                // TODO: getTask에서 아래의 로직을 처리할 수 있도록 함
                if (editableTask.getId() == null) {
                    throw new ResponseHandlingException(HttpStatusCode.NOT_FOUND);
                }

                tasks.set(tasks.indexOf(editableTask), taskParser.toTask(body, editableTask.getId()));
                return taskParser.taskToJSON(taskParser.getTask(tasks, taskId));

            case DELETE:
                if (taskParser.getTask(tasks, taskId).getId() == null) {
                    throw new ResponseHandlingException(HttpStatusCode.NOT_FOUND);
                }

                tasks.removeIf(task -> Objects.equals(taskId, task.getId()));
                return "";

            default:
                throw new ResponseHandlingException(HttpStatusCode.METHOD_NOT_ALLOWED);
        }

        throw new ResponseHandlingException(HttpStatusCode.NOT_FOUND);
    }

    @Nullable
    private Long extractTaskId(String path) {
        String[] splitPath = path.split("/");

        // return Longs.tryParse(splitPath[splitPath.length - 1])으로 대체 가능
        if (splitPath.length > 0 && splitPath[splitPath.length - 1].matches("^[0-9]+$")) {
            return Long.valueOf(splitPath[splitPath.length - 1]);
        }

        return null;
    }
}
