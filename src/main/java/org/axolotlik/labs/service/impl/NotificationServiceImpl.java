package org.axolotlik.labs.service.impl;

import org.axolotlik.labs.service.NotificationService;
import org.springframework.stereotype.Component;

@Component
public class NotificationServiceImpl implements NotificationService {
    public void notify(String message) {
        System.out.println("NOTIFICATION: " + message);
    }
}