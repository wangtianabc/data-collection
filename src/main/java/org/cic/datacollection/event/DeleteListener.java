package org.cic.datacollection.event;

import org.cic.datacollection.model.HandleCollection;
import org.cic.datacollection.repository.HandleCollectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeleteListener implements ApplicationListener<DeleteEvent> {
    private Logger logger = LoggerFactory.getLogger(DeleteListener.class);
    @Autowired
    private HandleCollectionRepository handleCollectionRepository;

    @Override
    @Async
    public void onApplicationEvent(DeleteEvent deleteEvent) {
        List<HandleCollection> data = deleteEvent.getData();
        try {
            //logger.info("delete begin");
            List<Long> ids = new ArrayList<>();
            for (HandleCollection handleCollection : data) {
                ids.add(handleCollection.getId());
            }
            handleCollectionRepository.deleteBatch(ids);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
