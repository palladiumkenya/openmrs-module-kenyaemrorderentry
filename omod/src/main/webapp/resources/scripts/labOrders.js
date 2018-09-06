(function () {
    'use strict';
    var labs = [
        {
            "name":"Blood clot",
            "panel": [
                {
                    "panel_name":"HIV",
                    "id":'1',
                    "tests":[
                        {
                            "name":"VL",
                            "concept_id":"277272"
                        },
                        {
                            "name":"CD4",
                            "concept_id":"5555"
                        }
                    ]
                },
                {
                    "panel_name":"TB",
                    "id":'2',
                    "tests":[
                        {
                            "name":"PCR",
                            "concept_id":"8888"
                        },
                        {
                            "name":"CD4 counts",
                            "concept_id":"44444"
                        }
                    ]
                }
            ]
        }
    ];
angular.module('secondApp', [])
    .controller('SecondController', SecondController);
    SecondController.$inject = [
        '$scope'

    ];
    function SecondController($scope) {
        $scope.desc = labs;
    }
   /* .controller('SecondController', function($scope) {
        $scope.init = function() {


            loadExistingOrders();

        }

        function loadExistingOrders() {
            $scope.desc = labs;

        }
    });*/

/*
var secondApp = angular.module('secondApp', []);
secondApp.controller('SecondController', function($scope) {
    $scope.desc = "Second app. ";
});*/
})();