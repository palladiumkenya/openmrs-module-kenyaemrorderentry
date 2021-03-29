<%
    ui.decorateWith("kenyaemr", "standardPage", [patient: patient])
    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to Client home", href: ui.pageLink("kenyaemr", "clinician/clinicianViewPatient", [patient: patient, patientId: patient])]
    ]

    ui.includeJavascript("uicommons", "emr.js")
    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.11.2.js")
    ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
    ui.includeJavascript("kenyaemrorderentry", "bootstrap.min.js")


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

    ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
    ui.includeCss("kenyaemrorderentry", "drugOrders.css")
    ui.includeCss("uicommons", "styleguide/jquery-ui-1.9.2.custom.min.css")
    ui.includeCss("kenyaemrorderentry", "index.css")


    ui.includeCss("kenyaemrorderentry", "bootstrap.min.css")
    ui.includeCss("kenyaemrorderentry", "labOrders.css")
    ui.includeCss("kenyaemrorderentry", "font-awesome.css")
    ui.includeCss("kenyaemrorderentry", "font-awesome.min.css")
    ui.includeCss("kenyaemrorderentry", "font-awesome.css.map")
    ui.includeCss("kenyaemrorderentry", "fontawesome-webfont.svg")
%>
<style type="text/css">
#new-order input {
    margin: 5px;
}

th, td {
    text-align: left;
}
</style>
<script type="text/javascript">


    window.OpenMRS = window.OpenMRS || {};
    window.OpenMRS.drugOrdersConfig = ${ jsonConfig };
    window.OpenMRS.labTestJsonPayload = ${labTestJsonPayload}
    window.OpenMRS.enterLabOrderResults =${enterLabOrderResults}
    window.OpenMRS.pastLabOrdersResults =${pastLabOrdersResults}

            patientId = ${ patient.patientId };

    jq(document).ready(function() {
        jq("#btnBack").click(function(){
            ui.navigate('${ ui.pageLink("kenyaemr", "clinician/clinicianViewPatient", [patient: patient, patientId: patient]) }');
        });
    });


</script>

${ui.includeFragment("appui", "messages", [codes: [
        "kenyaemrorderentry.pastAction.REVISE",
        "kenyaemrorderentry.pastAction.DISCONTINUE"
]])}

<div class="ke-page-content">
    <div >
        <div class="ui-tabs">

            <div class="ui-tabs-panel ui-widget-content">

                <div>
                    <button type="button" class="fa fa-arrow-left " style="float: left" id="btnBack">
                        Back to client home
                    </button>
                    <label id="orderHeader"> <h3>Lab Orders</h3></label>
                </div>
                <div id="lab-orders-app" data-ng-controller="LabOrdersCtrl" ng-init='init()'>
                

                <div id="program-tabs" class="ke-tabs" style="padding-top: 10px">
                    <div class="ke-tabmenu">
                        <div class="ke-tabmenu-item" data-tabid="active_orders">Active Order(s)</div>

                        <div class="ke-tabmenu-item" data-tabid="new_orders">Create New Order(s)</div>

                        <div class="ke-tabmenu-item" data-tabid="lab_results">Enter Lab Result(s)</div>
                        <div class="ke-tabmenu-item" data-tabid="past_orders">Previous Lab Order(s)</div>

                    </div>

                    <div class="ke-tab" data-tabid="new_orders" style="padding-top:10px" >

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
                                                <div class="card border-dark" >
                                                    <div class="list-group list-group-decorator" >
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
                                                                        <div class="btn-group button-size " role="group" aria-label="Basic example">
                                                                            <button type="button" class="text-left" style="width: 76%">{{order.name}}</button>
                                                                            <button type="button" class="fa fa-calendar fa-1x" style="width: 8%"
                                                                                    data-toggle="modal" data-target="#dateOrder"
                                                                                    ng-click="orderSelectedToAddDateActivated(order)"></button>
                                                                            <button type="button" class="fa fa-warning fa-1x" style="width: 8%"
                                                                                    data-placement="top" title="Urgency | Reason"
                                                                                    data-toggle="modal" data-target="#orderUrgency"
                                                                                    ng-click="orderSelectedToAddDateActivated(order)"
                                                                            ></button>
                                                                            <button type="button" class="fa fa-remove fa-1x"
                                                                                    ng-click="deselectedOrder(order)" style="color:#9D0101;cursor: pointer; width: 8%"></button>
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
                                        <button type="button"
                                                data-toggle="modal" data-target="#confirmation-dailog"
                                            ng-click="generateLabOrdersSummaryView()"
                                                ng-disabled="selectedOrders.length === 0"><img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" />
                                            Confirm</button>
                                    </div>

                                </form>
                            </div>



                        </div>



                    </div>

                    <div class="ke-tab" data-tabid="lab_results">

                        <div>
                            <div class="row">
                                <div class="col-lg-12">
                                    <form class="form-horizontal">
                                        <div style="padding-top: 10px">
                                            <div class="card">
                                                <div class="card-header">
                                                    <h4 class="card-title">
                                                        Enter Lab Result(s)
                                                    </h4>
                                                </div>

                                                <div class="card-body">
                                                    <span ng-show="InspireList.length ===0">No Lab orders to enter results for</span>
                                                    <div class="row" ng-repeat="items in InspireList">
                                                        <div class="col" ng-repeat="control in items" >

                                                            <div ng-if="control.rendering === 'select'">
                                                                <div class="form-group row">
                                                                    <label class="label label-md "><b>{{control.label}}:</b>
                                                                        <p>  <span >({{control.dateActivated | date:'dd-MM-yyyy'}})</span>
                                                                        </p></label>

                                                                    <div >
                                                                        <select class="form-control set-width"
                                                                                ng-model="typeValues[control.orderId]">
                                                                            <option ng-repeat=" o in control.answers"
                                                                                    ng-value="o.concept">{{o.label}}
                                                                            </option>
                                                                        </select>
                                                                    </div>
                                                                    <span class="btn-xl fa fa-calendar fa-1x" style="cursor: pointer;"
                                                                            data-toggle="modal" data-target="#dateOrder"
                                                                            ng-click="orderSelectedToAddResultsDate(control)"></span>
                                                                </div>
                                                            </div>

                                                            <div ng-if="control.rendering === 'inputtext'" class="label">
                                                                <div class="form-group row">
                                                                    <label class="label label-md" ><b>{{control.label}}:</b>
                                                                        <p>  <span >({{control.dateActivated | date:'dd-MM-yyyy'}})</span>
                                                                        </p></label>

                                                                    <div >
                                                                        <input class="form-control set-width" type="text"
                                                                               ng-model="typeValues[control.orderId]">
                                                                    </div>
                                                                    <span class="btn-xl fa fa-calendar fa-1x" style="cursor: pointer;"
                                                                            data-toggle="modal" data-target="#dateOrder"
                                                                            ng-click="orderSelectedToAddResultsDate(control)"></span>

                                                                </div>
                                                            </div>

                                                            <div ng-if="control.rendering === 'inputnumeric'" class="label">
                                                                <div class="form-group row">
                                                                    <label class="label label-md" ><b>{{control.label}}:</b>
                                                                        <p>  <span >({{control.dateActivated | date:'dd-MM-yyyy'}})</span>
                                                                        </p></label>

                                                                    <div>
                                                                        <input class="form-control set-width" type="number"
                                                                               ng-model="typeValues[control.orderId]">
                                                                    </div>
                                                                    <span class="btn-xl fa fa-calendar fa-1x" style="cursor: pointer;"
                                                                            data-toggle="modal" data-target="#dateOrder"
                                                                            ng-click="orderSelectedToAddResultsDate(control)"></span>
                                                                </div>
                                                            </div>

                                                            <div ng-if="control.rendering === 'textarea'">
                                                                <div class="form-group row">
                                                                    <label class="label label-md"><b>{{control.label}}:</b>
                                                                        <p>  <span >({{control.dateActivated | date:'dd-MM-yyyy'}})</span>
                                                                        </p></label>

                                                                    <div >
                                                                        <textarea class="form-control" ng-model="typeValues[control.orderId]">
                                                                        </textarea>
                                                                    </div>
                                                                    <span class=" btn-xl fa fa-calendar fa-1x" style="cursor: pointer;"
                                                                            data-toggle="modal" data-target="#dateOrder"
                                                                            ng-click="orderSelectedToAddResultsDate(control)"></span>

                                                                </div>
                                                            </div>


                                                            <div ng-if="control.hvVl">
                                                                <div class="form-group row">
                                                                    <label class="label label-md "><b>HIV viral load:</b>
                                                                        <p>  <span >({{control.hvVl[0].dateActivated | date:'dd-MM-yyyy'}})</span>
                                                                        </p></label>

                                                                    <div ng-repeat="vl in control.hvVl">
                                                                        <input class="form-control set-width-vl" type="number" id="vload" ng-if="vl.rendering === 'inputnumeric'"
                                                                               ng-model="hivViralValues[vl.orderId]"
                                                                               ng-disabled="ischecked ==='yes'">


                                                                        <input class="form-check-input " ng-if="vl.rendering ==='checkbox'"
                                                                               type="checkbox" id="vl"
                                                                               name="feature"
                                                                               ng-checked="fag"
                                                                               ng-model="hivViralValuesLDL[vl.orderId]"
                                                                               ng-click="toggleSelection(vl.orderId)"
                                                                               value="'1302AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'">
                                                                        <label class="form-check-label" ng-if="vl.rendering ==='checkbox'">LDL</label>
                                                                        <span class="float-left btn-xl fa fa-calendar fa-1x " style="cursor: pointer;" ng-if="vl.rendering ==='checkbox'"
                                                                              data-toggle="modal" data-target="#dateOrderVl"
                                                                              ng-click="orderSelectedToAddResultsDateForVl(vl)"></span>

                                                                    </div>


                                                                </div>
                                                            </div>


                                                        </div>

                                                    </div>

                                                </div>

                                                <div style="padding-left: 50%; padding-bottom: 20px" ng-show="InspireList.length >0"
                                                >
                                                    <button type="button" ng-click="postLabOrderResults()" data-toggle="modal">
                                                        <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" />  Save</button>
                                                </div>
                                            </div>
                                        </div>

                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>


                    <div class="ke-tab" data-tabid="active_orders" style="padding-top: 10px">

                        <form>
                            <div class="card">
                                <div class="card-header">
                                    <h4 class="card-title">
                                        Active Lab Order(s)
                                    </h4>
                                </div>

                                <div class="card-body">
                                    <span ng-show="activeTestOrders.length==0">No active lab orders</span>
                                    <div class="table-responsive" ng-show="activeTestOrders.length > 0">
                                        <table ng-hide="activeTestOrders.loading" class="table table-striped">
                                            <tr>
                                                <th>Order Date</th>
                                                <th>Order No</th>
                                                <th>Test Name</th>
                                                <th>Order Reason</th>
                                                <th>Ordered By</th>
                                                <th>Actions</th>
                                            </tr>
                                            <tr ng-repeat="test in activeTestOrders">
                                                <td>
                                                    {{ test.dateActivated | date:'dd-MM-yyyy' }}
                                                </td>
                                                <td>
                                                    {{ test.orderNumber }}
                                                </td>
                                                <td>
                                                    {{test.display}}

                                                </td>
                                                <td>
                                                    <span ng-if="test.orderReasonCoded || test.orderReasonNonCoded">
                                                        {{test.orderReasonCoded}}  {{orderReasonNonCoded}}
                                                    </span>
                                                </td>
                                                <td>
                                                    {{test.orderer.display}}

                                                </td>
                                                <td>
                                                    <button type="button" class="btn btn-warning" data-toggle="modal"
                                                            data-target="#voidOrdersModal" ng-click="getOrderUuid(test)">
                                                        Cancel
                                                    </button>
                                                </td>

                                            </tr>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </form>
                        <!-- Modal void for lab orders -->
                        <div class="modal fade" id="voidOrdersModal" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
                            <div class="modal-dialog modal-dialog-centered" role="document">
                                <div class="modal-content">
                                    <div class="modal-header modal-header-primary">
                                        <h5 class="modal-title" id="exampleModalCenterTitle">Cancel Order</h5>
                                        <button type="button" class="close" data-dismiss="modal2" ng-click="closeModal()">&times;

                                        </button>
                                    </div>
                                    <div class="modal-body">
                                        <label >Reason(s) for voiding orders</label>
                                        <div>
                                            <textarea class="form-control" ng-model="voidOrderReason">
                                            </textarea>
                                        </div>
                                    </div>
                                    <div class="modal-footer">
                                        <button type="button"  data-dismiss="modal2" ng-click="closeModal()">Close</button>
                                        <button type="button"  ng-disabled="voidOrders === ''" ng-click="voidAllHivViralLoadOrders()">
                                            <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="ke-tab" data-tabid="past_orders" style="padding-top: 10px">
                        <form>
                            <div class="card">
                                <div class="card-header">
                                    <h4 class="card-title">
                                        Previous Lab Order(s)
                                    </h4>
                                </div>

                                <div class="card-body">
                                    <span ng-show="pastLabOrders.length==0">No previous lab orders</span>
                                    <div class="table-responsive" ng-show="pastLabOrders.length > 0">
                                        <table ng-hide="activeTestOrders.loading" class="table table-striped">
                                            <tr>
                                                <th>Order Date</th>
                                                <th>Tests Ordered</th>
                                                <th>Result Date</th>
                                                <th>Results</th>
                                                <th>Order Reason</th>
                                                <th>Actions</th>
                                            </tr>
                                            <tr ng-repeat="past in pastLabOrders | limitTo:limit">
                                                <td>
                                                    {{ past.dateActivated | date:'dd-MM-yyyy'}}
                                                </td>
                                                <td>
                                                    {{past.name}}
                                                </td>
                                                <td>
                                                    {{ past.resultDate | date:'dd-MM-yyyy' }}
                                                </td>
                                                <td>
                                                    <span ng-if="past.valueNumeric !==null">
                                                        {{past.valueNumeric}}
                                                    </span>
                                                    <span ng-if="past.valueCoded !==null">
                                                        {{past.valueCoded}}
                                                    </span>
                                                    <span ng-if="past.valueText !==null">
                                                        {{past.valueText}}
                                                    </span>


                                                </td>
                                                <td>
                                                    <span ng-if="past.orderReasonCoded || past.orderReasonNonCoded">
                                                        {{past.orderReasonCoded}}  {{orderReasonNonCoded}}
                                                    </span>

                                                </td>
                                                <td>
                                                    <button type="button" class="fa fa-edit fa-1x"
                                                            data-toggle="modal" data-target="#editOrderResults"
                                                            ng-click="editOrderResultsDialog(past)" style="cursor: pointer">Edit</button>
                                                    <button type="button" class="fa fa-remove fa-1x" data-toggle="modal" data-target="#orderWarning"
                                                            ng-click="deleteOrderResultsDialog(past)" style="color:#9D0101;cursor: pointer; width: 8%"></button>
                                                </td>

                                            </tr>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div>

                    <!-- confirmation dialog -->
                    <div class="modal fade" id="confirmation-dailog" tabindex="-1" role="dialog" aria-labelledby="confirmationMessageModalCenterTitle" aria-hidden="true">
                        <div class="modal-dialog modal-lg" role="document">
                            <div class="modal-content">
                                <div class="modal-header modal-header-primary">
                                    <h5 class="modal-title" id="confirmationModalCenterTitle">Confirm Orders</h5>
                                    <button type="button" class="close" data-dismiss="modal" ng-click="closeConfirmationDialogModal()">&times;

                                    </button>
                                </div>

                                <div class="modal-body">
                                    <div class="table-responsive">
                                        <table class="table table-striped">
                                            <tr>
                                                <th>Order Date</th>
                                                <th>Test Name</th>
                                                <th>Order Reason</th>
                                                <th>Order Urgency</th>
                                                <th>Actions</th>
                                            </tr>
                                            <tr ng-repeat="testSummary in viewSummaryLabs">
                                                <td>
                                                    {{testSummary.dateActivated}}
                                                    <span>
                                                    <button type="button" class="fa fa-calendar fa-1x"
                                                            data-toggle="modal" data-target="#dateOrder"
                                                            ng-click="orderSelectedToAddDateActivated(testSummary)"></button>
                                                </span>
                                                </td>
                                                <td>
                                                    {{testSummary.name}}
                                                </td>
                                                <td>
                                                    {{testSummary.orderReasonCodedName}}
                                                    <span>
                                                    <button type="button" class="fa fa-warning fa-1x"
                                                            data-placement="top" title="Urgency | Reason"
                                                            data-toggle="modal" data-target="#orderUrgency"
                                                            ng-click="orderSelectedToAddDateActivated(testSummary)"></button>
                                                </span>

                                                </td>
                                                <td>
                                                    {{testSummary.urgency}}
                                                    <span>
                                                    <button type="button" class="fa fa-warning fa-1x"
                                                            data-placement="top" title="Urgency | Reason"
                                                            data-toggle="modal" data-target="#orderUrgency"
                                                            ng-click="orderSelectedToAddDateActivated(testSummary)"></button>
                                                </span>

                                                </td>
                                                <td>
                                                    <div  role="group" aria-label="Basic example">


                                                        <button type="button" class="fa fa-remove fa-1x"
                                                                ng-click="deselectedOrder(testSummary)" style="color:#9D0101;cursor: pointer"></button>
                                                    </div>
                                                </td>

                                            </tr>
                                        </table>
                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" class="mr-auto"   data-dismiss="modal" ng-click="closeConfirmationDialogModal()"
                                    >Cancel</button>

                                    <div>
                                        <button type="button" ng-click="postLabOrdersEncounters()"
                                                ng-disabled="selectedOrders.length === 0">Save</button>
                                    </div>


                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Modal date for lab orders -->
                    <div class="modal fade" id="dateOrder" tabindex="-1" role="dialog" aria-labelledby="dateModalCenterTitle" aria-hidden="true">
                        <div class="modal-dialog modal-dialog-centered" role="document">
                            <div class="modal-content">
                                <div class="modal-header modal-header-primary">
                                    <h5 class="modal-title" id="dateModalCenterTitle"></h5>
                                    <button type="button" class="close" data-dismiss="modal2" ng-click="closeDateOrderModal()">&times;

                                    </button>
                                </div>
                                <div class="modal-body">
                                    <label >{{titleDate}}</label>
                                    <div>
                                        Date: ${ ui.includeFragment("kenyaui", "field/java.util.Date", [ id: "orderDate", formFieldName: "orderDate"]) }
                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" data-dismiss="modal2"   ng-click="closeDateOrderModal()">Close</button>
                                    <button type="button" ng-click="setOrderDate()">
                                        <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Modal date for vl results -->
                    <div class="modal fade" id="dateOrderVl" tabindex="-1" role="dialog" aria-labelledby="dateModalCenterTitle" aria-hidden="true">
                        <div class="modal-dialog modal-dialog-centered" role="document">
                            <div class="modal-content">
                                <div class="modal-header modal-header-primary">
                                    <h5 class="modal-title" id="dateVlModalCenterTitle"></h5>
                                    <button type="button" class="close" data-dismiss="modal2" ng-click="closeDateOrderModal()">&times;

                                    </button>
                                </div>
                                <div class="modal-body">
                                    <label >{{titleDate}}</label>
                                    <div>
                                        Date: ${ ui.includeFragment("kenyaui", "field/java.util.Date", [ id: "dateOrderVl", formFieldName: "dateOrderVl"]) }
                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button" data-dismiss="modal2"   ng-click="closeDateOrderModal()">Close</button>
                                    <button type="button" ng-click="setResultDateForViralLoad()">
                                        <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!-- Modal urgency  and reason for lab orders -->
                    <div class="modal fade" id="orderUrgency" tabindex="-1" role="dialog" aria-labelledby="urgencyModalCenterTitle" aria-hidden="true">
                        <div class="modal-dialog modal-dialog-centered" role="document">
                            <div class="modal-content">
                                <div class="modal-header modal-header-primary">
                                    <h5 class="modal-title" id="urgencyModalCenterTitle"> Order Urgency | Reason(s)</h5>
                                    <button type="button" class="close" data-dismiss="modal2" ng-click="closeModal()">&times;
                                    </button>
                                </div>
                                <div class="modal-body" >
                                    <label ><b>Order Urgency</b></label>
                                    <div>
                                        <select id="ddlOrderUrgency" class="form-control">
                                            <option value="ROUTINE" selected="selected">ROUTINE</option>
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

                                <div class="modal-body" id="modal-text">
                                    {{showErrorToast}}
                                </div>
                                <div class="modal-footer">
                                    <button type="button"  data-dismiss="modal2" ng-click="closeModal()">Close</button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!--Warning Modal -->
                    <div class="modal fade" id="orderWarning" tabindex="-1" role="dialog" style="font-size:16px;">
                        <div class="modal-dialog modal-dialog-centered" role="document">
                            <div class="modal-content">
                                <div class="modal-header modal-header-primary">
                                    <h5 class="modal-title" id="warningModalLabel">Warning!</h5>
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>

                                <div class="modal-body" id="modal-text-waring">
                                    {{showErrorToast}}
                                </div>
                                <div class="modal-footer">
                                    <button type="button"  data-dismiss="modal2" ng-click="closeModal()">NO</button>
                                    <button type="button"  data-dismiss="modal2" ng-click="deleteLabOrderResults()">Yes</button>
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
                                        Active <b>{{testName}}</b>  Order Already exist. Please check the Active Orders Tab to cancel the order and proceed.
                                    </div>
                                </div>
                                <div class="modal-footer">
                                    <button type="button"  data-dismiss="modal2" ng-click="closeModal()">Close</button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <!--Edit Results Modal -->
                    <div class="modal fade" id="editOrderResults" tabindex="-1" role="dialog" style="font-size:16px;">
                        <div class="modal-dialog modal-dialog-centered" role="document">
                            <div class="modal-content">
                                <div class="modal-header modal-header-primary">
                                    <h5 class="modal-title" id="editResultModalLabel">Edit Lab Results</h5>
                                    <button type="button" aria-label="Close" ng-click="closeEditResultsDialogModal()">
                                        <span aria-hidden="true">&times;</span>
                                    </button>
                                </div>

                                <div class="modal-body" style="color:red;" id="modalEditResult">
                                    <label class="label label-md "><b>{{orderName}}:</b></label>
                                    <div ng-if="valueNumericResults">
                                        <input class="form-control" type="number" ng-model="obsValue" id="numericResults">
                                    </div>
                                    <div ng-if="valueTextResults">
                                        <input class="form-control" type="text" ng-model="obsValue" id="textResults">
                                    </div>
                                    <div ng-if="obsConcept ==='1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'">
                                        <input class="form-control" type="number" ng-model="obsVlValueToEdit" id="numeriVl">

                                    </div>
                                    <div ng-if="obsConcept ==='856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'">
                                        <input class="form-control set-width-vl" type="number" id="editVload"
                                               ng-model="editVload"
                                               ng-disabled="ischecked ==='yes'">

                                        <label class="form-check-label set-width-ldl">LDL</label>
                                        <input class="form-check-input set-width-ldl"
                                               type="checkbox" id="editLdl"
                                               name="feature"
                                               ng-checked="fag"
                                               ng-model="hivViralValuesLDL[vl.orderId]"
                                               ng-click="isEditToggleSelection()"
                                               value="'1302AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA'">


                                    </div>


                                    <div ng-if="valueCodedResults">
                                        <select class="form-control set-width" id="singleSelect"
                                                ng-model="data.singleSelect">
                                            <option ng-repeat=" o in answers"
                                                    ng-value="o.uuid">{{o.display}}
                                            </option>
                                        </select>
                                    </div>


                                </div>
                                <div class="modal-footer">
                                    <button type="button" ng-click="closeEditResultsDialogModal()">Close</button>
                                    <button type="button"  data-dismiss="modal2" ng-click="updateLabResults()">Save</button>
                                </div>
                            </div>
                        </div>
                    </div>


                </div>
                </div>

            </div>

        </div>


    </div>


</div>

</div>
<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
    angular.bootstrap('#lab-orders-app', ['labOrders']);

</script>
