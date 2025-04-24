package org.example.taskservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.taskservice.model.dto.request.CommentRequest;
import org.example.taskservice.model.dto.response.CommentResponse;
import org.example.taskservice.model.dto.request.TaskRequest;
import org.example.taskservice.model.dto.response.TaskResponse;
import org.example.taskservice.model.entity.enums.TaskPriority;
import org.example.taskservice.model.entity.enums.TaskStatus;
import org.example.taskservice.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/tasks")
@Slf4j
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Метод для создания задачи.
     * Доступно только администратору
     *
     * @param taskRequest - DTO задачи
     * @param request - запрос
     * @return - ID созданной задачи
     * @throws IOException - исключение ввода-вывода
     */
    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> createTask(
            @RequestBody TaskRequest taskRequest,
            HttpServletRequest request
    ) throws IOException {
        log.info("Received request to create task: {}", taskRequest);

        Long taskId = taskService.createTask(taskRequest, request);

        log.info("Task created successfully with ID: {}", taskId);

        return ResponseEntity.ok(taskId); // Возвращаем ID созданной задачи с кодом 200 OK
    }


    /**
     * Метод для обновления задачи.
     * Доступно только администратору
     *
     * @param taskId - идентификатор задачи
     * @param taskRequest - DTO задачи
     * @return - ID обновленной задачи
     */
    @PutMapping("/admin/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskRequest taskRequest) {

        log.info("Updating task {}: {}", taskId, taskRequest);
        Long updatedTaskId = taskService.updateTask(taskId, taskRequest);
        log.info("Task updated successfully with ID: {}", updatedTaskId);

        return ResponseEntity.ok(updatedTaskId);
    }


    /**
     * Метод для обновления статуса задачи.
     * Доступно администратору и исполнителю
     *
     * @param taskId - идентификатор задачи
     * @param status - новый статус
     * @return - идентификатор обновленной задачи
     */
    @PatchMapping("/executors/{taskId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_EXECUTOR')")
    public ResponseEntity<Long> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam TaskStatus status,
            HttpServletRequest request) throws IOException {

        log.info("Updating status: {} for task: {}", status, taskId);
        Long updatedTaskId = taskService.updateTaskStatus(taskId, status, request);
        log.info("Task status updated successfully with ID: {}", updatedTaskId);

        return ResponseEntity.ok(updatedTaskId);
    }


    /**
     * Метод для обновления приоритета задачи.
     * Доступно только администратору
     *
     * @param taskId - идентификатор задачи
     * @param priority - новый приоритет
     * @return -
     */
    @PatchMapping("/admin/{taskId}/priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> updateTaskPriority(
            @PathVariable Long taskId,
            @RequestParam TaskPriority priority) {

        log.info("Updating priority: {} for task: {}", priority, taskId);
        Long updatedTaskId = taskService.updateTaskPriority(taskId, priority);
        log.info("Task priority updated successfully with ID: {}", updatedTaskId);

        return ResponseEntity.ok(updatedTaskId);
    }


    // Назначение исполнителя задачи - доступно только администратору
    @PatchMapping("/admin/executor/{userId}/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Long> updateTaskExecutor(
            @PathVariable Long taskId,
            @PathVariable Long userId,
            HttpServletRequest request) {
        Long task = taskService.updateTaskExecutor(taskId, userId, request);
        return ResponseEntity.ok(task);
    }


    /**
     * Метод для удаления задачи.
     * Доступно только администратору
     *
     * @param taskId - идентификатор задачи
     * @return - сообщение об успешном удалении
     */
    @DeleteMapping("/admin/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok("Task deleted successfully");
    }

    /**
     * Метод для добавления комментария к задаче.
     * Доступно только администратору и исполнителю
     *
     * @param taskId - идентификатор задачи
     * @param commentRequest - данные комментария
     * @param request - HTTP-запрос (токен пользователя)
     * @return - сообщение об успешном добавлении
     * @throws IOException - исключение
     */
    @PatchMapping("/executors/{taskId}/comment")
    @PreAuthorize("hasAnyRole('ADMIN', 'EXECUTOR')")
    public ResponseEntity<String> addComment(
            @PathVariable Long taskId,
            @RequestBody CommentRequest commentRequest,
            HttpServletRequest request
            ) throws IOException {

        taskService.addComment(taskId, commentRequest, request);

        return ResponseEntity.ok("Comment added");
    }


    /**
     * Получение задач по автору с фильтрацией и пагинацией.
     *
     * @param userId - идентификатор пользователя
     * @param page - номер страницы
     * @param size - размер страницы
     * @param status - статус задачи
     * @param priority - приоритет задачи
     * @return - задачи по автору
     */
    @GetMapping("/author/{userId}")
    public ResponseEntity<Page<TaskResponse>> getTasksByAuthor(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority) {

        log.info("Fetching tasks for author {}", userId);
        // Поиск задач по идентификатору пользователя
        Page<TaskResponse> tasks = taskService.getTasksByAuthor(userId, page, size, status, priority);
        return ResponseEntity.ok(tasks);
    }


    /**
     * Получение задачи по идентификатору
     *
     * @param taskId - идентификатор задачи
     * @return - задача
     */
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> findTaskById(@PathVariable Long taskId) {

        log.info("Fetching task with ID: {}", taskId);
        // Поиск задачи по идентификатору
        TaskResponse task = taskService.getTaskById(taskId);

        log.info("Task found: {} with ID: {}", task.getName(), taskId);
        return ResponseEntity.ok(task);
    }


    /**
     * Получение всех задач с фильтрацией и пагинацией.
     * Доступно всем аутентифицированным пользователям
     *
     * @param page - номер страницы
     * @param size - размер страницы
     * @param status - статус задачи
     * @param priority - приоритет задачи
     * @return - все задачи
     */
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> findAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority
    ) {

        log.info("Fetching all tasks");
        Page<TaskResponse> tasks = taskService.getAllTasks(status, priority, page, size);
        return ResponseEntity.ok(tasks);
    }


    /**
     * Получение задач по исполнителю с фильтрацией и пагинацией.
     *
     * @param userId - идентификатор исполнителя
     * @param page - номер страницы
     * @param size - размер страницы
     * @param status - статус задачи
     * @param priority - приоритет задачи
     * @return - задачи по исполнителю
     */
    @GetMapping("/executor/{userId}")
    public ResponseEntity<Page<TaskResponse>> findTasksByExecutorId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority
    ) {

        log.info("Fetching tasks for executor {}", userId);
        Page<TaskResponse> tasks = taskService.getTasksByExecutorId(userId, page, size, status, priority);
        return ResponseEntity.ok(tasks);
    }


    /**
     * Получение комментариев по задаче
     *
     * @param taskId - идентификатор задачи
     * @return - список комментариев
     */
    @GetMapping("/comment/{taskId}")
    public ResponseEntity<Page<CommentResponse>> findCommentsByTaskId(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching comments for task {}", taskId);
        Page<CommentResponse> comments = taskService.getCommentsByTaskId(taskId, page, size);
        return ResponseEntity.ok(comments);
    }
}

