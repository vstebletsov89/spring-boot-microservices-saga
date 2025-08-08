package ru.otus.notification.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;
import ru.otus.notification.entity.Notification;

import java.util.UUID;

@Repository
public interface NotificationRepository extends CassandraRepository<Notification, UUID> {
}
