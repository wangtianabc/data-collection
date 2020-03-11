package org.cic.datacollection.model;

import java.io.Serializable;
import java.sql.Blob;

import javax.persistence.*;

@Entity
@Table(name="handles", schema="handle-dev")
@IdClass(HandlesPk.class)
public class Handles implements Serializable {
    @Id
    @Column(nullable = false)
    private String handle;
    @Id
    @Column(nullable = false)
    private Long idx;
    @Column (name = "type")
    private Blob type;
    @Lob
    @Column(name = "data")
    private Blob data;
    @Column
    private Integer ttlType;
    @Column
    private Long ttl;
    @Column
    private Blob refs;
    @Column
    private Integer adminRead;
    @Column
    private Integer adminWrite;
    @Column
    private Integer pubRead;
    @Column
    private Integer pubWrite;

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public Long getIdx() {
        return idx;
    }

    public void setIdx(Long idx) {
        this.idx = idx;
    }

    public Blob getType() {
        return type;
    }

    public void setType(Blob type) {
        this.type = type;
    }

    public Blob getData() {
        return data;
    }

    public void setData(Blob data) {
        this.data = data;
    }

    public Integer getTtlType() {
        return ttlType;
    }

    public void setTtlType(Integer ttlType) {
        this.ttlType = ttlType;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public Blob getRefs() {
        return refs;
    }

    public void setRefs(Blob refs) {
        this.refs = refs;
    }

    public Integer getAdminRead() {
        return adminRead;
    }

    public void setAdminRead(Integer adminRead) {
        this.adminRead = adminRead;
    }

    public Integer getAdminWrite() {
        return adminWrite;
    }

    public void setAdminWrite(Integer adminWrite) {
        this.adminWrite = adminWrite;
    }

    public Integer getPubRead() {
        return pubRead;
    }

    public void setPubRead(Integer pubRead) {
        this.pubRead = pubRead;
    }

    public Integer getPubWrite() {
        return pubWrite;
    }

    public void setPubWrite(Integer pubWrite) {
        this.pubWrite = pubWrite;
    }
}
