package org.example.taskservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.taskservice.model.entity.enums.TaskPriority;
import org.example.taskservice.model.entity.enums.TaskStatus;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToMany
    @JoinTable(
        name = "task_executors",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> executors = new HashSet<>();

    @OneToMany(mappedBy = "task")
    private Set<Comment> comments;

}
