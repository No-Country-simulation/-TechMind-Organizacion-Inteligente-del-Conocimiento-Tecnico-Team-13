package com.application.events;

import org.springframework.context.ApplicationEvent;
import java.util.UUID;

public class ContentAddedEvent extends ApplicationEvent {
    private final UUID contentId;
    private final String title;

    public ContentAddedEvent(Object source, UUID contentId, String title) {
        super(source);
        this.contentId = contentId;
        this.title = title;
    }

    public UUID getContentId() {
        return contentId;
    }

    public String getTitle() {
        return title;
    }
}
