package com.guet.clinic.server.service;

import com.guet.clinic.pojo.entity.SystemMessage;

public interface SystemMessageService extends CrudService<SystemMessage> {
    SystemMessage markRead(Long id);
    void markAllRead(Long receiverId);
}
