<%
   // ui.decorateWith("kenyaemr", "standardPage", [patient: patient])
    ui.includeCss("kenyaemrorderentry", "labOrders.css")
   ui.includeJavascript("uicommons", "emr.js")
   ui.includeJavascript("uicommons", "angular.min.js")
   ui.includeJavascript("uicommons", "angular-app.js")
   ui.includeJavascript("uicommons", "angular-resource.min.js")
   ui.includeJavascript("uicommons", "angular-common.js")
   ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.11.2.js")
   ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
   ui.includeJavascript("kenyaemrorderentry", "bootstrap.min.js")
   ui.includeCss("kenyaemrorderentry", "bootstrap.min.css")


   ui.includeJavascript("uicommons", "filters/display.js")
   ui.includeJavascript("uicommons", "filters/serverDate.js")
   ui.includeJavascript("uicommons", "services/conceptService.js")
   ui.includeJavascript("uicommons", "services/drugService.js")
   ui.includeJavascript("uicommons", "services/encounterService.js")
   ui.includeJavascript("uicommons", "services/orderService.js")
   ui.includeJavascript("uicommons", "services/session.js")

   ui.includeJavascript("uicommons", "directives/select-concept-from-list.js")
   ui.includeJavascript("uicommons", "directives/select-order-frequency.js")
   ui.includeJavascript("uicommons", "directives/select-drug.js")
   ui.includeJavascript("kenyaemrorderentry", "order-model.js")
   ui.includeJavascript("kenyaemrorderentry", "order-entry.js")
   ui.includeJavascript("kenyaemrorderentry", "labOrders.js")
%>
<script type="text/javascript">

    window.OpenMRS = window.OpenMRS || {};
    window.OpenMRS.drugOrdersConfig = ${ jsonConfig };
    window.OpenMRS.labTestJsonPayload = ${labTestJsonPayload}

</script>

<div id="create-lab-orders" data-ng-controller="LabOrdersCtrl" ng-init='init()'>

    <div class="card">
        <div class="card-header">
            <h4 class="card-title">
                Create New Order(s)
            </h4>
        </div>

        <div class="card-body">
            <form>
                <table class="table col-lg-12">


                    <tr>
                        <td class="col-lg-3" style="width: 25%">
                            <div class="card border-dark">
                                <div class="list-group" >
                                    <div class="list-group-item" ng-repeat="lab in labOrders" style="cursor: pointer"
                                         ng-click="loadLabPanels(lab)">
                                        <div class="link-item">
                                            <a class="formLink">
                                                {{lab.name}}
                                            </a>
                                        </div>

                                    </div>
                                </div>
                            </div>

                            <div style="padding-top:10px">
                                <div class="card border-dark" >
                                    <div class="card-header">
                                        <h5 class="card-title">
                                            Selected Order(s)
                                        </h5>
                                    </div>

                                    <div class="card-body " >
                                        <div ng-show="selectedOrders.length === 0">{{noOrderSelected}}</div>

                                        <div class="list-group ">
                                            <div class="list-group-item"
                                                 ng-repeat="order in filteredOrders">
                                                <div class="link-item">
                                                    <div class="btn-group" role="group" aria-label="Basic example">
                                                        <button type="button">{{order.name}}</button>
                                                        <button type="button" class="fa fa-calendar fa-1x"
                                                                data-toggle="modal" data-target="#dateOrder"
                                                                ng-click="orderSelectedToAddDateActivated(order)"></button>
                                                        <button type="button" class="fa fa-warning fa-1x"
                                                                data-placement="top" title="Urgency"
                                                                data-toggle="modal" data-target="#orderUrgency"
                                                                ng-click="orderSelectedToAddDateActivated(order)"
                                                        ></button>
                                                        <button type="button" class="fa fa-remove fa-1x"
                                                                ng-click="deselectedOrder(order)" style="color:#9D0101;cursor: pointer"></button>
                                                    </div>

                                                </div>

                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </td>
                        <td class="col-lg-9" style="width: 75%">
                            <div class="col-lg-12">
                                <fieldset class="col-lg-12 scheduler-border">
                                    <legend class="col-lg-12 scheduler-border">Panels | <span style="background-color: pink">{{sampleTypeName}}</span></legend>

                                    <div class="row">
                                        <div class="col-lg-12">
                                            <ul>
                                                <li ng-repeat="panel in labPanels"
                                                    ng-click="loadLabPanelTests(panel)">
                                                    <button type="button" class="column">
                                                        {{panel.name}}</button>
                                                </li>
                                            </ul>
                                        </div>

                                    </div>

                                </fieldset>
                            </div>

                            <div class="col-lg-12">
                                <fieldset class="col-lg-12 scheduler-border">
                                    <legend class="col-lg-12 scheduler-border">Tests | <span style="background-color: pink">{{panelTypeName}}</span></legend>

                                    <div class="row">
                                        <div class="col-lg-12">
                                            <div ng-repeat="test in panelTests"
                                                 ng-click="getSelectedTests(test)">
                                                <div class="column">
                                                    <div class="form-group form-check">
                                                        <input class="form-check-input"
                                                               type="checkbox" id="scales"
                                                               name="feature"
                                                               ng-model='test.selected'
                                                               value="test.concept_id">
                                                        <label class="form-check-label">{{test.name}}</label>
                                                    </div>
                                                </div>

                                            </div>
                                        </div>
                                    </div>
                                </fieldset>
                            </div>

                        </td>

                    </tr>
                </table>

                <div style="padding-left: 50%">
                    <button type="button" ng-click="postLabOrdersEncounters()"
                            ng-disabled="selectedOrders.length === 0"><img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" />
                        Save orders</button>
                </div>

            </form>
        </div>



    </div>

    <!-- Modal date for lab orders -->
    <div class="modal fade" id="dateOrder" tabindex="-1" role="dialog" aria-labelledby="dateModalCenterTitle" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header modal-header-primary">
                    <h5 class="modal-title" id="dateModalCenterTitle"></h5>
                    <button type="button" class="close" data-dismiss="modal2" ng-click="closeModal()">&times;

                    </button>
                </div>
                <div class="modal-body">
                    <label >Enter Date Order was made</label>
                    <div>
                        Date: ${ ui.includeFragment("kenyaui", "field/java.util.Date", [ id: "orderDate", formFieldName: "orderDate"]) }
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" data-dismiss="modal2"   ng-click="closeModal()">Close</button>
                    <button type="button" ng-click="setOrderDate()">
                        <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
                </div>
            </div>
        </div>
    </div>
    <!-- Modal urgency for lab orders -->
    <div class="modal fade" id="orderUrgency" tabindex="-1" role="dialog" aria-labelledby="urgencyModalCenterTitle" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header modal-header-primary">
                    <h5 class="modal-title" id="urgencyModalCenterTitle"></h5>
                    <button type="button" class="close" data-dismiss="modal2" ng-click="closeModal()">&times;
                    </button>
                </div>
                <div class="modal-body ">
                    <label >Order Urgency</label>
                    <div>
                        <select id="ddlOrderUrgency" class="form-control">
                            <option value="ROUTINE"selected="selected">ROUTINE</option>
                            <option value="STAT" >IMMEDIATELY</option>
                        </select>

                    </div>
                    <div style="padding-top: 5px">
                        <label ><b>Order Reason </b></label>
                    <div>
                        Reason:
                        <select id="ddlOrderReason" class="form-control" ng-model="orderReasonCoded" >
                            <option value="{{r.uuid}}" ng-repeat=" r in OrderReason">{{r.name}}</option>

                        </select>
                        Reason(other):
                        <input class="form-control" type="text" ng-model="orderReasonNonCoded">
                    </div>
                </div>
                </div>
                <div class="modal-footer">
                    <button type="button" data-dismiss="modal2"   ng-click="closeModal()">Close</button>
                    <button type="button" ng-click="setOrderUrgency()">
                        <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
                </div>
            </div>
        </div>
    </div>

    <!-- spinner modal -->
    <div class="modal fade" id="spinner" tabindex="-1" role="dialog" aria-labelledby="spinnerModalCenterTitle" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">

                <div class="modal-body modal-header-primary">
                    <div>
                        <i class="fa fa-spinner fa-spin" style="font-size:30px"></i> Saving...
                    </div>
                </div>
            </div>
        </div>
    </div>


    <!-- general message modal -->

    <!-- general message modal -->
    <div class="modal fade" id="generalMessage" tabindex="-1" role="dialog" aria-labelledby="generalMessageModalCenterTitle" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header modal-header-warning">
                    <button type="button" class="close" data-dismiss="modal2" ng-click="closeModal()">&times;

                    </button>
                </div>

                <div class="modal-body">
                    <div>
                        Active <b>{{testName}}</b>  Order Already exits. Please check the Active Orders Tab to cancel the order and proceed.
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button"  data-dismiss="modal2" ng-click="closeModal()">Close</button>
                </div>
            </div>
        </div>
    </div>

    <!--Error Modal -->
    <div class="modal fade" id="orderError" tabindex="-1" role="dialog" style="font-size:16px;">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header modal-header-primary">
                    <h5 class="modal-title" id="exampleModalLabel">Server Error</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>

                <div class="modal-body" style="color:red;" id="modal-text">
                    {{showErrorToast}}                </div>
                <div class="modal-footer">
                    <button type="button"  data-dismiss="modal2" ng-click="closeModal()">Close</button>
                </div>
            </div>
        </div>
    </div>

</div>
<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
    angular.bootstrap('#create-lab-orders', ['labOrders']);

</script>