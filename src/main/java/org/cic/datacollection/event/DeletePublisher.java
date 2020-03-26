package org.cic.datacollection.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DeletePublisher {
    @Autowired
    ApplicationContext applicationContext;

    public void publish(DeleteEvent demoEvent) {
        applicationContext.publishEvent(demoEvent);
    }
}
