angular.module('drugOrders', ['orderService', 'encounterService', 'uicommons.filters', 'uicommons.widget.select-concept-from-list',
    'uicommons.widget.select-order-frequency', 'uicommons.widget.select-drug', 'session', 'orderEntry']).config(function ($locationProvider) {
    $locationProvider.html5Mode({
        enabled: true,
        requireBase: false
    });
}).filter('dates', ['serverDateFilter', function (serverDateFilter) {
    return function (order) {
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
}]).filter('instructions', function () {
    return function (order) {
        if (!order || typeof order != 'object') {
            return "";
        }
        if (order.action == 'DISCONTINUE') {
            return "Discontinue " + (order.drug ? order.drug : order.concept).display;
        }
        else {
            var text = order.getDosingType().format(order);
            if (order.quantity) {
                text += ' (Dispense: ' + order.quantity + ' ' + order.quantityUnits.display + ')';
            }
            return text;
        }
    }
}).filter('replacement', ['serverDateFilter', function (serverDateFilter) {
    // given the order that replaced the one we are displaying, display the details of the replacement
    return function (replacementOrder) {
        if (!replacementOrder) {
            return "";
        }
        return emr.message("kenyaemrorderentry.pastAction." + replacementOrder.action) + ", " + serverDateFilter(replacementOrder.dateActivated);
    }
}]).controller('DrugOrdersCtrl', ['$scope', '$window', '$location', '$timeout', 'OrderService', 'EncounterService', 'SessionInfo', 'OrderEntryService',
    function ($scope, $window, $location, $timeout, OrderService, EncounterService, SessionInfo, OrderEntryService) {

        var orderContext = {};
        SessionInfo.get().$promise.then(function (info) {
            orderContext.provider = info.currentProvider;
            $scope.newDraftDrugOrder = OpenMRS.createEmptyDraftOrder(orderContext);
        });

        // TODO changing dosingType of a draft order should reset defaults (and discard non-defaulted properties)
          var programRegimens = OpenMRS.kenyaemrRegimenJsonPayload;
        $scope.showRegimenPanel = false;

        function loadExistingOrders() {
            $scope.activeDrugOrders = {loading: true};
            OrderService.getOrders({
                t: 'drugorder',
                v: 'full',
                patient: config.patient.uuid,
                careSetting: $scope.careSetting.uuid
            }).then(function (results) {
                $scope.activeDrugOrders = _.map(OpenMRS.activeOrdersPayload.single_drugs, function (item) {
                    return new OpenMRS.DrugOrderModel(item)
                });
                $scope.activeDrugOrders.sort(function(a, b) {
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

                $scope.patientRegimens = addRegimenStatus(programRegimens);
                $scope.patientActiveDrugOrders = OpenMRS.activeOrdersPayload;
                if($scope.patientActiveDrugOrders) {
                    if($scope.patientActiveDrugOrders.order_groups[0]) {
                        $scope.patientRegimenInstruction =formatDisplayOfRegimenInstructions($scope.patientActiveDrugOrders.order_groups[0].components);
                    }
                }

                $scope.regimenStatus = "absent";
                if ($scope.patientRegimens.length == 0) {
                    $scope.showRegimenPanel = false;
                }
                if ($scope.patientActiveDrugOrders.order_groups.length > 0) {
                    $scope.disableButton = true
                }

            });

            $scope.pastDrugOrders = {loading: true};
            OrderService.getOrders({
                t: 'drugorder',
                v: 'full',
                patient: config.patient.uuid,
                careSetting: $scope.careSetting.uuid,
                status: 'inactive'
            }).then(function (results) {

                $scope.patientPastDrugOrders = OpenMRS.pastDrugOrdersPayload;
                $scope.addQuantity = $scope.patientPastDrugOrders.pastOrder_groups;
                $scope.addQuantity.sort(function(a, b) {
                    var key1 = a.date;
                    var key2 = b.date;
                    if (key1 > key2) {
                        return -1;
                    } else if (key1 === key2) {
                        return 0;
                    } else {
                        return 1;
                    }
                });
                if($scope.patientPastDrugOrders) {
                    $scope.patientPastSingleDrugInstruction = formatDisplayOfPastSingleDrugInstructions($scope.patientPastDrugOrders.pastSingle_drugs);

                    if($scope.patientPastDrugOrders.pastOrder_groups) {
                        $scope.pastOrders = formatDisplayOfPastRegimenInstructions($scope.patientPastDrugOrders.pastOrder_groups);
                       Array.prototype.push.apply($scope.pastOrders,$scope.patientPastSingleDrugInstruction);
                        $scope.pastOrders.sort(function(a, b) {
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
                }
            });
        }

        function addRegimenStatus(completedFields) {

            var reg = [];
            for (var i = 0; i < completedFields.length; ++i) {
                var data = completedFields[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        data['regimenStatus'] = "active";
                    }
                }
                reg.push(data);
            }
            return reg
        }

        function addQuantityAndQuantityUnits(completedFields) {
            var pastOrders = $scope.addQuantity[0].components;
            var reg = [];
            for (var i = 0; i < completedFields.length; ++i) {
                var data = completedFields[i];
                for (var t =0; t<pastOrders.length; ++t) {
                    if(i === t) {
                        for (var r in data) {
                            if (data.hasOwnProperty(r)) {
                                data['quantity_units'] = pastOrders[t].quantity_units;
                                data['quantity'] = pastOrders[t].quantity;
                            }
                        }
                        reg.push(data);

                    }

                }

            }
            return reg
        }


        function replaceWithUuids(obj, props) {
            var replaced = angular.extend({}, obj);
            _.each(props, function (prop) {
                if (replaced[prop] && replaced[prop].uuid) {
                    replaced[prop] = replaced[prop].uuid;
                }
            });
            return replaced;
        }

        $scope.loading = false;

        $scope.activeDrugOrders = {loading: true};
        $scope.pastDrugOrders = {loading: true};
        $scope.draftDrugOrders = [];
        $scope.dosingTypes = OpenMRS.dosingTypes;
        $scope.showCurrentRegimenView = true;
        $scope.showActiveTabs = true;
        $scope.showPastDrugTabs= true;
        $scope.showOtherDrugs = true;
        $scope.showStandardRegimenTab = true;


        var config = OpenMRS.drugOrdersConfig;
        $scope.init = function () {
            $scope.routes = config.routes;
            $scope.doseUnits = config.doseUnits;
            $scope.durationUnits = config.durationUnits;
            $scope.quantityUnits = config.quantityUnits;
            $scope.frequencies = config.frequencies;
            $scope.careSettings = config.careSettings;
            $scope.careSetting = config.intialCareSetting ?
                _.findWhere(config.careSettings, {uuid: config.intialCareSetting}) :
                config.careSettings[0];

            orderContext.careSetting = $scope.careSetting;

            loadExistingOrders();

            $timeout(function () {
                angular.element('#new-order input[type=text]').first().focus();
            });
        }
        // functions that affect the overall state of the page

        $scope.setCareSetting = function (careSetting) {
            // TODO confirm dialog or undo functionality if this is going to discard things
            $scope.careSetting = careSetting;
            orderContext.careSetting = $scope.careSetting;
            loadExistingOrders();
            $scope.draftDrugOrders = [];
            $scope.newDraftDrugOrder = OpenMRS.createEmptyDraftOrder(orderContext);
            $location.search({patient: config.patient.uuid, careSetting: careSetting.uuid});
        }


        // functions that affect the new order being written

        $scope.addNewDraftOrder = function () {
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

        $scope.cancelNewDraftOrder = function () {
            $scope.newDraftDrugOrder = OpenMRS.createEmptyDraftOrder(orderContext);
        }


        // functions that affect the shopping cart of orders written but not yet saved

        $scope.cancelAllDraftDrugOrders = function () {
            $scope.draftDrugOrders = [];
        }

        $scope.cancelDraftDrugOrder = function (draftDrugOrder) {
            $scope.draftDrugOrders = _.without($scope.draftDrugOrders, draftDrugOrder);
        }

        $scope.editDraftDrugOrder = function (draftDrugOrder) {
            $scope.draftDrugOrders = _.without($scope.draftDrugOrders, draftDrugOrder);
            $scope.newDraftDrugOrder = draftDrugOrder;
        }

        /**
         * Finds the replacement order for a given active order (e.g. the order that will DC or REVISE it)
         */
        $scope.replacementFor = function (activeOrder) {
            var lookAt = $scope.newDraftDrugOrder ?
                _.union($scope.draftDrugOrders, [$scope.newDraftDrugOrder]) :
                $scope.draftDrugOrders;
            return _.findWhere(lookAt, {previousOrder: activeOrder});
        }

        $scope.replacementForPastOrder = function (pastOrder) {
            var candidates = _.union($scope.activeDrugOrders, $scope.pastDrugOrders);
            return _.find(candidates, function (item) {
                return item.previousOrder && item.previousOrder.uuid === pastOrder.uuid;
            });
        }

        $scope.signAndSaveDraftDrugOrders = function () {
            var encounterContext = {
                patient: config.patient,
                encounterType: config.drugOrderEncounterType,
                location: null, // TODO
                encounterRole: config.encounterRole
            };

            $scope.loading = true;
            OrderEntryService.signAndSave({draftOrders: $scope.draftDrugOrders}, encounterContext)
                .$promise.then(function (result) {
                location.href = location.href;
            }, function (errorResponse) {
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });
        }


        // functions that affect existing active orders

        $scope.discontinueOrder = function (activeOrder) {
            if(config.provider === '' || config.provider === undefined || config.provider === null) {
                $scope.showErrorToast ='You are not login as provider, please contact System Administrator';
                $('#orderError').modal('show');
                return;
            }
            var dcOrder = activeOrder.createDiscontinueOrder(orderContext);
            $scope.draftDrugOrders.push(dcOrder);
            $scope.$broadcast('added-dc-order', dcOrder);
        }

        $scope.reviseOrder = function (activeOrder) {
            $scope.newDraftDrugOrder = activeOrder.createRevisionOrder();
        }


        // events

        $scope.$on('added-dc-order', function (dcOrder) {
            $timeout(function () {
                angular.element('#draft-orders input.dc-reason').last().focus();
            });
        });
        $scope.activeRegimens = [];
        $scope.components = [];
        $scope.components.quantity = [];
        $scope.setProgramRegimens = function (regimens) {
            $scope.activeRegimens = [];
            $scope.oldComponents = [];
            $scope.regimenDosingInstructions = "";
            $scope.activeRegimens = regimens;
        }
        $scope.setRegimenMembers = function (regimen) {
            $scope.components = [];
            $scope.components = regimen.components;
            orderSetId = regimen.orderSetId;
        }
        $scope.setRegimenLines = function (regimenLine) {
            $scope.regimenLines = [];
            $scope.activeRegimens = [];
            $scope.regimenLines = regimenLine;
        }
        window.drugOrderMembers = [];
        window.orderSetSelected = {};
        window.regimenDosingInstructions = null;
        $scope.saveOrderSet = function (orderset) {
            if(config.provider === '' || config.provider === undefined || config.provider === null) {
                $scope.showErrorToast ='You are not login as provider, please contact System Administrator';
                $('#orderError').modal('show');
                return;
            }
            drugOrderMembers = orderset;
            regimenDosingInstructions = $scope.regimenDosingInstructions;
            orderSetId = $scope.orderSetId;
        }
        $scope.closeModal = function() {
            $('#orderError').modal('hide');
        };
        window.activeOrderGroupUuId = null;
        window.discontinueOrderUuId = null;
        $scope.editOrderGroup = function (orderGroup) {
            $scope.showStandardRegimenTab = true;
            $scope.showRegimenPanel = true;
            $scope.showActiveTabs= false;
            $scope.showPastDrugTabs= false;
            $scope.showOtherDrugs = false;
            $scope.editRegimenTitle ="Edit Regimen Order";

            $scope.showCurrentRegimenView = false;
            $scope.components = orderGroup.components;
            $scope.regimenNames = orderGroup.name;
            activeOrderGroupUuId = orderGroup.orderGroupUuId;
            $scope.regimenStatus = "edit";
            $scope.orderSetId = orderGroup.orderSetId;
            $scope.regimenDosingInstructions = orderGroup.instructions;


            _.map($scope.patientRegimens, function (regimen) {
                if (regimen.regimenName === orderGroup.name) {
                    $scope.regimenLines = regimen.groupCodeName;
                    $scope.programName = regimen.program;

                    $scope.showRegimenPanel = true;
                }

            });

        }

        $scope.discontinueOrderGroup = function (components) {

            /*if(config.provider === '' || config.provider === undefined || config.provider === null) {
                console.log('Entering this zone of discontinue drugs',config.provider);
                $scope.showErrorToast ='You are not login as provider, please contact System Administrator';
                $('#orderError').modal('show');
                return;
            }*/
            drugOrderMembers = components;
            orderSetId = null;
            discontinueOrderUuId = null;
        }
        $scope.changeRegimen = function (currentRegimen) {
            $scope.regimenStatus = 'change';
            $scope.showRegimenPanel = true;
        }
        $scope.stopRegimen = function (regimen) {
            $scope.components = [];
            $scope.components = regimen.components;
            $scope.regimenStatus = 'stopped';
        }
        $scope.refillRegimen = function (regimen) {
            $scope.components = [];
            $scope.components = regimen.components;
            orderSetId = $scope.orderSetId;
            $scope.regimenStatus = 'active';
            $scope.showRegimenPanel = true;
        }

        $scope.matchSetMembers = function (members) {
            _.map($scope.programs.programs, function (program) {
                _.map(program.regimen_lines, function (regimenLine) {
                    _.map(regimenLine.regimens, function (regimen) {
                        var drugsFromOrderSet = $scope.createDrugsArrayFromPayload(regimen.components);
                        var drugsFromCurrentRegimen = $scope.createDrugsArrayFromPayload(members);
                        if ($scope.arraysEqual(drugsFromOrderSet, drugsFromCurrentRegimen)) {
                            orderSetId = regimen.orderSetId;
                        }
                    });
                });
            });
        }

        $scope.getCurrentRegimen = function (res) {
            if($scope.addQuantity && $scope.addQuantity[0] !== undefined) {
                $scope.components = addQuantityAndQuantityUnits(res.orderSetComponents);
            }else {
                $scope.components = res.orderSetComponents
            }
            $scope.regimenLines = res.groupCodeName;
            $scope.regimenNames = res.regimenName;
            $scope.programName = res.program;
            $scope.regimenStatus = res.regimenStatus;
            $scope.orderSetId = res.orderSetId;
            $scope.showRegimenPanel = true;
            $scope.disableButton = true

        }
        $scope.cancelView = function() {
            $scope.showRegimenPanel = false;
            $scope.disableButton = true;
            $scope.showActiveTabs = true;
            $scope.showPastDrugTabs= true;
            $scope.showOtherDrugs = true;
            $scope.showStandardRegimenTab = true;
            $scope.showCurrentRegimenView = true;

        }

        $scope.cancelViewOrderRegimen = function() {
            $scope.showRegimenPanel = false;
            $scope.disableButton = false;
            $scope.showActiveTabs = true;
            $scope.showPastDrugTabs= true;
            $scope.showOtherDrugs = true;
            $scope.showStandardRegimenTab = true;
            $scope.showCurrentRegimenView = true;

        }

        $scope.createDrugsArrayFromPayload = function (components) {
            var drugs = [];
            var i;
            for (i = 0; i < components.length; i++) {
                var drug_id = components[i].drug_id;
                if (typeof drug_id == "string") {
                    drug_id = parseInt(drug_id);
                }
                drugs.push(drug_id);
            }
            drugs.sort(function (a, b) {
                return a - b
            });
            return drugs;
        }
        $scope.arraysEqual = function (arr1, arr2) {
            if (arr1.length !== arr2.length)
                return false;
            for (var i = arr1.length; i--;) {
                if (arr1[i] !== arr2[i])
                    return false;
            }

            return true;
        }

        function formatDisplayOfRegimenInstructions(res) {
            var orders = [];
            var instructionDesc = [];
            for (var i = 0; i < res.length; ++i) {
                var data = res[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        data['instructionDetails'] = data.name +"(" + data.dose + " "+data.units_name +',' + data.frequency_name
                        +','+'Quantity:' +data.quantity +' ' +data.quantity_units_name+")" ;
                    }

                }
                orders.push(data);

            }
            var str = orders.map(function(elem){
                return elem.instructionDetails;
            }).join(" + ");
            instructionDesc.push({instructionDetailsFinal:str});

            return instructionDesc;

        }
        function formatDisplayOfPastRegimenInstructions(res) {

            var orders = [];
            var instructionDesc = [];
            for (var i = 0; i < res.length; ++i) {
                var dat = res[i].components;
                for (var t = 0; t < dat.length; ++t) {
                    var data = dat[t];
                    for (var r in data) {
                        if (data.hasOwnProperty(r)) {
                            if(data.drug_id) {
                                data['instructionDetails'] = data.name + "(" + data.dose + " " + data.units_name + ',' + data.frequency_name
                                    + ',' + 'Quantity:' + data.quantity + ' ' + data.quantity_units_name + ")";
                            }
                        }

                    }
                    orders.push(data);
                }
            }

            var grouped = _.groupBy(orders, function (o) {
                return o.order_group_id;
            });

            var valueForGroupedOrder = [];
            if (grouped) {

                Object.keys(grouped).forEach(function (key) {
                    valueForGroupedOrder = grouped[key];
                    var dateActivated = valueForGroupedOrder[0].dateActivated;
                    var dateStopped = valueForGroupedOrder[0].dateStopped;
                    var str = valueForGroupedOrder.map(function (elem) {
                        return elem.instructionDetails;
                    }).join(" + ");
                    instructionDesc.push({
                        instructionDetailsFinal: str,
                        dateActivated: dateActivated,
                        dateStopped: dateStopped
                    });

                })
            }

            return instructionDesc;

        }

        function formatDisplayOfPastSingleDrugInstructions(res) {
            var orders = [];
            for (var i = 0; i < res.length; ++i) {
                var data = res[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        data['instructionDetailsFinal'] = data.drug +"(" + data.dose + " "+data.doseUnits +',' + data.frequency
                            +','+'Quantity:' +data.quantity +' ' +data.quantityUnits+")" ;
                    }

                }
                orders.push(data);

            }
            return orders;
        }
    }]);
