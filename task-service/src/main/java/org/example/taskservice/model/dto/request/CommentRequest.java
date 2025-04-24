package org.example.taskservice.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO комментария")
public class CommentRequest {

    @Schema(description = "Текст комментария")
    private String content;
}
