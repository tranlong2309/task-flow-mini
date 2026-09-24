package com.taskflow.infrastructure.persistence.repository;

import com.taskflow.domain.model.Notification;
import com.taskflow.domain.model.NotificationReceiver;
import com.taskflow.domain.model.UserNotification;
import com.taskflow.domain.repository.NotificationRepositoryPort;
import com.taskflow.infrastructure.persistence.entity.NotificationEntity;
import com.taskflow.infrastructure.persistence.entity.NotificationReceiverEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class NotificationRepositoryAdapter implements NotificationRepositoryPort {

    private final SpringDataNotificationRepository notificationRepository;
    private final SpringDataNotificationReceiverRepository receiverRepository;

    public NotificationRepositoryAdapter(SpringDataNotificationRepository notificationRepository, SpringDataNotificationReceiverRepository receiverRepository) {
        this.notificationRepository = notificationRepository;
        this.receiverRepository = receiverRepository;
    }

    @Override
    public Notification saveNotification(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        entity.setId(notification.getId());
        entity.setType(notification.getType());
        entity.setTitle(notification.getTitle());
        entity.setMessage(notification.getMessage());
        entity.setRelatedTaskId(notification.getRelatedTaskId());
        entity.setCreatedAt(notification.getCreatedAt());
        entity.setExpiresAt(notification.getExpiresAt());
        
        entity = notificationRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public void saveReceivers(List<NotificationReceiver> receivers) {
        List<NotificationReceiverEntity> entities = receivers.stream().map(this::toReceiverEntity).collect(Collectors.toList());
        receiverRepository.saveAll(entities);
    }

    @Override
    public Optional<NotificationReceiver> findReceiverById(Long id) {
        return receiverRepository.findById(id).map(this::toReceiverDomain);
    }

    @Override
    public NotificationReceiver saveReceiver(NotificationReceiver receiver) {
        return toReceiverDomain(receiverRepository.save(toReceiverEntity(receiver)));
    }

    @Override
    public boolean hasRecentNotification(String type, UUID relatedTaskId, Instant since) {
        return notificationRepository.existsByTypeAndRelatedTaskIdAndCreatedAtAfter(type, relatedTaskId, since);
    }

    @Override
    public List<UserNotification> getUserNotifications(Long userId) {
        List<NotificationReceiverEntity> receivers = receiverRepository.findByUserIdOrderByNotificationIdDesc(userId);
        
        List<Long> notificationIds = receivers.stream().map(NotificationReceiverEntity::getNotificationId).collect(Collectors.toList());
        
        Map<Long, NotificationEntity> notifications = notificationRepository.findAllById(notificationIds).stream()
                .collect(Collectors.toMap(NotificationEntity::getId, n -> n));

        return receivers.stream()
                .filter(r -> notifications.containsKey(r.getNotificationId()))
                .map(r -> {
                    NotificationEntity n = notifications.get(r.getNotificationId());
                    return new UserNotification(r.getId(), r.getUserId(), n.getType(), n.getTitle(), n.getMessage(), r.getIsRead(), n.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    private Notification toDomain(NotificationEntity entity) {
        return new Notification(entity.getId(), entity.getType(), entity.getTitle(), entity.getMessage(), entity.getRelatedTaskId(), entity.getCreatedAt(), entity.getExpiresAt());
    }

    private NotificationReceiverEntity toReceiverEntity(NotificationReceiver r) {
        NotificationReceiverEntity e = new NotificationReceiverEntity();
        e.setId(r.getId());
        e.setNotificationId(r.getNotificationId());
        e.setUserId(r.getUserId());
        e.setIsRead(r.getIsRead() != null ? r.getIsRead() : false);
        e.setReadAt(r.getReadAt());
        return e;
    }

    private NotificationReceiver toReceiverDomain(NotificationReceiverEntity e) {
        return new NotificationReceiver(e.getId(), e.getNotificationId(), e.getUserId(), e.getIsRead(), e.getReadAt());
    }
}
