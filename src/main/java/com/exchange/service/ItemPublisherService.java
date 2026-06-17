package com.exchange.service;

import com.exchange.dto.PublisherVO;

public interface ItemPublisherService {
    PublisherVO getPublisher(Long itemId, Long viewerUserId);
}
