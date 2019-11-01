package org.boot.uploader.model;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@Entity
public class FileInfo implements Serializable {
    @Id
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    @GeneratedValue(generator = "system-uuid")
    @Length(max = 36)
    private String id;

    private Date created;
    private Date updated;
    /**
     * 文件名
     */
    @Column(nullable = false)
    private String filename;
    /**
     * 文件标识
     */
    @Column(nullable = false)
    private String uniqueIdentifier;

    @Column(nullable = false)
    private Long totalSize;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String location;


    @PrePersist
    protected void onCreate() {
        if (created == null) {
            created = new Date();
        }
    }
    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
