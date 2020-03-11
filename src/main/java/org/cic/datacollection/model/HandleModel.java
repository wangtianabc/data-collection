package org.cic.datacollection.model;

import java.util.ArrayList;
import java.util.List;

public class HandleModel {
    private String handle;
    private List<Object> values;
    private String opt;

    public HandleModel() {
        this.values = new ArrayList<>();
    }
    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }
}
