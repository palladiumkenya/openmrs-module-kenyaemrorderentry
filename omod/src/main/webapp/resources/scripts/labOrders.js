angular.module('labOrders', ['orderService', 'encounterService', 'uicommons.filters', 'uicommons.widget.select-concept-from-list',
    'uicommons.widget.select-order-frequency', 'uicommons.widget.select-drug', 'session', 'orderEntry']).

config(function($locationProvider) {
    $locationProvider.html5Mode({
        enabled: true,
        requireBase: false
    });
}).

filter('dates', ['serverDateFilter', function(serverDateFilter) {
    return function(order) {
        if (!order || typeof order != 'object') {
            return "";
        }
        if (order.action === 'DISCONTINUE' || !order.dateActivated) {
            return "";
        } else {
            var text = serverDateFilter(order.dateActivated);
            if (order.dateStopped) {
                text += ' - ' + serverDateFilter(order.dateStopped);
            }
            else if (order.autoExpireDate) {
                text += ' - ' + serverDateFilter(order.autoExpireDate);
            }
            return text;
        }
    }
}]).

filter('instructions', function() {
    return function(order) {
        if (!order || typeof order != 'object') {
            return "";
        }
        if (order.action == 'DISCONTINUE') {
            return "Discontinue " + (order.drug ? order.drug : order.concept ).display;
        }
        else {
            var text = order.getDosingType().format(order);
            if (order.quantity) {
                text += ' (Dispense: ' + order.quantity + ' ' + order.quantityUnits.display + ')';
            }
            return text;
        }
    }
}).

filter('replacement', ['serverDateFilter', function(serverDateFilter) {
    // given the order that replaced the one we are displaying, display the details of the replacement
    return function(replacementOrder) {
        if (!replacementOrder) {
            return "";
        }
        return emr.message("kenyaemrorderentry.pastAction." + replacementOrder.action) + ", " + serverDateFilter(replacementOrder.dateActivated);
    }
}]).

controller('LabOrdersCtrl', ['$scope', '$window','$rootScope', '$location', '$timeout', 'OrderService', 'EncounterService', 'SessionInfo', 'OrderEntryService',
    function($scope, $window,$rootScope, $location, $timeout, OrderService, EncounterService, SessionInfo, OrderEntryService) {

        var orderContext = {};
        SessionInfo.get().$promise.then(function(info) {
            orderContext.provider = info.currentProvider;
            $scope.newDraftDrugOrder = OpenMRS.createEmptyDraftOrder(orderContext);
        });


        // TODO changing dosingType of a draft order should reset defaults (and discard non-defaulted properties)

        function loadExistingOrders() {
            $scope.activeTestOrders = { loading: true };
            $scope.pastLabOrders = { loading: true };
            $scope.OrderUuid = '';


            OrderService.getOrders({
                t: 'testorder',
                v: 'full',
                patient: config.patient.uuid,
                careSetting: $scope.careSetting.uuid
            }).then(function(results) {
                $scope.OrderUuid = '';
                $scope.activeTestOrders = _.map(results, function(item) { return new OpenMRS.TestOrderModel(item) });
                $scope.activeTestOrdersForHvVl = $scope.activeTestOrders;
                $scope.activeTestOrders = customizeActiveOrdersToDisplaySingHivVl($scope.activeTestOrders);
                $scope.activeTestOrders.sort(function(a, b) {
                    var key1 = a.dateActivated;
                    var key2 = b.dateActivated;
                    if (key1 > key2) {
                        return -1;
                    } else if (key1 === key2) {
                        return 0;
                    } else {
                        return 1;
                    }
                });

                $scope.labOrders = labs;
                $scope.OrderReason = [
                    {
                        name:'Clinical treatment failure',
                        uuid:'843AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Pregnancy',
                        uuid:'1434AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Baseline',
                        uuid:'162080AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Follow up',
                        uuid:'162081AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    }
                ];


            });

            OrderService.getOrders({
                t: 'testorder',
                v: 'full',
                patient: config.patient.uuid,
                careSetting: $scope.careSetting.uuid
            }).then(function(results) {
                $scope.pList = enterLabOrderResults;
                if($scope.pList) {
                    $scope.panelListResults = customiseHivViralLoadObj($scope.pList);
                    $scope.labResultsRaw =$scope.panelListResults;
                    $scope.panelListResults =removeHivVl($scope.panelListResults);
                    $scope.panelListResults =removeHivLdl($scope.panelListResults);
                    $scope.InspireList = $rootScope.matrixList($scope.panelListResults, 2);
                }

            });


            OrderService.getOrders({
                t: 'testorder',
                v: 'full',
                patient: config.patient.uuid,
                careSetting: $scope.careSetting.uuid,
                status: 'inactive'
            }).then(function(results) {
                $scope.OrderUuid = '';
                $scope.limit = 12;
                $scope.pastLabOrders = pastOrders;
                if($scope.pastLabOrders ) {
                    $scope.pastLabOrders = filterDuplicates($scope.pastLabOrders);
                    $scope.pastLabOrders = renameNotDetectedToLDL($scope.pastLabOrders);
                    $scope.pastLabOrders.sort(function (a, b) {
                        var key1 = a.dateActivated;
                        var key2 = b.dateActivated;
                        if (key1 > key2) {
                            return -1;
                        } else if (key1 === key2) {
                            return 0;
                        } else {
                            return 1;
                        }
                    });
                }
            });
        }
        function renameNotDetectedToLDL(res) {
            var orders = [];
            for (var i = 0; i < res.length; ++i) {
                var data = res[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {

                        if (data.valueCoded === 'NOT DETECTED') {
                            data['valueCoded'] = "LDL";
                        }
                        if (data.resultDate ) {
                            data['resultDate'] = new Date(data.resultDate );
                        }
                        if (data.dateActivated ) {
                            data['dateActivated'] = new Date(data.dateActivated );
                        }

                    }

                }
                orders.push(data);

            }
            return orders;
        }
        function filterDuplicates(arr){
            var newArr = [];
            angular.forEach(arr, function(value, key) {
                var exists = false;
                angular.forEach(newArr, function(val2, key) {
                    if(angular.equals(value.OrderId, val2.OrderId)){ exists = true };
                });
                if(exists == false && value.OrderId != "") { newArr.push(value); }
            });
            return newArr;
        }


        function customizeActiveOrdersToDisplaySingHivVl(result) {
            return _.filter(result, function(o) {

                return o.display !== 'HIV VIRAL LOAD, QUALITATIVE';
            });
        }

        function customiseHivViralLoadObj(panelList) {
            var orders = [];
            var l = {};
            var ldl ={};
            var vLoad =[];
            var finalVl = {};
            for (var i = 0; i < panelList.length; ++i) {
                var data = panelList[i];
                for (var r in data) {

                    if (data.hasOwnProperty(r)) {
                        if (data.dateActivated ) {
                            data['dateActivated'] = new Date(data.dateActivated );
                        }

                    if(data.concept ==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                        delete data.label;
                        delete data.rendering;
                         l =
                            {
                                concept:data.concept,
                                encounter:data.encounter,
                                orderId:data.orderId,
                                orderUuid:data.orderUuid,
                                rendering:'inputnumeric',
                                dateActivated:data.dateActivated

                            }
                    }
                   else if(data.concept ==='1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                        delete data.label;
                        delete data.rendering;
                        ldl =
                            {
                                concept:data.concept,
                                encounter:data.encounter,
                                orderId:data.orderId,
                                orderUuid:data.orderUuid,
                                rendering:'checkbox',
                                dateActivated:data.dateActivated

                            }
                    }

                    }
                }
                if(l) {
                    vLoad.push(l);

                }
                if(ldl) {
                    vLoad.push(ldl);
                }

                orders.push(data);

            }
            vLoad =_.uniq(vLoad);
            var vls = _.filter(vLoad, function(o) {

                return Object.keys(o).length !== 0;
            });

            if(!_.isEmpty(vls)) {
                finalVl['hvVl'] = vls;
                finalVl['name'] ='HIV viral load';
                orders.push(finalVl);
                return orders;
            } else {
                return  panelList
            }



        }


        function replaceWithUuids(obj, props) {
            var replaced = angular.extend({}, obj);
            _.each(props, function(prop) {
                if (replaced[prop] && replaced[prop].uuid) {
                    replaced[prop] = replaced[prop].uuid;
                }
            });
            return replaced;
        }

        $scope.loading = false;

        $scope.activeTestOrders = { loading: true };
        $scope.pastLabOrders = { loading: true };
        $scope.draftDrugOrders = [];
        $scope.dosingTypes = OpenMRS.dosingTypes;
        $scope.showFields = false;
        $scope.showTestFields = false;

        var config = OpenMRS.drugOrdersConfig;
        var labs = OpenMRS.labTestJsonPayload;
        var enterLabOrderResults = OpenMRS.enterLabOrderResults;
        var pastOrders = OpenMRS.pastLabOrdersResults;


        // labObsResults
        $scope.init = function() {
            $scope.routes = config.routes;
            $scope.careSettings = config.careSettings;
            $scope.careSetting = config.intialCareSetting ?
                _.findWhere(config.careSettings, { uuid: config.intialCareSetting }) :
                config.careSettings[0];

            orderContext.careSetting = $scope.careSetting;

            loadExistingOrders();

            $timeout(function() {
                angular.element('#new-order input[type=text]').first().focus();
            });
        }


        // functions that affect the overall state of the page

        $scope.setCareSetting = function(careSetting) {
            // TODO confirm dialog or undo functionality if this is going to discard things
            $scope.careSetting = careSetting;
            orderContext.careSetting = $scope.careSetting;
            loadExistingOrders();
            $scope.draftDrugOrders = [];
            $scope.newDraftDrugOrder = OpenMRS.createEmptyDraftOrder(orderContext);
            $location.search({ patient: config.patient.uuid, careSetting: careSetting.uuid });
        }


        // functions that affect the new order being written

        $scope.addNewDraftOrder = function() {
            if ($scope.newDraftDrugOrder.getDosingType().validate($scope.newDraftDrugOrder)) {
                $scope.newDraftDrugOrder.asNeeded = $scope.newDraftDrugOrder.asNeededCondition ? true : false;
                $scope.draftDrugOrders.push($scope.newDraftDrugOrder);
                $scope.newDraftDrugOrder = OpenMRS.createEmptyDraftOrder(orderContext);
                $scope.newOrderForm.$setPristine();
                // TODO upgrade to angular 1.3 and work on form validation
                $scope.newOrderForm.$setUntouched();
            } else {
                emr.errorMessage("Invalid");
            }
        }

        $scope.cancelNewDraftOrder = function() {
            $scope.newDraftDrugOrder = OpenMRS.createEmptyDraftOrder(orderContext);
        }


        // The beginning of lab orders functionality
        $scope.selectedRow = null;

        $scope.loadLabPanels = function(panels) {
            $scope.sampleTypeName =panels.name;
            $scope.showFields = true;
            $scope.panelTests = [];
            $scope.panelTypeName = '';
            $scope.labPanels = panels.panels
        }

        $scope.loadLabPanelTests = function(tests) {
            $scope.panelTypeName = tests.name;
            $scope.showTestFields = true;
            $scope.panelTests = tests.tests
        }
        $scope.deselectedOrder = function(order) {
            order.selected = false;
            var unchecked = _.filter($scope.filteredOrders, function(o) {
                return o.concept_id !== order.concept_id;
            });
            $scope.filteredOrders = unchecked;
            $scope.selectedOrders = $scope.filteredOrders;


        }
        $scope.labOrdersTests = [];
        $scope.selectedOrders = [];
        $scope.noOrderSelected ='None';
        $scope.getSelectedTests = function(tests) {
            if(tests.selected === true) {
                checkIfSelectedTestIsActiveOrder(tests);
                $scope.selectedOrders.push(tests);
                $scope.filteredOrders = _.uniq($scope.selectedOrders);


            }

            if (tests.selected === false) {
                var unchecked = _.filter($scope.filteredOrders, function(o) {
                    return o.concept_id !== tests.concept_id;
                });
                $scope.filteredOrders = unchecked;
                $scope.selectedOrders = $scope.filteredOrders;
            }

        };

        function checkIfSelectedTestIsActiveOrder(test) {

            _.each($scope.activeTestOrders, function(o) {

                if (o.concept.uuid === test.concept) {
                    $scope.testName =test.name;
                    $('#generalMessage').modal('show');
                    test.selected = false;
                }

        });


        }

        $scope.postLabOrdersEncounters = function() {
            if(config.provider === '' || config.provider === undefined || config.provider === null) {
                $('#orderError').modal('show');
                return;
            }
            var uuid = {uuid:"e1406e88-e9a9-11e8-9f32-f2801f1b9fd1"};
            $scope.OrderUuid = '';
            $('#spinner').modal('show');

            $scope.lOrders = createLabOrdersPaylaod($scope.filteredOrders);
            $scope.lOrdersPayload = angular.copy( $scope.lOrders);

            for (var i = 0; i < $scope.lOrdersPayload.length; ++i) {
                $scope.encounterDatetime = $scope.lOrdersPayload[i].encounterDatetime;
                delete $scope.lOrdersPayload[i].concept_id;
                delete $scope.lOrdersPayload[i].name;
                delete $scope.lOrdersPayload[i].$$hashKey;
                delete $scope.lOrdersPayload[i].selected;
                delete $scope.lOrdersPayload[i].encounterDatetime;
            }

            var encounterContext = {
                patient: config.patient,
                encounterType: uuid,
                location: null, // TODO
                encounterDatetime: $scope.encounterDatetime,
                encounterRole: config.encounterRole
            };


            $scope.loading = true;
            OrderEntryService.signAndSave({ draftOrders: $scope.lOrdersPayload }, encounterContext)
                .$promise.then(function(result) {
                $('#spinner').modal('hide');
                loadExistingOrders();
                $window.location.reload();
              //  location.href = location.href;
            }, function(errorResponse) {
                $('#orderError').modal('show');
                $('#spinner').modal('hide');
                console.log('errorResponse.data.error.message',errorResponse.data.error);
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });
        }


        function createLabOrdersPaylaod(selectedOrders) {
            var orders = [];

            for (var i = 0; i < selectedOrders.length; ++i) {
                var vl = {};
                var data = selectedOrders[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        data['orderer'] = config.provider.uuid;
                        data['careSetting'] = $scope.careSetting.uuid;
                        data['type'] = "testorder";

                    }
                    if(data.concept ==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                        // create another object for LDL
                        vl = {
                            orderer:config.provider.uuid,
                            careSetting:$scope.careSetting.uuid,
                            type:"testorder",
                            concept:"1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                            concept_id: 1305,
                            orderReasonNonCoded:$scope.orderReasonNonCoded,
                            orderReason:$scope.orderReasonCoded

                        }
                    }
                }
                orders.push(data);
                orders.push(vl);

            }
            orders =_.uniq(orders);
            var filterOrders = _.filter(orders, function(o) {

                return Object.keys(o).length !== 0;
            });
            return filterOrders;

        }

        $scope.orderSelectedToAddDateActivated = function(order) {
            $scope.orderSel = order;
            $scope.orderUrgency = order;


        }

        // The start of test result rendering components
        $scope.typeValues = {};

        $scope.postLabOrderResults = function() {

            $scope.obsPayload = createLabResultsObsPaylaod($scope.labResultsRaw);
            $scope.discontinueFilledOrders = angular.copy($scope.obsPayload);
            for (var i = 0; i < $scope.obsPayload.length; ++i) {
                delete $scope.obsPayload[i].label;
                delete $scope.obsPayload[i].orderId;
                delete $scope.obsPayload[i].orderUuid;
                delete $scope.obsPayload[i].answers;
                delete $scope.obsPayload[i].$$hashKey;
                delete $scope.obsPayload[i].rendering;
                delete $scope.obsPayload[i].hivVl;
                delete $scope.obsPayload[i].name;
                delete $scope.obsPayload[i].dateActivated;
            }
            var uuid = {uuid:"e1406e88-e9a9-11e8-9f32-f2801f1b9fd1"};
            var encounterContext = {
                patient: config.patient,
                encounterType: uuid,
                location: null, // TODO
                // encounterDatetime: "2018-09-20",
                encounterRole: config.encounterRole
            };
            $scope.loading = true;
            OrderEntryService.signAndSave({ draftOrders: [] }, encounterContext, $scope.obsPayload)
                .$promise.then(function(result) {
                    if($scope.OrderUuid) {
                        $scope.voidActiveLabOrders();
                    }

                discontinueLabTestOrders($scope.discontinueFilledOrders);
                $('#spinnerSave').modal('hide');


                location.href = location.href;
            }, function(errorResponse) {
                console.log('errorResponse.data.error.message',errorResponse.data.error);
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });

        };

        $scope.hivViralValues = {};
        $scope.hivViralValuesLDL = {};
        $scope.toggleSelection = function (vl) {
            if ($scope.flag === false || $scope.flag === undefined) {
                $scope.flag = true;
                $scope.ischecked='yes';
                $scope.hivViralValues ={};
            }else {
                $scope.flag = false;
                $scope.ischecked=' ';
            }

        };

         function cancelOrder () {
            for (var i = 0; i < $scope.activeTestOrdersForHvVl.length; ++i) {
                var data = $scope.activeTestOrdersForHvVl[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        if(data.concept.uuid ==='1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' && data.dateActivated ===$scope.ldlDateActivated) {
                            $scope.OrderUuidHvl =data.uuid;
                        }

                    }

                }

            }
            var voidOrderPayload ={
                voided: true,
                voidReason: $scope.voidOrders
            };

            $scope.loading = true;
            OrderEntryService.saveVoidedOrders(voidOrderPayload, $scope.OrderUuidHvl)
                .then(function(result) {
                 location.href = location.href;
            }, function(errorResponse) {
                location.href = location.href;
                console.log('errorResponse.data.error.message',errorResponse.data.error);
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });


        }


        function createLabResultsObsPaylaod(res) {
            var obs = [];
            for (var i = 0; i < res.length; ++i) {
                var data = res[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        data['order'] = data.orderUuid;
                        data['value'] =  $scope.typeValues[data.orderId];

                        if(data.concept==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['order'] = data.orderUuid;
                            data['value'] = $scope.hivViralValues[data.orderId];
                            data['concept'] = data.concept;
                            data['encounter'] = data.encounter;
                        }
                        if(data.concept==='1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['order'] = data.orderUuid;
                            data['value'] = $scope.hivViralValuesLDL[data.orderId];
                            data['concept'] = data.concept;
                            data['encounter'] = data.encounter;
                        }
                    }
                    var hv =data.hvVl;
                        for (var l in hv) {
                            if (hv.hasOwnProperty(l)) {
                                if(hv.concept==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                    data['order'] = hv.orderUuid;
                                    data['value'] = $scope.hivViralValues[hv.orderId];
                                    data['concept'] = hv.concept;
                                    data['encounter'] = hv.encounter;
                                }
                                if(hv.concept==='1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                    data['order'] = hv.orderUuid;
                                    data['value'] = $scope.hivViralValuesLDL[hv.orderId];
                                    data['concept'] = hv.concept;
                                    data['encounter'] = hv.encounter;
                                }
                            }
                        }

                }
                if(data.value === true) {
                    data['value'] = "1302AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
                }


                obs.push(data);

            }

            _.each(obs, function(o) {
                if (o.concept === '856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                    $scope.hivVlUuid = o.order;
                }
                if (o.concept === '1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                    $scope.hivLdlUuid = o.order;
                }
            });
            _.each(obs, function(o) {

                if(o.order) {
                    if (o.concept === '856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                        if(o.value !== undefined){
                            $scope.OrderUuid = $scope.hivLdlUuid;
                        }
                    }
                   if (o.concept === '1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                        if(o.value !== undefined) {
                            $scope.OrderUuid = $scope.hivVlUuid;
                        }
                    }
                }
            });
            var completedFields = _.filter(obs, function(o) {
                return o.value !== undefined
            });
            completedFields = _.filter(completedFields, function(o) {
                return  o.value !== null;
            });
            completedFields = _.filter(completedFields, function(o) {
                return  o.value !== false;
            });
            return completedFields;
        }

        // discontinue lab test orders
        function discontinueLabTestOrders(completedFields) {

            var obs = [];
            for (var i = 0; i < completedFields.length; ++i) {
                var data = completedFields[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        data['previousOrder'] = data.orderUuid;
                        data['type'] = "testorder";
                        data['action'] = "DISCONTINUE";
                        data['careSetting'] = $scope.careSetting.uuid;
                        data['orderReasonNonCoded'] = "";
                    }

                }

                obs.push(data);

            }
            $scope.lOrders = obs;

            for (var i = 0; i < $scope.lOrders.length; ++i) {
                delete $scope.lOrders[i].label;
                delete $scope.lOrders[i].orderId;
                delete $scope.lOrders[i].orderUuid;
                delete $scope.lOrders[i].answers;
                delete $scope.lOrders[i].$$hashKey;
                delete $scope.lOrders[i].rendering;
                delete $scope.lOrders[i].order;
                delete $scope.lOrders[i].value;
                delete $scope.lOrders[i].name;
                delete $scope.lOrders[i].dateActivated;
                delete $scope.lOrders[i].hivVl;
            }

            var uuid = {uuid:"e1406e88-e9a9-11e8-9f32-f2801f1b9fd1"};
            var encounterContext = {
                patient: config.patient,
                encounterType: uuid,
                location: null, // TODO
                // encounterDatetime: "2018-08-23 11:24:36",
                encounterRole: config.encounterRole
            };


            $scope.loading = true;
            OrderEntryService.signAndSave({ draftOrders: $scope.lOrders }, encounterContext)
                .$promise.then(function(result) {
                location.href = location.href;
            }, function(errorResponse) {
                console.log('errorResponse.data.error.message',errorResponse.data.error.message);
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });

        }

        /**
         * Finds the replacement order for a given active order (e.g. the order that will DC or REVISE it)
         */
        $scope.replacementFor = function(activeOrder) {
            var lookAt = $scope.newDraftDrugOrder ?
                _.union($scope.draftDrugOrders, [$scope.newDraftDrugOrder]) :
                $scope.draftDrugOrders;
            return _.findWhere(lookAt, { previousOrder: activeOrder });
        }

        $scope.replacementForPastOrder = function(pastOrder) {
            var candidates = _.union($scope.activeTestOrders, $scope.pastLabOrders);
            return _.find(candidates, function(item) {
                return item.previousOrder && item.previousOrder.uuid === pastOrder.uuid;
            });
        }

        // functions that affect existing active orders

        $scope.discontinueOrder = function(activeOrder) {
            var dcOrder = activeOrder.createDiscontinueOrder(orderContext);
            $scope.draftDrugOrders.push(dcOrder);
            $scope.$broadcast('added-dc-order', dcOrder);
        };

        $scope.reviseOrder = function(activeOrder) {
            $scope.newDraftDrugOrder = activeOrder.createRevisionOrder();
        };
        $scope.voidOrders = '';
        $scope.OrderUuid = '';
        $scope.getOrderUuid = function(order) {
            $scope.OrderUuid = order.uuid;
            $scope.ldlDateActivated = order.dateActivated;
            $scope.ldlConcept = order.concept.uuid;

        }
        $scope.voidAllHivViralLoadOrders = function () {
            $scope.voidActiveLabOrders();
            if($scope.ldlConcept ==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                cancelOrder();
            }
        }


        $scope.voidActiveLabOrders = function() {
            var voidOrderPayload ={
                voided: true,
                voidReason: $scope.voidOrders
            };
            $scope.loading = true;
            OrderEntryService.saveVoidedOrders(voidOrderPayload, $scope.OrderUuid)
                .then(function(result) {

                $('#voidOrdersModal').modal('hide');

                location.href = location.href;
            }, function(errorResponse) {
                $('#voidOrdersModal').modal('hide');
                location.href = location.href;
                console.log('errorResponse.data.error.message',errorResponse.data.error);
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });

        };
        $scope.closeModal = function() {
            $scope.voidOrders = '';
            $scope.orderDate = '';
            angular.element('#orderDate').val('');
            $('#dateOrder').modal('hide');
            $('#orderUrgency').modal('hide');
            $('#generalMessage').modal('hide');
            $('#voidOrdersModal').modal('hide');
        };
        $scope.setOrderDate = function() {
            $scope.orderDate = angular.element('#orderDate').val();
            $scope.orderSel['dateActivated'] =  $scope.orderDate.substring(0, 10);
            $scope.orderSel['encounterDatetime'] =  $scope.orderDate.substring(0, 10);

            $scope.filteredOrders.push($scope.orderSel);
            $scope.filteredOrders = _.uniq($scope.filteredOrders);

            $('#dateOrder').modal('hide');
            $('#orderUrgency').modal('hide');

        };
        $scope.orderReasonNonCoded = '';
        $scope.orderReasonCoded = '';

        $scope.setOrderUrgency = function() {

            var e = document.getElementById("ddlOrderUrgency");
            $scope.orderUrgency['urgency'] =  e.options[e.selectedIndex].value;
            $scope.orderUrgency['orderReasonNonCoded'] =  $scope.orderReasonNonCoded;
            $scope.orderUrgency['orderReason'] =  $scope.orderReasonCoded;
            $scope.filteredOrders.push($scope.orderUrgency);
            $scope.filteredOrders = _.uniq($scope.filteredOrders);
            $('#orderUrgency').modal('hide');

        };
        // events

        $scope.$on('added-dc-order', function(dcOrder) {
            $timeout(function() {
                angular.element('#draft-orders input.dc-reason').last().focus();
            });
        });

        $rootScope.matrixList = function(data, n) {
            var grid = [], i = 0, x = data.length, col, row = -1;
            for (var i = 0; i < x; i++) {
                col = i % n;
                if (col === 0) {
                    grid[++row] = [];
                }
                grid[row][col] = data[i];
            }
            return grid;
        };

        function removeHivVl(result) {
            return _.filter(result, function(o) {

                return o.concept !== '856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';
            });
        }
        function removeHivLdl(result) {
            return _.filter(result, function(o) {

                return o.concept !== '1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';
            });
        }




    }]);