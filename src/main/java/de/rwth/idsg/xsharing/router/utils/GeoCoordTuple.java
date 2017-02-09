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
package de.rwth.idsg.xsharing.router.utils;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import static java.lang.Double.doubleToLongBits;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 28.01.2016
 */
@ToString
@RequiredArgsConstructor
public final class GeoCoordTuple {

    /** Cache the hash code */
    private int hash = 0; // Default to 0

    private final double fromX;
    private final double fromY;
    private final double toX;
    private final double toY;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GeoCoordTuple)) {
            return false;
        }

        GeoCoordTuple that = (GeoCoordTuple) o;
        return Double.compare(that.fromX, fromX) == 0
                && Double.compare(that.fromY, fromY) == 0
                && Double.compare(that.toX, toX) == 0
                && Double.compare(that.toY, toY) == 0;
    }

    /**
     * Compute only, once and then reuse
     *
     * Inspired by {@link String#hash} and {@link String#hashCode()}
     */
    @Override
    public int hashCode() {
        if (hash == 0) {
            computeHash();
        }
        return hash;
    }

    private void computeHash() {
        final int PRIME = 59;
        int result = 1;
        final long $fromX = doubleToLongBits(this.fromX);
        result = result * PRIME + (int) ($fromX >>> 32 ^ $fromX);
        final long $fromY = doubleToLongBits(this.fromY);
        result = result * PRIME + (int) ($fromY >>> 32 ^ $fromY);
        final long $toX = doubleToLongBits(this.toX);
        result = result * PRIME + (int) ($toX >>> 32 ^ $toX);
        final long $toY = doubleToLongBits(this.toY);
        result = result * PRIME + (int) ($toY >>> 32 ^ $toY);
        hash = result;
    }
}
