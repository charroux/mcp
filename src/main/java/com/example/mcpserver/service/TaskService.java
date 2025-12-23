package com.example.mcpserver.service;

import com.example.mcpserver.model.Task;
import com.example.mcpserver.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional
    public Task createTask(Task task) {
        log.info("Creating new task: {}", task.getTitle());
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<Task> getAllTasks() {
        log.info("Retrieving all tasks");
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Task> getTaskById(Long id) {
        log.info("Retrieving task with id: {}", id);
        return taskRepository.findById(id);
    }

    @Transactional
    public Optional<Task> updateTask(Long id, Task taskDetails) {
        log.info("Updating task with id: {}", id);
        return taskRepository.findById(id)
                .map(task -> {
                    if (taskDetails.getTitle() != null) {
                        task.setTitle(taskDetails.getTitle());
                    }
                    if (taskDetails.getDescription() != null) {
                        task.setDescription(taskDetails.getDescription());
                    }
                    if (taskDetails.getStatus() != null) {
                        task.setStatus(taskDetails.getStatus());
                    }
                    if (taskDetails.getPriority() != null) {
                        task.setPriority(taskDetails.getPriority());
                    }
                    if (taskDetails.getDueDate() != null) {
                        task.setDueDate(taskDetails.getDueDate());
                    }
                    if (taskDetails.getTags() != null) {
                        task.setTags(taskDetails.getTags());
                    }
                    return taskRepository.save(task);
                });
    }

    @Transactional
    public boolean deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Task> searchTasks(String keyword) {
        log.info("Searching tasks with keyword: {}", keyword);
        return taskRepository.searchByKeyword(keyword);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        log.info("Retrieving tasks with status: {}", status);
        return taskRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByPriority(Task.TaskPriority priority) {
        log.info("Retrieving tasks with priority: {}", priority);
        return taskRepository.findByPriority(priority);
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksSortedByPriority() {
        log.info("Retrieving tasks sorted by priority");
        return taskRepository.findByOrderByPriorityDescCreatedAtDesc();
    }
}
