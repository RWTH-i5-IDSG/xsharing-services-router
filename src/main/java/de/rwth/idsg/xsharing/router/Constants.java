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
package de.rwth.idsg.xsharing.router;

import java.util.concurrent.TimeUnit;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 01.02.2017
 */
public final class Constants {

    public static final String PROPS_FILE = "application.properties";

    public static final String EH_CACHE_NAME = "de.rwth.idsg.xsharing.router.cache.mainCache";

    /**
     * Config containing ASEAG server URLs and client config
     */
    public static final class UraConfig {

        // Parameter names
        private static final String RETURN_LIST = "ReturnList";
        private static final String STOP_POINT_NAME = "StopPointName";
        private static final String STOP_ID = "StopID";
        private static final String LATITUDE = "Latitude";
        private static final String LONGITUDE = "Longitude";

        public static String getBaseParams() {
            return RETURN_LIST + "=" + STOP_POINT_NAME + "," + STOP_ID + "," + LATITUDE + "," + LONGITUDE;
        }
    }

    /**
     * Central JMS queue set up, including specific consumer count for receiving messages (allows tuning)
     */
    public static final class JMSConfig {

        public static final String DESTINATION_TYPE = "javax.jms.Queue";
        public static final String MAX_CONSUMER_COUNT = "50";

        // configure JMS Queue names for xsharing requests
        public static final String MINIMAL_QUEUE_NAME = "java:jboss/exported/jms/queue/xSharingMinimalRequestQueue";
        public static final String COMPACT_QUEUE_NAME = "java:jboss/exported/jms/queue/xSharingCompactRequestQueue";
        public static final String DETAILS_QUEUE_NAME = "java:jboss/exported/jms/queue/xSharingDetailsRequestQueue";
        public static final String LOWERBOUNDS_QUEUE_NAME = "java:jboss/exported/jms/queue/xSharingLowerBoundsRequestQueue";
    }

    /**
     * Constants used for batch job configuration
     */
    public static final class BatchConstants {

        // job names
        public static final String REQUEST_JOB = "requestJob";
        public static final String MATRIX_JOB = "matrixJob";
        public static final String NEIGHBORS_JOB = "nearestStationsJob";

        // parameter names
        public static final String REQUEST = "request";
        public static final String REQUEST_POINTS = "requestPoints";
        public static final String PATH_TYPE = "pathType";
        public static final String BATCH_SIZE = "batchSize";
        public static final String MAX_WALK = "maxWalk";
        public static final String PARTITIONING = "doPartition";
        public static final String IVROUTE = "isIVRoute";
        public static final String CARTESIAN = "isCartesian";

        // property names
        public static final String START = "start";
        public static final String END = "end";
    }

    /**
     * Config for "Individualverkehr"-Router
     */
    public static final class IVRouterConfig {

        public static final String REQUEST_FORMAT = "JSON";

        // change these to increase iv request matrix sizes in preprocessing
        public static final int MAX_REQUEST_SIZE = 200;
        public static final int MAX_MATRIX_SIZE = 250;

        public static final double DISTANCE_TIME_WEIGHTING_VALUE = 0.0;

        // endpoint
        public static final String GET_INFO = "RtIvuGp";
        public static final String ISO_DISTANCE = "RtIvuGp.EZB/execute";
        public static final String SHORTEST_PATHS = "RtIvuGp.SP/execute";
        public static final String DISTANCE_MATRIX = "RtIvuGp.DMX/execute";
        public static final String ENTRY_POINT = "RtIvuGp.ESP/execute";

        // vehicle classes
        public static final String PKW = "PKW";
        public static final String LKW = "LKW";
        public static final String BUS = "Bus";
        public static final String PEDESTRIAN_SLOW = "PedestrianSlow";
        public static final String PEDESTRIAN_NORMAL = "PedestrianNormal";
        public static final String PEDESTRIAN_FAST = "PedestrianFast";
        public static final String BIKE = "Fahrrad";

        // -------------------------------------------------------------------------
        // Parameter names
        // -------------------------------------------------------------------------

        // request stuff
        public static final String FORMAT = "f";

        // shared param names
        public static final String VEHICLE_ID = "VehicleId";
        public static final String VEHICLE_WIDTH_OVERRIDE = "VehicleWidthOverride";
        public static final String VEHICLE_HEIGHT_OVERRIDE = "VehicleHeightOverride";
        public static final String VEHICLE_LENGTH_OVERRIDE = "VehicleLengthOverride";
        public static final String VEHICLE_WEIGHT_OVERRIDE = "VehicleWeightOverride";
        public static final String VEHICLE_PROFILES_OVERRIDE = "vehicleProfilesOverride";

        public static final String VEHICLE_OFFROAD_SPEED_OVERRIDE = "VehicleOffroadSpeedOverride";
        public static final String VEHICLE_OFFROAD_FACTOR_OVERRIDE = "VehicleOffroadFactorOverride";
        public static final String DISTANCE_TIME_WEIGHTING = "DistanceTimeWeighting";

        // shortest paths
        public static final String MODE = "Mode";
        public static final String CREATE_ROUTES = "CreateRoutes";
        public static final String CREATE_WAYPOINTS = "CreateWaypoints";
        public static final String POINTS = "Points";

        // distance matrix
        public static final String IGNORE_OFFROAD_DISTANCE = "IgnoreOffroadDistance";
        public static final String USE_BLOCKING_POLYGONS = "UseBlockingPolygons";
        public static final String START_POINTS = "StartPoints";
        public static final String END_POINTS = "EndPoints";

        // ShortestPathsRequest modes
        public static final String MODE_ONE_TO_N = "1ToN";
        public static final String MODE_N_TO_ONE = "NTo1";
        public static final String MODE_ONE_TO_ONE = "1To1";
        public static final String MODE_N_X_ONE_TO_ONE = "Nx1To1";
    }

    public static final class DatabaseConstants {

        public static final String DB_NAME_XSHARING = "xsharing-db";
        public static final String DB_NAME_MB = "mb-adapter";

        // as defined in persistence.xml with property "hibernate.jdbc.batch_size"
        public static final int BATCH_SIZE = 100;

        public static final int BIKE_LEG_TRANSFER_TIME = (int) TimeUnit.MINUTES.toSeconds(2);
        public static final int CAR_LEG_TRANSFER_TIME = (int) TimeUnit.MINUTES.toSeconds(5);
    }

}
