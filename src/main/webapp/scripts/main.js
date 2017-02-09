/**
 * Created by max on 11/07/15.
 */
controlApp.controller('MainController', ['$scope', 'Control', 'resolvedSharingStatus',
    function ($scope, Control, resolvedSharingStatus) {

        $scope.sharingStatus = resolvedSharingStatus;

        $scope.request = {
            'from': {
                'x': 6.0509,
                'y': 50.775000000000006
            },
            'to': {
                'x': 6.1196,
                'y': 50.7498
            },
            'routeId': ""
        };

        $scope.showCacheKeys = false;
        $scope.cacheKeys = '[]';

        $scope.destinations = [
            'java:jboss/exported/jms/queue/xSharingCompactRequestQueue',
            'java:jboss/exported/jms/queue/xSharingDetailsRequestQueue'
        ];

        $scope.rebuildAll = function () {
            Control.rebuildAll({granularity: $scope.sharingStatus.rasterGranularity});
        };

        $scope.clearCache = function () {
            Control.clearCache();
        };

        $scope.destRequest = function (dest) {
            Control.destRequest({destination: dest, routeId: $scope.request.routeId});
        };

        $scope.routeRequest = function (request) {
            Control.routeRequest({
                'from_x': request.from.x,
                'from_y': request.from.y,
                'to_x': request.to.x,
                'to_y': request.to.y
            })
        };

        $scope.getStatus = function () {
            $scope.sharingStatus.cacheStatus = Control.cacheStatus();
        };

        $scope.toggleCacheKeys = function () {
            $scope.showCacheKeys = !$scope.showCacheKeys;

            if ($scope.showCacheKeys) {
                Control.cacheKeys().$promise.then(function (result) {
                    $scope.cacheKeys = result.cacheKeys;
                });
            }
        };

        $scope.resetQueueStats = function () {
            Control.resetQueueStats().$promise.then(function () {
                $scope.sharingStatus = Control.getHome();
            });
        };

        $scope.clearRouteLegCache = function () {
            Control.clearRouteLegCache().$promise.then(function () {
                $scope.sharingStatus = Control.getHome();
            });
        };
    }
]);