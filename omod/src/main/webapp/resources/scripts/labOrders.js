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
                mapTestNameAndOrderReason($scope.activeTestOrders);

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

                $scope.heiPCRTestOrderReasons = [
                    {
                        name:'Initial PCR (6week or first contact)',
                        uuid:'1040AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'2nd PCR (6 months)',
                        uuid:'1326AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'3rd PCR (12months)',
                        uuid:'844AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Confirmatory PCR and Baseline VL',
                        uuid:'162082AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    }

                ];
                $scope.heiAbTestOrderReasons = [
                    {
                        name:'Ab test 6 weeks after cessation of breastfeeding',
                        uuid:'164460AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Ab test at 18 months (1.5 years)',
                        uuid:'164860AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    }

                ];

                $scope.labOrders = labs;
                $scope.OrderReason = [
                    {
                        name:'Confirmation of treatment failure (repeat VL)',
                        uuid:'843AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Pregnancy',
                        uuid:'1434AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Baseline VL (for infants diagnosed through EID)',
                        uuid:'162080AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Single Drug Substitution',
                        uuid:'1259AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Breastfeeding',
                        uuid:'159882AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Clinical failure',
                        uuid:'163523AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name:'Routine',
                        uuid:'161236AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    },
                    {
                        name: 'Confirmation of persistent low level Viremia (PLLV)',
                        uuid: '160032AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    }
                ];
                $scope.OrderReason =  _.filter($scope.OrderReason, function(o) {
                    if(config.patient.person.gender !== 'F') {
                        return o.uuid !== '1434AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' && o.uuid !== '159882AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';
                    } else {
                        return o;
                    }
                });



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
                    $scope.panelListResults = removeHivVl($scope.panelListResults);
                    $scope.panelListResults = removeHivLdl($scope.panelListResults);
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
        var CurrentDate = new Date();
        var time = CurrentDate.getHours() + ":" + CurrentDate.getMinutes() + ":" + CurrentDate.getSeconds();

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

                        if (data.orderReasonCoded === '843AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['orderReasonCoded'] = "Confirmation of treatment failure (repeat VL) ";
                        }
                        if (data.orderReasonCoded === '1434AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' ) {
                            data['orderReasonCoded'] = "Pregnancy";
                        }
                        if (data.orderReasonCoded === '162080AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' ) {
                            data['orderReasonCoded'] = "Baseline VL (for infants diagnosed through EID)";
                        }
                        if (data.orderReasonCoded === '1259AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' ) {
                            data['orderReasonCoded'] = "Single Drug Substitution";
                        }
                        if (data.orderReasonCoded === '159882AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' ) {
                            data['orderReasonCoded'] = "Breastfeeding";
                        }
                        if (data.orderReasonCoded === '163523AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['orderReasonCoded'] = "Clinical failure";
                        }
                        if (data.orderReasonCoded === '161236AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' ) {
                            data['orderReasonCoded'] = "Routine";
                        }
                        if (data.orderReasonCoded === '160032AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['orderReasonCoded'] = "Confirmation of persistent low level Viremia (PLLV)";
                        }

                        if (data.orderReasonCoded === '1040AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['orderReasonCoded'] = "Initial PCR (6week or first contact)";
                        }
                        if (data.orderReasonCoded === '1326AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['orderReasonCoded'] = "2nd PCR (6 months)";
                        }
                        if (data.orderReasonCoded === '844AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['orderReasonCoded'] = "3rd PCR (12months)";
                        }
                        if (data.orderReasonCoded === '162082AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['orderReasonCoded'] = "Confirmatory PCR and Baseline VL";
                        }
                        if (data.orderReasonCoded === '164460AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['orderReasonCoded'] = "Ab test 6 weeks after cessation of breastfeeding";
                        }
                        if (data.orderReasonCoded === '164860AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['orderReasonCoded'] = "Ab test at 18 months (1.5 years)";
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
            if(o.display === 'HIV VIRAL LOAD') {
             return o.display !== 'HIV VIRAL LOAD, QUALITATIVE';
            }
            if(o.display === 'CD4 COUNT') {
             return o.display !== 'CD4 count result (qualitative)';
            }

            });
        }

        function mapTestNameAndOrderReason(result) {

            var orders = [];
            for (var i = 0; i < result.length; ++i) {
                var data = result[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        if (data.display ==='Tuberculosis polymerase chain reaction with rifampin resistance checking' ) {
                            data['display'] =  'GeneXpert';
                        }
                        if (data.display ==='Serum cryptococcal antigen status' ) {
                            data['display'] =  'Serum Cryptococcal Antigen (CRAG)';
                        }
                        if(data.orderReason) {
                            if (data.orderReason.uuid === '843AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Confirmation of treatment failure (repeat VL) ";
                            }
                            if (data.orderReason.uuid === '1434AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Pregnancy";
                            }
                            if (data.orderReason.uuid === '162080AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Baseline VL (for infants diagnosed through EID)";
                            }
                            if (data.orderReason.uuid === '1259AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Single Drug Substitution";
                            }
                            if (data.orderReason.uuid === '159882AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Breastfeeding";
                            }
                            if (data.orderReason.uuid === '163523AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Clinical failure";
                            }
                            if (data.orderReason.uuid === '161236AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Routine";
                            }

                            if (data.orderReason.uuid === '160032AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Confirmation of persistent low level Viremia (PLLV)";
                            }
                            if (data.orderReason.uuid === '1040AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Initial PCR (6week or first contact)";
                            }
                            if (data.orderReason.uuid === '1326AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "2nd PCR (6 months)";
                            }
                            if (data.orderReason.uuid === '844AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "3rd PCR (12months)";
                            }
                            if (data.orderReason.uuid === '162082AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Confirmatory PCR and Baseline VL";
                            }
                            if (data.orderReason.uuid === '164460AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Ab test 6 weeks after cessation of breastfeeding";
                            }
                            if (data.orderReason.uuid === '164860AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['orderReasonCoded'] = "Ab test at 18 months (1.5 years)";
                            }
                        }

                    }

                }
                orders.push(data);

            }
            return orders;
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
                        if (data.label ==='Tuberculosis polymerase chain reaction with rifampin resistance checking' ) {
                            data['label'] =  'GeneXpert';
                        }
                        if (data.label ==='Serum cryptococcal antigen status' ) {
                            data['label'] =  'Serum Cryptococcal Antigen (CRAG)';
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
            var test = filterTestWithDataTypeNA(tests.tests);
            $scope.panelTypeName = tests.name;
            $scope.showTestFields = true;
            $scope.panelTests = test;
            $scope.panelTests = customTestName($scope.panelTests);

        }

        function customTestName (res) {
            var orders = [];
            for (var i = 0; i < res.length; ++i) {
                var data = res[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {

                        if (data.name ==='Tuberculosis polymerase chain reaction with rifampin resistance checking' ) {
                            data['name'] =  'GeneXpert';
                        }
                        if (data.name ==='Serum cryptococcal antigen status' ) {
                            data['name'] =  'Serum Cryptococcal Antigen (CRAG)';
                        }

                    }

                }
                orders.push(data);

            }
            return orders;

        }
        $scope.deselectedOrder = function(order) {
            order.selected = false;
            var unchecked = _.filter($scope.filteredOrders, function(o) {
                return o.concept_id !== order.concept_id;
            });
            $scope.filteredOrders = unchecked;
            $scope.selectedOrders = $scope.filteredOrders;
            $scope.generateLabOrdersSummaryView();


        }
        $scope.labOrdersTests = [];
        $scope.selectedOrders = [];
        $scope.noOrderSelected ='None';
        $scope.filteredOrders = [];
        $scope.getSelectedTests = function(tests) {

            if(tests.selected === true) {
                checkIfSelectedTestIsActiveOrder(tests);
                customizeOrderReasonsToDisplay(tests);
                $scope.selectedOrders.push(tests);
                $scope.filteredOrders = _.uniq($scope.selectedOrders);
                $scope.filteredOrders = addDefaultDateAndUrgency($scope.filteredOrders);

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

        function customizeOrderReasonsToDisplay(test) {
            // Antibody test
            if (test.concept === '163722AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'&& config.patient.person.age <= 5) {
                $scope.OrderReason = $scope.heiAbTestOrderReasons;
            }
            // PCR test
            if (test.concept === '1030AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'&& config.patient.person.age <= 5) {
                $scope.OrderReason = $scope.heiPCRTestOrderReasons;
            }
        }
        
        function addDefaultDateAndUrgency(res) {
            var today = new Date();
            var dd = today.getDate();
            var mm = today.getMonth() + 1; //January is 0!
            var yyyy = today.getFullYear();

            if (dd < 10) {
                dd = '0' + dd;
            }

            if (mm < 10) {
                mm = '0' + mm;
            }

            today = yyyy + '-' + mm + '-' + dd;
            var orders = [];
            for (var i = 0; i < res.length; ++i) {
                var data = res[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {

                        if (data.concept_id ) {
                            data['dateActivated'] =  today.toString();
                            data['encounterDatetime'] =  today.toString();
                            data['urgency'] = 'ROUTINE';
                        }

                    }

                }
                orders.push(data);

            }
            return orders;

        }

        $scope.postLabOrdersEncounters = function() {
            if(config.provider === '' || config.provider === undefined || config.provider === null) {
                $scope.showErrorToast ='You are not login as provider, please contact System Administrator';
                $('#orderError').modal('show');
                return;
            }
            var uuid = {uuid:"e1406e88-e9a9-11e8-9f32-f2801f1b9fd1"};
            $scope.OrderUuid = '';


            $scope.lOrders = createLabOrdersPaylaod($scope.filteredOrders);
            $scope.lOrdersPayload = angular.copy( $scope.lOrders);
            for (var i = 0; i < $scope.lOrdersPayload.length; ++i) {
                $scope.encounterDatetime = $scope.lOrdersPayload[i].encounterDatetime;
                delete $scope.lOrdersPayload[i].concept_id;
                delete $scope.lOrdersPayload[i].dataType;
                delete $scope.lOrdersPayload[i].name;
                delete $scope.lOrdersPayload[i].orderReasonCodedName;
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

            var checkVlOrderReason = _.filter($scope.lOrdersPayload, function(o) {
                return o.concept ==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                    || o.concept ==='1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';
            });
            var checkCd4OrderReason = _.filter($scope.lOrdersPayload, function(o) {
                return o.concept ==='730AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'

            });

            var checkCd4CountOrderReason = _.filter($scope.lOrdersPayload, function(o) {
                return o.concept ==='5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'

            });


            if(checkVlOrderReason && checkVlOrderReason[0]) {
                if ((checkVlOrderReason[0].orderReasonNonCoded === '' || checkVlOrderReason[0].orderReasonNonCoded === null
                    || checkVlOrderReason[0].orderReasonNonCoded === undefined) && (checkVlOrderReason[0].orderReason === ''
                    || checkVlOrderReason[0].orderReason === null || checkVlOrderReason[0].orderReason === undefined)) {
                    $scope.showErrorToast = 'Order reason for HIV viral load is required';

                    $('#orderError').modal('show');
                    return;
                }
            }

            if(checkCd4CountOrderReason && checkCd4CountOrderReason[0]) {
                if ((checkCd4CountOrderReason[0].orderReasonNonCoded === '' || checkCd4CountOrderReason[0].orderReasonNonCoded === null ||
                    checkCd4CountOrderReason[0].orderReasonNonCoded === undefined) && (checkCd4CountOrderReason[0].orderReason === ''
                    || checkCd4CountOrderReason[0].orderReason === null || checkCd4CountOrderReason[0].orderReason === undefined)) {

                    $scope.showErrorToast = 'Order reason for CD4 Count is required';

                    $('#orderError').modal('show');
                    return;
                }
            }

            if(checkCd4OrderReason && checkCd4OrderReason[0]) {
                if ((checkCd4OrderReason[0].orderReasonNonCoded === '' || checkCd4OrderReason[0].orderReasonNonCoded === null
                    || checkCd4OrderReason[0].orderReasonNonCoded === undefined) && (checkCd4OrderReason[0].orderReason === ''
                    || checkCd4OrderReason[0].orderReason === null || checkCd4OrderReason[0].orderReason === undefined)) {

                    $scope.showErrorToast = 'Order reason for CD4% is required';

                    $('#orderError').modal('show');
                    return;
                }
            }

            $('#confirmation-dailog').modal('hide');
            $('#spinner').modal('show');

            var newOrders = _.filter($scope.lOrdersPayload, function(o) {
                return !o.dateActivated;
            });

            $scope.groupedVlOrders = _.filter($scope.lOrdersPayload, function(o) {
                return o.concept ==="856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" || o.concept ==="1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
            });

            $scope.otherOrdersExcludingVL = removeHivVl($scope.lOrdersPayload);
            $scope.otherOrdersNotVL = removeHivLdl($scope.otherOrdersExcludingVL);

            if(!_.isEmpty($scope.groupedVlOrders)) {
                $scope.loading = true;
                for (var i = 0; i < $scope.groupedVlOrders.length; ++i) {
                    $scope.encounterDatetime = $scope.groupedVlOrders[i].dateActivated;
                }
                var encounterContextForVL = {
                    patient: config.patient,
                    encounterType: uuid,
                    encounterDatetime:  $scope.encounterDatetime,
                    encounterRole: config.encounterRole
                };
                OrderEntryService.signAndSave({ draftOrders: $scope.groupedVlOrders }, encounterContextForVL)
                    .$promise.then(function(result) {
                    $('#spinner').modal('hide');
                    loadExistingOrders();
                    $window.location.reload();
                    location.href = location.href;
                }, function(errorResponse) {
                    $('#spinner').modal('hide');
                    emr.errorMessage(errorResponse.data.error.message);
                    $scope.loading = false;
                });

            }


            if(!_.isEmpty($scope.otherOrdersNotVL)) {
                $scope.loading = true;
                _.each($scope.otherOrdersNotVL, function(o) {

                    if (o.dateActivated) {
                        var encounterContextOldOrders = {};
                        $scope.oldOrdrs = [];
                        for (var property in o) {
                            if (o.hasOwnProperty(property)) {
                                if(property ==='dateActivated') {
                                    encounterContextOldOrders = {
                                        patient: config.patient,
                                        encounterType: uuid,
                                        location: null, // TODO
                                        encounterDatetime: o.dateActivated,
                                        encounterRole: config.encounterRole
                                    };
                                    $scope.oldOrdrs.push(o);
                                    OrderEntryService.signAndSave({ draftOrders: $scope.oldOrdrs }, encounterContextOldOrders)
                                        .$promise.then(function(result) {
                                        $('#spinner').modal('hide');
                                        loadExistingOrders();
                                        $window.location.reload();
                                        location.href = location.href;
                                    }, function(errorResponse) {
                                        $('#spinner').modal('hide');
                                        emr.errorMessage(errorResponse.data.error.message);
                                        $scope.loading = false;
                                    });
                                }

                            }
                        }

                    }

                });

            }


            if(!_.isEmpty(newOrders)) {
                $scope.loading = true;
                OrderEntryService.signAndSave({ draftOrders: newOrders }, encounterContext)
                    .$promise.then(function(result) {
                    $('#spinner').modal('hide');
                    loadExistingOrders();
                    $window.location.reload();
                    location.href = location.href;
                }, function(errorResponse) {
                    $('#spinner').modal('hide');
                    emr.errorMessage(errorResponse.data.error.message);
                    $scope.loading = false;
                });

            }

        }


        function createLabOrdersPaylaod(selectedOrders) {
            var orders = [];

            for (var i = 0; i < selectedOrders.length; ++i) {
                var vl = {};
                var cd4Qualitative = {};
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
                            orderReason:$scope.orderReasonCoded,
                            dateActivated:data.dateActivated

                        }
                    }
                      if(data.concept ==='5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {

                        // create cd4 qualitative
                        cd4Qualitative = {
                            orderer:config.provider.uuid,
                            careSetting:$scope.careSetting.uuid,
                            type:"testorder",
                            concept:"d0a3677f-3b3a-404c-9010-6ec766d7072e",
                            concept_id: 167718,
                            orderReasonNonCoded:$scope.orderReasonNonCoded,
                            orderReason:$scope.orderReasonCoded,
                            dateActivated:data.dateActivated

                        }
                      }
                }
                orders.push(data);
                orders.push(vl);
                orders.push(cd4Qualitative);

            }
            orders =_.uniq(orders);
            var filterOrders = _.filter(orders, function(o) {

                return Object.keys(o).length !== 0;
            });
            return filterOrders;

        }

        $scope.orderSelectedToAddDateActivated = function(order) {
            $scope.titleDate ='Enter Date Order was made';
            $scope.orderReasonNonCoded = '';
            $scope.orderReasonCoded = '';
            $scope.orderDate = '';
            $scope.orderSel = order;
            $scope.orderUrgency = order;


        }

        // The start of test result rendering components
        $scope.typeValues = {};
        $scope.postLabOrderResults = function() {
            var obsDate ='';
            $scope.obsPayload = hvVl($scope.labResultsRaw);
            if ($scope.obsPayload.length === 0) {
                $scope.showErrorToast = 'You have not filled any results to post';

                $('#orderError').modal('show');
                return;
            }
            $('#spinner').modal('show');
            $scope.discontinueFilledOrders = angular.copy($scope.obsPayload);
            $scope.discontinueFilledOrders = _.filter($scope.discontinueFilledOrders, function(o) {
                return !o.obsDatetime;
            });


            $scope.discontinueOrdersRetrospectively = angular.copy($scope.obsPayload);
            for (var i = 0; i < $scope.obsPayload.length; ++i) {
                obsDate = $scope.obsPayload[i].dateActivated;
                delete $scope.obsPayload[i].label;
                delete $scope.obsPayload[i].orderId;
                delete $scope.obsPayload[i].orderUuid;
                delete $scope.obsPayload[i].answers;
                delete $scope.obsPayload[i].$$hashKey;
                delete $scope.obsPayload[i].rendering;
                delete $scope.obsPayload[i].hivVl;
                delete $scope.obsPayload[i].name;
                delete $scope.obsPayload[i].dateActivated;
                delete $scope.obsPayload[i].dateStopped;
            }
            var uuid = {uuid:"e1406e88-e9a9-11e8-9f32-f2801f1b9fd1"};
            var encounterContext = {
                patient: config.patient,
                encounterType: uuid,
                location: null, // TODO
                encounterDatetime: obsDate,
                encounterRole: config.encounterRole
            };
            var newObs = _.filter($scope.obsPayload, function(o) {
                return !o.obsDatetime;
            });

            _.each($scope.obsPayload, function(o) {

                if (o.obsDatetime) {
                    var encounterContextOldOrders = {};
                    $scope.oldObResults = [];
                    $scope.obs = [];

                    for (var property in o) {
                        if (o.hasOwnProperty(property)) {
                            if(property ==='obsDatetime') {
                                $scope.oldObResults.push(o);
                                _.each($scope.oldObResults, function(r) {
                                    encounterContextOldOrders = {
                                        patient: config.patient,
                                        encounterType: uuid,
                                        location: null, // TODO
                                        encounterDatetime: r.obsDatetime,
                                        encounterRole: config.encounterRole
                                    };
                                    $scope.obs.push(r);
                                    OrderEntryService.signAndSave({ draftOrders: [] }, encounterContextOldOrders,$scope.obs)
                                        .$promise.then(function(result) {
                                        if($scope.OrderUuid) {
                                           $scope.voidSelectedOrders();
                                        }

                                        $scope.discontinueOrdersRetrospectively = _.filter($scope.discontinueOrdersRetrospectively, function(o) {
                                            var obs =$.datepicker.formatDate(dateFormat, new Date(o.obsDatetime));
                                            var today =$.datepicker.formatDate(dateFormat, new Date());
                                            return obs !== today ;
                                        });
                                        $scope.discontinueOrdersRetrospectively = checkForDateActivated($scope.discontinueOrdersRetrospectively);
                                        discontinueLabOrdersRetrospectivelyAndPoc($scope.discontinueOrdersRetrospectively);
                                        $('#spinner').modal('hide');

                                         location.href = location.href;
                                    }, function(errorResponse) {
                                        $('#spinner').modal('hide');
                                        emr.errorMessage(errorResponse.data.error.message);
                                        $scope.loading = false;
                                    });

                                });

                            }

                        }
                    }

                }

            });

            if(!_.isEmpty(newObs)) {
                $scope.loading = true;
                newObs.forEach(function (element) {
                    element.obsDatetime = obsDate;
                });
                $scope.discontinueFilledOrders.forEach(function (element) {
                    element.dateActivated = CurrentDate;
                });

                OrderEntryService.signAndSave({ draftOrders: [] }, encounterContext, newObs)
                    .$promise.then(function(result) {
                    if($scope.OrderUuid) {
                        $scope.voidSelectedOrders();
                    }

                    $scope.discontinueFilledOrders = _.filter($scope.discontinueFilledOrders, function(o) {
                        var dateActivated =$.datepicker.formatDate(dateFormat, new Date(o.dateActivated));
                        var today =$.datepicker.formatDate(dateFormat, new Date());
                        return dateActivated === today ;
                    });
                    $scope.discontinueOrdersRetrospectively = checkForDateActivated($scope.discontinueOrdersRetrospectively);
                    discontinueLabOrdersRetrospectivelyAndPoc($scope.discontinueOrdersRetrospectively);
                    $('#spinner').modal('hide');
                      location.href = location.href;
                }, function(errorResponse) {
                    emr.errorMessage(errorResponse.data.error.message);
                    $scope.loading = false;
                });
            }



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
             var reasonForVoidingOrder = document.getElementById("ddlvoidReason");
             $scope.voidOrderReason = reasonForVoidingOrder.options[reasonForVoidingOrder.selectedIndex].value;

             if ($scope.voidOrderReason === '' || $scope.voidOrderReason === null ||
                 $scope.voidOrderReason === undefined) {
                 $scope.showErrorToast = 'Void reason is required';
                 $('#orderError').modal('show');
                 return;
             }

             for (var i = 0; i < $scope.activeTestOrdersForHvVl.length; ++i) {
                var data = $scope.activeTestOrdersForHvVl[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {
                        if(data.concept.uuid === '1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            $scope.OrderUuidHvl = data.uuid;
                        }
                        if(data.concept.uuid === 'd0a3677f-3b3a-404c-9010-6ec766d7072e') {
                            $scope.OrderUuidHvl = data.uuid;
                        }

                    }

                }

             }
            var voidOrderPayload ={
                voided: true,
                voidReason: $scope.voidOrderReason
            };

            $scope.loading = true;
            OrderEntryService.saveVoidedOrders(voidOrderPayload, $scope.OrderUuidHvl,$scope.voidOrderReason)
                .then(function(result) {
                 location.href = location.href;
            }, function(errorResponse) {
                location.href = location.href;
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


                        if(data.concept==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['order'] = data.orderUuid;
                            data['value'] = $scope.hivViralValues[data.orderId];
                            data['concept'] = data.concept;
                            data['encounter'] = data.encounter;

                        }
                        else if(data.concept==='1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            data['order'] = data.orderUuid;
                            data['value'] = $scope.hivViralValuesLDL[data.orderId];
                            data['concept'] = data.concept;
                            data['encounter'] = data.encounter;

                        } else {
                            data['order'] = data.orderUuid;
                            data['value'] =  $scope.typeValues[data.orderId];
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
                delete $scope.lOrders[i].obsDatetime;
                delete $scope.lOrders[i].hivVl;
            }

            var uuid = {uuid:"e1406e88-e9a9-11e8-9f32-f2801f1b9fd1"};
            var encounterContext = {
                patient: config.patient,
                encounterType: uuid,
                location: null, // TODO
                encounterRole: config.encounterRole
            };

            $scope.loading = true;
            OrderEntryService.signAndSave({ draftOrders: $scope.lOrders }, encounterContext)
                .$promise.then(function(result) {
                location.href = location.href;
            }, function(errorResponse) {
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
        $scope.voidOrderReason = '';
        $scope.OrderUuid = '';
        $scope.getOrderUuid = function(order) {
            $scope.OrderUuid = order.uuid;
            $scope.ldlDateActivated = order.dateActivated;
            $scope.orderConcept = order.concept.uuid;

        }
        $scope.voidAllHivViralLoadOrders = function () {
            $scope.voidActiveLabOrders();
            if($scope.orderConcept ==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA' || $scope.orderConcept ==='5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                cancelOrder();
            }
        }


        $scope.voidActiveLabOrders = function() {
            var reasonForVoidingOrder = document.getElementById("ddlvoidReason");
            $scope.voidOrderReason = reasonForVoidingOrder.options[reasonForVoidingOrder.selectedIndex].value;
            if ($scope.voidOrderReason === '' || $scope.voidOrderReason === null ||
                $scope.voidOrderReason === undefined) {
                $scope.showErrorToast = 'Void reason is required';
                $('#orderError').modal('show');
                return;
            }

            var voidOrderPayload ={
                voided: true,
                voidReason: $scope.voidOrderReason
            };
            $scope.loading = true;
            OrderEntryService.saveVoidedOrders(voidOrderPayload, $scope.OrderUuid,$scope.voidOrderReason)
                .then(function(result) {

                $('#voidOrdersModal').modal('hide');

                location.href = location.href;
            }, function(errorResponse) {
                $('#voidOrdersModal').modal('hide');
                location.href = location.href;
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });

        };

        $scope.closeModal = function() {
            $scope.voidOrderReason = '';

            $('#orderUrgency').modal('hide');
            $('#generalMessage').modal('hide');
            $('#voidOrdersModal').modal('hide');
            $('#orderError').modal('hide');
            $('#orderWarning').modal('hide');
        };
        $scope.closeDateOrderModal = function () {
            $scope.orderDate = '';
            angular.element('#orderDate').val('');
            $('#dateOrder').modal('hide');
            $('#dateOrderVl').modal('hide');
        }
        $scope.dateActivatedForLdl = '';

        $scope.orderSel = [];
        $scope.orderResultSel = [];
        $scope.labResultsRaw = [];
        var dateFormat = "yy-mm-dd";
        $scope.setOrderDate = function() {
            $scope.orderDate = angular.element('#orderDate').val();
            $scope.GivenDate = new Date($scope.orderDate);
            $scope.resultDateSelected = new Date($scope.dateOrderDone);

            if($scope.GivenDate > CurrentDate){
                $scope.showErrorToast = 'Selected date is greater than the current date.';

                $('#orderError').modal('show');
                return;
            }
            if($scope.GivenDate < $scope.resultDateSelected){
                $scope.showErrorToast = 'Result date cannot be before the order date.';

                $('#orderError').modal('show');
                return;
            }
            var initialOrderDate = $.datepicker.formatDate(dateFormat, new Date($scope.initialOrderDate));



            $scope.dateActivatedForLdl = $scope.orderDate.substring(0, 10);
            $scope.orderSel['dateActivated'] =  $scope.orderDate.substring(0, 10);
            $scope.orderSel['encounterDatetime'] =  $scope.orderDate.substring(0, 10);
            $scope.orderResultSel['obsDatetime'] =  initialOrderDate;
            $scope.orderResultSel['dateActivated'] =  $scope.orderDate !== undefined ? $scope.orderDate.substring(0, 10).concat(' ' +time):CurrentDate;
            
            $scope.filteredOrders.push($scope.orderSel);
            $scope.labResultsRaw.push($scope.orderResultSel);
            $scope.filteredOrders = _.uniq($scope.filteredOrders);
            $scope.labResultsRaw = _.uniq($scope.labResultsRaw);
            $scope.generateLabOrdersSummaryView();

            $('#dateOrder').modal('hide');

        };

        $scope.orderReasonNonCoded = '';
        $scope.orderReasonCoded = '';

        $scope.setOrderUrgency = function() {
            var e = document.getElementById("ddlOrderUrgency");
            $scope.orderUrgency['urgency'] =  e.options[e.selectedIndex].value;
            $scope.orderUrgency['orderReasonNonCoded'] =  $scope.orderReasonNonCoded;
            $scope.orderUrgency['orderReason'] =  $scope.orderReasonCoded;

            _.each($scope.OrderReason, function(o) {
                if (o.uuid === $scope.orderReasonCoded) {
                    $scope.name = o.name;

                }
                $scope.orderUrgency['orderReasonCodedName'] = $scope.name + ',' + $scope.orderReasonNonCoded;

            });
            $scope.filteredOrders.push($scope.orderUrgency);
            $scope.filteredOrders = _.uniq($scope.filteredOrders);
            $scope.generateLabOrdersSummaryView();
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

        $scope.generateLabOrdersSummaryView = function () {
            $scope.viewSummaryLabs= $scope.filteredOrders;
        }

        $scope.closeConfirmationDialogModal = function() {
            $('#confirmation-dailog').modal('hide');
        };

        $scope.editOrderResultsDialog = function (res) {
            $scope.orderUuid = res.orderUuid;
            $scope.isEditLdLResults = false;
            $scope.isEditLdLOrVlResults = false;
            $scope.dateActivated =res.dateActivated;
            $scope.orderReasonNonCoded = res.orderReasonNonCoded;
            $scope.orderReasonCoded = res.orderReason;
            $scope.obsValue = '';
            $scope.data = {};
            $scope.ObsUuid = res.obsUuid;
            $scope.obsConcept = res.concept;
            $scope.obsDatetime = res.resultDate;
            $scope.orderName = res.name;
            $scope.valueCodedResults = res.valueCoded;
            $scope.valueNumericResults = res.valueNumeric;
            $scope.valueTextResults = res.valueText;
            if($scope.valueCodedResults) {
                fetchConceptAnswers(res.concept);
            }
            if(res.concept ==='1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                $scope.valueCodedResults = null;
                $scope.isEditLdLResults = true

            }
            if(res.concept ==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                $scope.valueNumericResults = null;
                $scope.isEditLdLOrVlResults = true;

            }

        }
        function fetchConceptAnswers(conceptUuid) {
            $scope.answers = [];
                OrderEntryService.getConceptAnswers(conceptUuid)
                    .then(function(posts) {
                        if (conceptUuid === '1029AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                            $scope.answers = [{
                                display:'NEGATIVE',
                                uuid:'664AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                            }, {
                                display:'POSITIVE',
                                uuid:'703AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                            }]
                        }
                        else if (conceptUuid === 'd0a3677f-3b3a-404c-9010-6ec766d7072e') {
                             $scope.answers = [{
                                 display:'CD4 COUNT GREATER THAN 200',
                                 uuid:'1254AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'
                             }, {
                                 display:'CD4 count less than or equal to 200',
                                 uuid:'9395a2a8-21e9-4e06-bd2f-24c52f56cd21'
                             }]
                                                 }

                        else {
                            $scope.answers = posts.answers;

                        }
                    });


        }

        $scope.closeEditResultsDialogModal = function() {
            $scope.answers = [];
            $scope.obsConcept = '';
            $('#editOrderResults').modal('hide');
        };
        $scope.data = {
            singleSelect: null
        };
        $scope.obsValue = '';
        var editLdlPayload = {};


        $scope.isEditToggleSelection = function () {
            if ($scope.flag === false || $scope.flag === undefined) {
                $scope.flag = true;
                $scope.ischecked='yes';
                $scope.hivViralValues ={};
            }else {
                $scope.flag = false;
                $scope.ischecked=' ';
            }
            $scope.ldlValue ='ldl';
        }


        $scope.voidSelectedOrders = function() {
            var voidOrderPayload ={
                voided: true,
                voidReason: $scope.voidOrderReason
            };
            $scope.loading = true;
            OrderEntryService.purgOrders(voidOrderPayload, $scope.OrderUuid)
                .then(function(result) {

                    $('#voidOrdersModal').modal('hide');

                    location.href = location.href;
                }, function(errorResponse) {
                    $('#voidOrdersModal').modal('hide');
                    location.href = location.href;
                    emr.errorMessage(errorResponse.data.error.message);
                    $scope.loading = false;
                });

        };

        $scope.updateLabResults = function() {
            if($scope.valueNumericResults) {
                $scope.obsValue = angular.element('#numericResults').val();
            }
            if($scope.valueTextResults) {
                $scope.obsValue = angular.element('#textResults').val();
            }
            if($scope.data.singleSelect) {
                $scope.obsValue = $scope.data.singleSelect;
            }
            if($scope.obsConcept ==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                $scope.obsValue = angular.element('#editVload').val();
            }
            if($scope.isEditLdLResults === true) {
                 editLdlPayload = {
                    orderer: config.provider.uuid,
                    careSetting: $scope.careSetting.uuid,
                    type:"testorder",
                    concept: '856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                    orderReasonNonCoded:$scope.orderReasonNonCoded,
                    orderReason:$scope.orderReasonCoded,
                    dateActivated:$scope.dateActivated
                }
                createAdhocViralLoadOrders(editLdlPayload);
                 return
            }
            if($scope.isEditLdLOrVlResults === true && $scope.ldlValue ==='ldl') {
                editLdlPayload = {
                    orderer: config.provider.uuid,
                    careSetting: $scope.careSetting.uuid,
                    type:"testorder",
                    concept: '1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                    orderReasonNonCoded:$scope.orderReasonNonCoded,
                    orderReason:$scope.orderReasonCoded,
                    dateActivated:$scope.dateActivated
                }
                createAdhocViralLoadOrders(editLdlPayload);
                return;

            }

                var obsPayload = {
                    obsDatetime:$scope.obsDatetime,
                    value:$scope.obsValue.toString(),
                    concept:$scope.obsConcept,
                    person:config.patient.uuid
                };

                JSON.stringify(obsPayload);
                $scope.loading = true;
                OrderEntryService.updateLabResults(obsPayload, $scope.ObsUuid)
                    .then(function(result) {

                        $('#editOrderResults').modal('hide');

                        location.href = location.href;
                    }, function(errorResponse) {
                        $('#editOrderResults').modal('hide');
                          location.href = location.href;
                        emr.errorMessage(errorResponse.data.error.message);
                        $scope.loading = false;
                    });


        };
        var uuid = {uuid:"e1406e88-e9a9-11e8-9f32-f2801f1b9fd1"};

        function createAdhocViralLoadOrders (o) {
            $scope.oldOrdrs = [];
            var encounterContextOldOrders = {
                patient: config.patient,
                encounterType: uuid,
                location: null, // TODO
                encounterDatetime: o.dateActivated,
                encounterRole: config.encounterRole
            };

            $scope.oldOrdrs.push(o);
            OrderEntryService.signAndSave({draftOrders: $scope.oldOrdrs}, encounterContextOldOrders)
                .$promise.then(function (result) {
                postAdhocViralResults(result);
                $('#spinner').modal('hide');
                 $window.location.reload();
                 location.href = location.href;
            }, function (errorResponse) {
                $('#spinner').modal('hide');
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });
        }
        $scope.obsVlValueToEdit = '';
        var obsPayload = {};

        function postAdhocViralResults(res) {
            $scope.obsVlValueToEdit = angular.element('#numeriVl').val();
            var obs = [];
            $scope.discontinueOrdersForVil = [];
            var encounterContext = {
                patient: config.patient,
                encounterType: uuid,
                location: null, // TODO
                encounterDatetime: res.encounterDatetime,
                encounterRole: config.encounterRole
            };
            if($scope.isEditLdLResults === true) {
                obsPayload = {
                    obsDatetime:res.encounterDatetime,
                    value: $scope.obsVlValueToEdit.toString(),
                    concept: '856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                    person:config.patient.uuid,
                    encounter:res.uuid,
                    order:res.orders[0].uuid
                };
            }else {
                obsPayload = {
                    obsDatetime:res.encounterDatetime,
                    value: '1302AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                    concept: '1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                    person:config.patient.uuid,
                    encounter:res.uuid,
                    order:res.orders[0].uuid
                };
            }


            obs.push(obsPayload);
            JSON.stringify(obs);
            OrderEntryService.signAndSave({ draftOrders: [] }, encounterContext,obs )
                .$promise.then(function(result) {
                var voidObsPayload ={
                    voided: true
                };

                $scope.loading = true;
                OrderEntryService.voidObs(voidObsPayload, $scope.ObsUuid)
                    .then(function(result) {
                        location.href = location.href;
                    }, function(errorResponse) {
                        location.href = location.href;
                        emr.errorMessage(errorResponse.data.error.message);
                        $scope.loading = false;
                    });
                if($scope.isEditLdLResults === true) {
                    $scope.discontinueOrdersForVil.push({
                        orderUuid:res.orders[0].uuid,
                        concept:'856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                        encounter:res.uuid
                    });
                }
                if($scope.isEditLdLOrVlResults === true) {
                    $scope.discontinueOrdersForVil.push({
                        orderUuid:res.orders[0].uuid,
                        concept:'1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA',
                        encounter:res.uuid
                    });
                }



                discontinueLabTestOrders($scope.discontinueOrdersForVil);
                $('#editOrderResults').modal('hide');
                $('#spinner').modal('hide');


                location.href = location.href;
            }, function(errorResponse) {
                emr.errorMessage(errorResponse.data.error.message);
                $scope.loading = false;
            });

        }

        function filterTestWithDataTypeNA(test) {
            return _.filter(test, function(o) {

                return o.dataType !== "N/A";
            });

        }
        $scope.deleteOrderResultsDialog = function (past) {
            $scope.orderName = past.name;

            $scope.showErrorToast ='You are about to delete Entry for' + ' ' +$scope.orderName+ ' '+
                'results. Do you want to proceed?';
            $scope.ObsUuid = past.obsUuid;


        }

        $scope.deleteLabOrderResults = function () {
            var voidObsPayload ={
                voided: true
            };

            $scope.loading = true;
            OrderEntryService.voidObs(voidObsPayload, $scope.ObsUuid)
                .then(function(result) {
                    $('#orderWarning').modal('hide');
                    location.href = location.href;
                }, function(errorResponse) {
                    location.href = location.href;
                    emr.errorMessage(errorResponse.data.error.message);
                    $scope.loading = false;
                });
        }


        $scope.orderSelectedToAddResultsDate = function(order) {
            $scope.dateOrderDone = order.dateActivated;
            $scope.titleDate ='Enter Results Date';
            $scope.orderResultSel = order;
            $scope.initialOrderDate= order.dateActivated
        }

        $scope.orderSelectedToAddResultsDateForVl = function(order) {
            $scope.dateOrderDone = order.dateActivated;
            $scope.titleDate ='Enter Results Date';
            $scope.orderResulForVl = order;
            $scope.initialOrderDateForVl= order.dateActivated
        }

        $scope.setResultDateForViralLoad = function () {
            var orders = [];
            $scope.orderDate = angular.element('#dateOrderVl').val();
            var initialOrderDateVl = $.datepicker.formatDate(dateFormat, new Date($scope.initialOrderDateForVl));


            $scope.GivenDate = new Date($scope.orderDate);
            $scope.resultDateSelected = new Date($scope.dateOrderDone);

            if($scope.GivenDate > CurrentDate){
                $scope.showErrorToast = 'Selected date is greater than the current date.';

                $('#orderError').modal('show');
                return;
            }
            if($scope.GivenDate < $scope.resultDateSelected){
                $scope.showErrorToast = 'Result date can not be before the order date.';

                $('#orderError').modal('show');
                return;
            }

            if($scope.orderDate === ''){
                $scope.showErrorToast = 'Result date is empty';

                $('#orderError').modal('show');
                return;
            }
            if($scope.orderDate) {

                for (var i = 0; i < $scope.labResultsRaw.length; ++i) {
                    var data = $scope.labResultsRaw[i];

                    for (var r in data) {
                        if (data.hasOwnProperty(r)) {

                            if (data.concept === '856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['obsDatetime'] = initialOrderDateVl;
                                data['dateActivated'] =  $scope.orderDate !== undefined ? $scope.orderDate.substring(0, 10).concat(' ' +time):CurrentDate;


                            }
                            if (data.concept === '1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA') {
                                data['obsDatetime'] = initialOrderDateVl;
                                data['dateActivated'] =  $scope.orderDate !== undefined ? $scope.orderDate.substring(0, 10).concat(' ' +time):CurrentDate;


                            }

                        }
                    }
                    orders.push(data);
                }
            }
            $scope.labResultsRaw = orders;
            $('#dateOrderVl').modal('hide');

        }

        function discontinueLabOrdersRetrospectivelyAndPoc(completedFields) {
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
            var uuid = {uuid:"e1406e88-e9a9-11e8-9f32-f2801f1b9fd1"};

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
                delete $scope.lOrders[i].obsDatetime;
                delete $scope.lOrders[i].hivVl;
            }


            _.each($scope.lOrders, function(o) {

                if (o.dateActivated) {
                    var encounterContextOldOrders = {};
                    $scope.oldOrdrs = [];
                    for (var property in o) {
                        if (o.hasOwnProperty(property)) {
                            if(property ==='dateActivated') {
                                encounterContextOldOrders = {
                                    patient: config.patient,
                                    encounterType: uuid,
                                    location: null, // TODO
                                    encounterDatetime: o.dateActivated,
                                    encounterRole: config.encounterRole
                                };
                                $scope.oldOrdrs.push(o);
                                OrderEntryService.signAndSave({ draftOrders: $scope.oldOrdrs }, encounterContextOldOrders)
                                    .$promise.then(function(result) {
                                    $('#spinner').modal('hide');
                                    loadExistingOrders();
                                }, function(errorResponse) {
                                    $('#spinner').modal('hide');
                                    emr.errorMessage(errorResponse.data.error.message);
                                    $scope.loading = false;
                                });
                            }

                        }
                    }

                }

            });

        }

        function checkForDateActivated (res) {
            var orders = [];
            for (var i = 0; i < res.length; ++i) {
                var data = res[i];

                for (var r in data) {
                    if (data.hasOwnProperty(r)) {

                        if (data.dateActivated instanceof Date) {
                            data['dateActivated'] = CurrentDate ;
                        }

                    }

                }
                orders.push(data);

            }
            return orders;

        }





    }]);
