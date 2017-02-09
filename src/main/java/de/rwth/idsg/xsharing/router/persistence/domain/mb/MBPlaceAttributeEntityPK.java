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

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * @author Max Wiederhold <maximilian.wiederhold@rwth-aachen.de>
 */
public class MBPlaceAttributeEntityPK implements Serializable {

    private String placeId;
    private String attributeId;

    @Column(name = "place_id", nullable = false, insertable = true, updatable = true, length = 255)
    @Id
    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Column(name = "attribute_id", nullable = false, insertable = true, updatable = true, length = 255)
    @Id
    public String getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(String attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MBPlaceAttributeEntityPK that = (MBPlaceAttributeEntityPK) o;

        if (placeId != null ? !placeId.equals(that.placeId) : that.placeId != null) return false;
        if (attributeId != null ? !attributeId.equals(that.attributeId) : that.attributeId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = placeId != null ? placeId.hashCode() : 0;
        result = 31 * result + (attributeId != null ? attributeId.hashCode() : 0);
        return result;
    }
}
