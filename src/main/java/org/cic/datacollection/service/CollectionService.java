package org.cic.datacollection.service;

import org.cic.datacollection.vo.ResultInfo;

public interface CollectionService {
    ResultInfo collectData();

    ResultInfo collectDataByMeta(String metaHandleCode);
}
