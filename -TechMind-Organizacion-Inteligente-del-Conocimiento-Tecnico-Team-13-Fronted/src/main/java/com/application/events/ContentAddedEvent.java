package com.application.events;

import org.springframework.context.ApplicationEvent;

public class ContentAddedEvent extends ApplicationEvent {
    private final Long contentId;
    private final String title;

    public ContentAddedEvent(Object source, Long contentId, String title) {
        super(source);
        this.contentId = contentId;
        this.title = title;
    }

    public Long getContentId() {
        return contentId;
    }

    public String getTitle() {
        return title;
    }
}
