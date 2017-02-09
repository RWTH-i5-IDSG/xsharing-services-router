/**
 * Created by max on 11/07/15.
 */

'use strict';

var controlApp = angular.module('controlApp', [
    'ngResource',
    'ngRoute'
])
    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/main', {
            templateUrl: 'views/main.html',
            controller: 'MainController',
            resolve: {
                    resolvedSharingStatus: ['Control',
                        function (Control) {
                            return Control.getHome();
                        }]
                }
        })
        .otherwise({
            redirectTo: '/main'
        });
    }])
;