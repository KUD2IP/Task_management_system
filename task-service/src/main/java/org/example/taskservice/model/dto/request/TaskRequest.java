package org.example.taskservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.example.taskservice.model.entity.enums.TaskPriority;
import org.example.taskservice.model.entity.enums.TaskStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "TaskRequestDto", description = "DTO для создания задачи")
public class TaskRequest {

    @Schema(description = "Название задачи", example = "Implement new feature")
    private String name;

    @Schema(description = "Описание задачи", example = "Implement a new feature in the task management system")
    private String description;

    @Schema(description = "Статус задачи", allowableValues = {"IN_WAITING", "IN_PROGRESS", "DONE"})
    private TaskStatus status;

    @Schema(description = "Приоритет задачи", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private TaskPriority priority;
}
