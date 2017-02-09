/**
 * Created by max on 11/07/15.
 */
controlApp.factory('Control', ['$resource',
    function ($resource) {
        return $resource('', {}, {
            'getHome': {
                method: 'GET', isArray: false, url: 'rest/control/home'
            },
            'rebuildAll': {
                method: 'GET', isArray: false, url: 'rest/control/rebuild-all/:granularity',
                params: {"granularity": "@granularity"}
            },
            'destRequest': {
                method: 'GET', url: 'rest/control/destRequest/:destination/:routeId',
                params: {"destination": "@destination", "routeId": "@routeId"}
            },
            'routeRequest': {method: 'GET', url: 'rest/control/routeRequest'},
            'cacheStatus': {
                method: 'GET', isArray: false, url: 'rest/control/cache/status',
                transformResponse: function (data, headersGetter, status) {
                    return {content: data};
                }
            },
            'clearCache': {method: 'GET', url: 'rest/control/cache/clear'},
            'cacheKeys': {
                method: 'GET', isArray: false, url: 'rest/control/cache/keys',
                transformResponse: function (data, headersGetter, status) {
                    return {cacheKeys: data};
                }
            },
            'getRasterGranularity': {
                method: 'GET', isArray: false, url: 'rest/control/raster-granularity',
                transformResponse: function (data, headersGetter, status) {
                    return {granularity: data};
                }
            },
            'getStatus': {
                method: 'GET', url: 'rest/control/status', transformResponse: function (data, headersGetter, status) {
                    return {status: data};
                }
            },
            'resetQueueStats': {
                method: 'PUT', url: 'rest/control/stats/reset'
            },
            'clearRouteLegCache': {
                method: 'PUT', url: 'rest/control/leg-cache/clear'
            }
        })
    }
]);
