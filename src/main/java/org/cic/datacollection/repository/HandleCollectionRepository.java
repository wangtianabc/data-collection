package org.cic.datacollection.repository;

import org.cic.datacollection.model.HandleCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface HandleCollectionRepository extends JpaRepository<HandleCollection, Long> {
    List<HandleCollection> findAll();

    @Query(nativeQuery=true, value = "select * from handle_collection h order by h.operate_time desc limit 100")
    List<HandleCollection> findPartHandleCollections();

    @Query("from HandleCollection h where h.refHandle=:refHandle")
    List<HandleCollection> findHandleCollectionByRefHandle(@Param("refHandle") String refHandle);

    Page<HandleCollection> findAll(Pageable pageable);

    @Modifying
    @Transactional
    @Query("delete from HandleCollection d where d.id in (?1)")
    void deleteBatch(List<Long> ids);
}
