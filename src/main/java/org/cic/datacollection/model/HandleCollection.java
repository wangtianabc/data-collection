package org.cic.datacollection.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name="handle_collection", schema="handle-dev")
public class HandleCollection implements Serializable {
    @Id
    private Long id;
    @Column
    private String handle;
    @Column
    private String operate;
    @Column
    private Timestamp operateTime;
    @Column
    private String refHandle;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public Timestamp getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(Timestamp operateTime) {
        this.operateTime = operateTime;
    }

    public String getRefHandle() {
        return refHandle;
    }

    public void setRefHandle(String refHandle) {
        this.refHandle = refHandle;
    }
}
