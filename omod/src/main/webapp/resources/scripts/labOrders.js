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
        return emr.message("orderentryui.pastAction." + replacementOrder.action) + ", " + serverDateFilter(replacementOrder.dateActivated);
    }
}]).

controller('LabOrdersCtrl', ['$scope', '$window', '$location', '$timeout', 'OrderService', 'EncounterService', 'SessionInfo', 'OrderEntryService',
    function($scope, $window, $location, $timeout, OrderService, EncounterService, SessionInfo, OrderEntryService) {

        var orderContext = {};
        SessionInfo.get().$promise.then(function(info) {
            orderContext.provider = info.currentProvider;
            $scope.newDraftDrugOrder = OpenMRS.createEmptyDraftOrder(orderContext);
        });


        // TODO changing dosingType of a draft order should reset defaults (and discard non-defaulted properties)

        function loadExistingOrders() {
            $scope.activeTestOrders = { loading: true };

            OrderService.getOrders({
                t: 'testorder',
                v: 'full',
                patient: config.patient.uuid,
                careSetting: $scope.careSetting.uuid
            }).then(function(results) {
                $scope.activeTestOrders = _.map(results, function(item) { return new OpenMRS.TestOrderModel(item) });
                $scope.labOrders = labs;
                $scope.panelListResults = panelList;
            });

            $scope.pastLabOrders = { loading: true };
            OrderService.getOrders({
                t: 'testorder',
                v: 'full',
                patient: config.patient.uuid,
                careSetting: $scope.careSetting.uuid,
                status: 'inactive'
            }).then(function(results) {
                $scope.pastLabOrders = _.map(results, function(item) { return new OpenMRS.TestOrderModel(item) });
            });
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
        var panelList = OpenMRS.panelList;
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

        $scope.loadLabPanels = function(panels) {
            $scope.showFields = true;
            $scope.panelTests = [];
            $scope.labPanels = panels.panels
        }

        $scope.loadLabPanelTests = function(tests) {
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
        $scope.getSelectedTests = function(tests) {
            if(tests.selected === true) {
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

        }

        $scope.postLabOrdersEncounters = function() {
            var uuid = {uuid:"b2d06302-0901-41a6-8045-dfa32e36b105"};
            var encounterContext = {
                patient: config.patient,
                encounterType: uuid,
                location: null, // TODO
                // encounterDatetime: "2018-08-23 11:24:36",
                encounterRole: config.encounterRole
            };
            $scope.lOrders = createLabOrdersPaylaod($scope.filteredOrders);

            for (var i = 0; i < $scope.lOrders.length; ++i) {
                delete $scope.lOrders[i].concept_id;
                delete $scope.lOrders[i].name;
                delete $scope.lOrders[i].$$hashKey;
                delete $scope.lOrders[i].selected;
            }



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

        function createLabOrdersPaylaod(selectedOrders) {
            var orders = [];
            for (var i = 0; i < selectedOrders.length; ++i) {
                var data = selectedOrders[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        data['orderer'] = config.provider.uuid;
                        data['careSetting'] = $scope.careSetting.uuid;
                        data['type'] = "testorder";
                    }
                }
                orders.push(data);
            }
            return orders;

        }

        // The start of test result rendering components
        $scope.typeValues = {};
        $scope.postLabOrderResults = function() {


            $scope.obsPayload = createLabResultsObsPaylaod($scope.panelListResults);
            $scope.discontinueFilledOrders = angular.copy($scope.obsPayload);
            for (var i = 0; i < $scope.obsPayload.length; ++i) {
                delete $scope.obsPayload[i].label;
                delete $scope.obsPayload[i].orderId;
                delete $scope.obsPayload[i].orderUuid;
                delete $scope.obsPayload[i].answers;
                delete $scope.obsPayload[i].$$hashKey;
                delete $scope.obsPayload[i].rendering;
            }
            var uuid = {uuid:"b2d06302-0901-41a6-8045-dfa32e36b105"};
            var encounterContext = {
                patient: config.patient,
                encounterType: uuid,
                location: null, // TODO
                // encounterDatetime: "2018-08-23 11:24:36",
                encounterRole: config.encounterRole
            };
            $scope.loading = true;
            OrderEntryService.signAndSave({ draftOrders: [] }, encounterContext, $scope.obsPayload)
                .$promise.then(function(result) {
                discontinueLabTestOrders($scope.discontinueFilledOrders);
                location.href = location.href;
            }, function(errorResponse) {
                console.log('errorResponse.data.error.message',errorResponse.data.error.message);
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });

        };


        function createLabResultsObsPaylaod(res) {
            var obs = [];
            for (var i = 0; i < res.length; ++i) {
                var data = res[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        data['order'] = data.orderUuid;
                        data['value'] =  $scope.typeValues[data.orderId];

                    }

                }

                obs.push(data);
                var completedFields = _.filter(obs, function(o) {
                    return o.value !== undefined ;
                });

            }

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
            }

            var uuid = {uuid:"b2d06302-0901-41a6-8045-dfa32e36b105"};
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


        // functions that affect the shopping cart of orders written but not yet saved

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

        $scope.signAndSaveDraftDrugOrders = function() {
            var encounterContext = {
                patient: config.patient,
                encounterType: config.drugOrderEncounterType,
                location: null, // TODO
                encounterRole: config.encounterRole
            };

            $scope.loading = true;
            OrderEntryService.signAndSave({ draftOrders: $scope.draftDrugOrders }, encounterContext)
                .$promise.then(function(result) {
                location.href = location.href;
            }, function(errorResponse) {
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });
        }


        // functions that affect existing active orders

        $scope.discontinueOrder = function(activeOrder) {
            var dcOrder = activeOrder.createDiscontinueOrder(orderContext);
            $scope.draftDrugOrders.push(dcOrder);
            $scope.$broadcast('added-dc-order', dcOrder);
        }

        $scope.reviseOrder = function(activeOrder) {
            console.log('revised order is clicked');
            $scope.which = 'single';
            $scope.newDraftDrugOrder = activeOrder.createRevisionOrder();
        }


        // events

        $scope.$on('added-dc-order', function(dcOrder) {
            $timeout(function() {
                angular.element('#draft-orders input.dc-reason').last().focus();
            });
        });

    }]);