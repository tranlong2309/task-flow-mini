# -*- coding: utf-8 -*-
import os

base_dir = r"c:\Users\SKYLAP.VN\Documents\dev\task-flow-mini\backend\src\main\java\com\taskflow"

def patch_port():
    path = os.path.join(base_dir, r"domain\repository\TaskRepositoryPort.java")
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "List<Task> findTasksForReminders" not in content:
        content = content.replace("Task save(Task task);", "Task save(Task task);\n    java.util.List<Task> findTasksForReminders(java.time.Instant upTo);")
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Patched TaskRepositoryPort")

def patch_spring_data():
    path = os.path.join(base_dir, r"infrastructure\persistence\repository\SpringDataTaskRepository.java")
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "findTasksForReminders" not in content:
        method = '''
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TaskEntity t WHERE t.deletedAt IS NULL AND t.completedAt IS NULL AND (t.isBlocked IS NULL OR t.isBlocked = false) AND t.dueDate IS NOT NULL AND t.dueDate <= :upTo")
    java.util.List<TaskEntity> findTasksForReminders(@org.springframework.data.repository.query.Param("upTo") java.time.Instant upTo);
'''
        content = content.replace("}", method + "\n}")
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Patched SpringDataTaskRepository")

def patch_adapter():
    path = os.path.join(base_dir, r"infrastructure\persistence\repository\TaskRepositoryAdapter.java")
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    if "findTasksForReminders" not in content:
        method = '''
    @Override
    public java.util.List<Task> findTasksForReminders(java.time.Instant upTo) {
        return repository.findTasksForReminders(upTo).stream().map(this::toDomain).collect(java.util.stream.Collectors.toList());
    }
'''
        content = content.replace("public Task save(Task task)", method + "\n    public Task save(Task task)")
        with open(path, 'w', encoding='utf-8') as f:
            f.write(content)
        print("Patched TaskRepositoryAdapter")

def patch_scheduler():
    path = os.path.join(base_dir, r"infrastructure\scheduler\TaskReminderScheduler.java")
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # TaskReminderScheduler injects SpringDataTaskRepository directly currently. We should change it to use TaskRepositoryPort if we can, 
    # but the requirement says "Thêm query trong TaskRepositoryPort... C?p nh?t scheduler g?i query này".
    # Currently TaskReminderScheduler has: private final SpringDataTaskRepository taskRepository;
    # We will change it to use TaskRepositoryPort!
    
    content = content.replace("private final SpringDataTaskRepository taskRepository;", "private final com.taskflow.domain.repository.TaskRepositoryPort taskRepositoryPort;")
    content = content.replace("public TaskReminderScheduler(SpringDataTaskRepository taskRepository,", "public TaskReminderScheduler(com.taskflow.domain.repository.TaskRepositoryPort taskRepositoryPort,")
    content = content.replace("this.taskRepository = taskRepository;", "this.taskRepositoryPort = taskRepositoryPort;")
    
    old_run = '''List<com.taskflow.infrastructure.persistence.entity.TaskEntity> entities = taskRepository.findAll().stream()
                .filter(t -> t.getDeletedAt() == null && t.getCompletedAt() == null && !Boolean.TRUE.equals(t.getIsBlocked()) && t.getDueDate() != null)
                .collect(Collectors.toList());

        Instant now = Instant.now();

        for (com.taskflow.infrastructure.persistence.entity.TaskEntity entity : entities) {
            Task task = toDomain(entity);'''
    
    new_run = '''Instant now = Instant.now();
        java.time.Instant upTo = now.plus(8, ChronoUnit.DAYS); // Fetch up to 8 days to cover the 7 days left reminder + 24h requirement
        List<Task> tasks = taskRepositoryPort.findTasksForReminders(upTo);

        for (Task task : tasks) {'''
    
    content = content.replace(old_run, new_run)
    # Remove toDomain method
    import re
    content = re.sub(r"private Task toDomain\(.*?\) \{.*?\n    \}", "", content, flags=re.DOTALL)
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Patched TaskReminderScheduler")

patch_port()
patch_spring_data()
patch_adapter()
patch_scheduler()
