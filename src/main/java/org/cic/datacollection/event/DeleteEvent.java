package org.cic.datacollection.event;

import org.cic.datacollection.model.HandleCollection;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class DeleteEvent extends ApplicationEvent {
    private List<HandleCollection> data;//传递的数据，根据实际业务需求传递

    public DeleteEvent(Object source,List<HandleCollection> data){
        super(source);  //要实现父类构造方法，source可以是任意的
        this.data = data;
    }

    public List<HandleCollection> getData() {
        return data;
    }

    public void setData(List<HandleCollection> data) {
        this.data = data;
    }
}
