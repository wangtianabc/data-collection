package org.cic.datacollection.repository;

import org.cic.datacollection.model.HandleCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HandleCollectionRepository extends JpaRepository<HandleCollection, Integer> {
    List<HandleCollection> findAll();

    @Query(nativeQuery=true, value = "select * from Handle_collection h order by h.operate_time desc limit 100")
    List<HandleCollection> findPartHandleCollections();

    @Query("from HandleCollection h where h.refHandle=:refHandle")
    List<HandleCollection> findHandleCollectionByRefHandle(@Param("refHandle") String refHandle);
}
