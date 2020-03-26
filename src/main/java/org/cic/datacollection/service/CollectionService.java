package org.cic.datacollection.service;

import net.handle.hdllib.AuthenticationInfo;
import org.cic.datacollection.model.HandleCollection;
import org.cic.datacollection.vo.ResultInfo;

import java.util.List;

public interface CollectionService {
    ResultInfo collectData(String handlePrefix, String handleSuffix, String password);

    ResultInfo collectDataByMeta(String handlePrefix, String handleSuffix, String password,String metaHandleCode);

    ResultInfo checkAuth(String handlePrefix, String handleSuffix, String password);

    ResultInfo getHandleRecordList(List<HandleCollection> handleCollections, AuthenticationInfo auth);
}
