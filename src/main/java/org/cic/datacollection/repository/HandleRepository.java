package org.cic.datacollection.repository;

import org.cic.datacollection.model.Handles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HandleRepository extends JpaRepository<Handles, Integer> {
    @Query("from Handles h where h.idx=:idx")
    List<Handles> findByIdx(@Param("idx") Long idx);

}
