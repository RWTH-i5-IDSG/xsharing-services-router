/*
 * Copyright (C) 2015-2017 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group.
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.rwth.idsg.xsharing.router.persistence.domain.mb;

import org.hibernate.annotations.Immutable;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
@Entity
@Immutable
@Cacheable(false)
@Table(name = "attribute", schema = "ixsi", catalog = "mobility_broker_db")
public class MBAttributeEntity {

    private String attributeId;
    private Boolean withText;
    private String clazz;
    private Boolean separate;
    private Boolean mandatory;
    private Short importance;
    private String url;

    @Id
    @Column(name = "attribute_id", nullable = false, insertable = true, updatable = true, length = 255)
    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    @Basic
    @Column(name = "with_text", nullable = true, insertable = true, updatable = true)
    public Boolean getWithText() {
        return withText;
    }

    public void setWithText(Boolean withText) {
        this.withText = withText;
    }

    @Basic
    @Column(name = "class", nullable = true, insertable = true, updatable = true, length = 100)
    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    @Basic
    @Column(name = "separate", nullable = true, insertable = true, updatable = true)
    public Boolean getSeparate() {
        return separate;
    }

    public void setSeparate(Boolean separate) {
        this.separate = separate;
    }

    @Basic
    @Column(name = "mandatory", nullable = true, insertable = true, updatable = true)
    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Basic
    @Column(name = "importance", nullable = true, insertable = true, updatable = true)
    public Short getImportance() {
        return importance;
    }

    public void setImportance(Short importance) {
        this.importance = importance;
    }

    @Basic
    @Column(name = "url", nullable = true, insertable = true, updatable = true, length = 255)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MBAttributeEntity that = (MBAttributeEntity) o;

        if (attributeId != null ? !attributeId.equals(that.attributeId) : that.attributeId != null) return false;
        if (withText != null ? !withText.equals(that.withText) : that.withText != null) return false;
        if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
        if (separate != null ? !separate.equals(that.separate) : that.separate != null) return false;
        if (mandatory != null ? !mandatory.equals(that.mandatory) : that.mandatory != null) return false;
        if (importance != null ? !importance.equals(that.importance) : that.importance != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = attributeId != null ? attributeId.hashCode() : 0;
        result = 31 * result + (withText != null ? withText.hashCode() : 0);
        result = 31 * result + (clazz != null ? clazz.hashCode() : 0);
        result = 31 * result + (separate != null ? separate.hashCode() : 0);
        result = 31 * result + (mandatory != null ? mandatory.hashCode() : 0);
        result = 31 * result + (importance != null ? importance.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
