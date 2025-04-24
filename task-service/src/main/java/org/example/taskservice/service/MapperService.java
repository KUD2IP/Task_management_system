package org.example.taskservice.service;

import lombok.extern.slf4j.Slf4j;
import org.example.taskservice.model.dto.request.CommentRequest;
import org.example.taskservice.model.dto.response.CommentResponse;
import org.example.taskservice.model.dto.request.TaskRequest;
import org.example.taskservice.model.dto.response.TaskResponse;
import org.example.taskservice.model.entity.Comment;
import org.example.taskservice.model.entity.Task;
import org.example.taskservice.model.entity.User;
import org.example.taskservice.model.entity.enums.TaskPriority;
import org.example.taskservice.model.entity.enums.TaskStatus;
import org.example.taskservice.exeception.InvalidCommentDataException;
import org.example.taskservice.exeception.InvalidTaskDataException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
public class MapperService {

    /**
     * Метод для преобразования задачи из DTO в сущность Task
     *
     * @param taskRequest - DTO задачи
     * @param oldTask        - существующая задача
     * @return - сущность Task
     */
    public Task mapToTask(TaskRequest taskRequest, Task oldTask) {

        // Если поле не передано, оставляем значение из существующей задачи
        String name = (taskRequest.getName() != null && !taskRequest.getName().isEmpty())
                ? taskRequest.getName()
                : oldTask.getName(); // оставляем старое значение

        String description = (taskRequest.getDescription() != null && !taskRequest.getDescription().isEmpty())
                ? taskRequest.getDescription()
                : oldTask.getDescription(); // оставляем старое значение

        TaskStatus status = taskRequest.getStatus();
        // Если статус не передан, оставляем существующий или по умолчанию
        if (taskRequest.getStatus() == null) {
            status = (oldTask.getStatus() != null)
                    ? oldTask.getStatus()
                    : TaskStatus.IN_WAITING;
        }

        TaskPriority priority = taskRequest.getPriority();

        if (taskRequest.getPriority() == null && oldTask.getPriority() == null) {
            throw new InvalidTaskDataException("Priority cannot be null");
        }

        log.info("Task name: {}, description: {}, status: {}, priority: {}", name, description, status, priority);
        // Создаем и возвращаем обновленную задачу
        return Task.builder()
                .id(oldTask.getId())
                .name(name)
                .description(description)
                .status(status)
                .priority(priority)
                .author(oldTask.getAuthor())
                .build();
    }


    /**
     * Метод для преобразования комментария из DTO в сущность Comment
     *
     * @param commentRequest - DTO комментария
     * @return - сущность Comment с текстом комментария
     */
    public Comment mapToComment(CommentRequest commentRequest) {

        // Если поле не передано, выбрасываем исключение
        if(commentRequest.getContent() == null || commentRequest.getContent().isEmpty()) {
            throw new InvalidCommentDataException("Content cannot be null or empty");
        }

        // Создаем и возвращаем обновленный комментарий
        return Comment.builder()
                .content(commentRequest.getContent())
                .build();
    }

    /**
     * Преобразование сущности Task в DTO TaskResponseDto.
     *
     * @param task задача
     * @return TaskResponseDto
     */
    public TaskResponse convertToTaskResponseDto(Task task) {
        return TaskResponse.builder()
                .id(task.getId()) // Идентификатор задачи
                .name(task.getName()) // Название задачи
                .description(task.getDescription()) // Описание задачи
                .status(task.getStatus()) // Статус задачи
                .priority(task.getPriority())  // Приоритет задачи
                .authorId(task.getAuthor().getId()) // ID автора задачи
                .authorName(task.getAuthor().getName()) // Имя автора задачи
                .executorId(task.getExecutors() != null
                        ? task.getExecutors().stream().map(User::getId).collect(Collectors.toSet())
                        : null) // ID исполнителя
                .executorName(task.getExecutors() != null
                        ? task.getExecutors().stream().map(User::getName).collect(Collectors.toSet())
                        : null) // Имя исполнителя
                .comments(task.getComments().stream().map(this::convertToCommentResponseDto).collect(Collectors.toSet())) // Комментарии
                .build();
    }


    /**
     * Преобразование сущности Comment в DTO CommentResponseDto.
     *
     * @param comment комментарий
     * @return CommentResponseDto
     */
    public CommentResponse convertToCommentResponseDto(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId()) // Идентификатор комментария
                .content(comment.getContent()) // Текст комментария
                .authorId(comment.getAuthor().getId()) // ID автора комментария
                .authorName(comment.getAuthor().getName()) // Имя автора комментария
                .build();
    }
}
