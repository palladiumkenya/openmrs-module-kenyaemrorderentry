<%
    ui.decorateWith("kenyaemr", "standardPage", [ patient: currentPatient])
    ui.includeJavascript("uicommons", "emr.js")
    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.11.2.js")
    ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
    ui.includeJavascript("orderentryui", "bootstrap.min.js")

    ui.includeJavascript("orderentryui", "angular-material.js")
    ui.includeJavascript("orderentryui", "angular-material.min.js")

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
    ui.includeJavascript("orderentryui", "order-model.js")
    ui.includeJavascript("orderentryui", "order-entry.js")
    ui.includeJavascript("orderentryui", "labOrders.js")

    ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
    ui.includeCss("orderentryui", "drugOrders.css")
    ui.includeCss("uicommons", "styleguide/jquery-ui-1.9.2.custom.min.css")
    ui.includeCss("orderentryui", "index.css")

    ui.includeCss("orderentryui", "angular-material.css")
    ui.includeCss("orderentryui", "angular-material.min.css")
    ui.includeCss("orderentryui", "bootstrap.min.css")
    ui.includeCss("orderentryui", "labOrders.css")
%>
<style type="text/css">
#new-order input {
    margin:5px;
}
th,td{
    text-align:left;
}
</style>
<script type="text/javascript">

    window.OpenMRS = window.OpenMRS || {};
    window.OpenMRS.drugOrdersConfig = ${ jsonConfig };
    window.sessionContext = {'locale':'en_GB'}
        window.OpenMRS.labTestJsonPayload=${labTestJsonPayload}
            window.OpenMRS.panelList=${panelList}

</script>

${ ui.includeFragment("appui", "messages", [ codes: [
        "orderentryui.pastAction.REVISE",
        "orderentryui.pastAction.DISCONTINUE"
] ])}

<div class="ke-page-content">
    <div id="lab-orders-app" ng-controller="LabOrdersCtrl" ng-init='init()'>
        <div class="ui-tabs">
            <ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header">
                <li ng-repeat="setting in careSettings" class="ui-state-default ui-corner-top"
                    ng-class="{ 'ui-tabs-active': setting == careSetting, 'ui-state-active': setting == careSetting }">
                    <a class="ui-tabs-anchor" ng-click="setCareSetting(setting)">
                        {{ setting | omrsDisplay }}
                    </a>
                </li>
            </ul>



            <div class="ui-tabs-panel ui-widget-content">
                <h3>Lab Orders</h3>
                <div id="program-tabs" class="ke-tabs">
        <div class="ke-tabmenu">
            <div class="ke-tabmenu-item" data-tabid="active_orders">Active Orders</div>

            <div class="ke-tabmenu-item" data-tabid="new_orders">Create New Order</div>

            <div class="ke-tabmenu-item" data-tabid="lab_results">Lab Results</div>


        </div>
                    <div class="ke-tab" data-tabid="new_orders" style="padding-top:45px">
                        <div class="card">
                            <div class = "card-header">
                                <h4 class = "card-title">
                                    Create New Orders
                                </h4>
                            </div>
                            <div class="card-body">
                <form>
                    <table class="table col-lg-12">
                        <tbody>
                        <tr>
                            <td class="col-lg-3">
                                <div class="list-group">
                                    <div class="list-group-item" ng-repeat="lab in labOrders" ng-click="loadLabPanels(lab)">
                                        <div class="link-item">
                                            <a class="formLink">
                                                {{lab.name}}
                                            </a>
                                        </div>


                                    </div>
                                </div>

                                <div style="padding-top:10px">
                                    <div class="card">
                                        <div class = "card-header">
                                            <h5 class = "card-title">
                                                Selected Order
                                            </h5>
                                        </div>
                                        <div class="card-body">
                                            <div class="list-group">
                                                <div class="list-group-item" ng-repeat="order in filteredOrders" >
                                                    <div class="link-item">
                                                        <button type="button" ng-click="deselectedOrder(order)">
                                                            {{order.name}}
                                                        </button>
                                                        <a><span class="glyphicon glyphicon-remove link" style="color:red;
                                                        padding-left: 1em; cursor: pointer" ></span></a>
                                                    </div>


                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </td>
                            <td class="col-lg-12">
                                <div class="col-lg-12">
                                    <fieldset class="col-lg-12 scheduler-border">
                                        <legend class="col-lg-12 scheduler-border">Panels</legend>
                                        <div class="row">
                                            <div class="col-lg-12">
                                                <ul>
                                                    <li ng-repeat="panel in labPanels"  ng-click="loadLabPanelTests(panel)">
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
                                        <legend class="col-lg-12 scheduler-border"> Tests</legend>
                                        <div class="row">
                                            <div class="col-lg-12">
                                                <div ng-repeat="test in panelTests" ng-click="getSelectedTests(test)">
                                                    <div class="column">
                                                        <input type="checkbox" id="scales" name="feature" ng-model='test.selected'
                                                               value="test.concept_id">
                                                        <label>{{test.name}}</label>
                                                    </div>


                                                </div>
                                            </div>
                                        </div>
                                    </fieldset>
                                </div>

                            </td>

                        </tr>

                        </tbody>
                    </table>
                    <div style="padding-left: 50%">
                        <button type="button"  ng-click="postLabOrdersEncounters()">
                            Post lab orders</button>
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
                                    <div  style="padding-top: 45px">
                                        <div class = "card">
                                            <div class = "card-header">
                                                <h4 class = "card-title">
                                                    Lab Results
                                                </h4>
                                            </div>

                                            <div class = "card-body">
                                                <div ng-repeat="control in panelListResults">
                                                    <div class="column">

                                                        <div ng-if="control.rendering === 'select'" >
                                                            <div class="form-group row">
                                                                <label class="col-lg-2">{{control.label}}:</label>
                                                                <div class="col-lg-4">
                                                                    <select class="form-control" ng-model="typeValues[control.orderId]" >
                                                                        <option ng-repeat=" o in control.answers"
                                                                                ng-value="o.concept">{{o.label}}
                                                                        </option>
                                                                    </select>
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div ng-if="control.rendering === 'inputtext'">
                                                            <div class="form-group row">
                                                                <label class="col-lg-2">{{control.label}}:</label>
                                                                <div class="col-lg-4">
                                                                    <input class="form-control" type="text" ng-model="typeValues[control.orderId]" >
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div ng-if="control.rendering === 'inputnumeric'">
                                                            <div class="form-group row">
                                                                <label class="col-lg-2">{{control.label}}:</label>
                                                                <div class="col-lg-4">
                                                                    <input class="form-control" type="number" ng-model="typeValues[control.orderId]">
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div ng-if="control.rendering === 'textarea'">
                                                            <div class="form-group row">
                                                                <label class="col-lg-2">{{control.label}}:</label>
                                                                <div class="col-lg-4">
                                                                    <textarea  class="form-control">
                                                                    </textarea>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    </div>



                                                </div>
                                                <div style="padding-left: 50%; padding-top: 10px">
                                                    <button type="button"  ng-click="postLabOrderResults()">
                                                        Save</button>
                                                </div>

                                            </div>
                                        </div>
                                    </div>

                                </form>
                            </div>
                        </div>
                    </div>

                </div>


                <div  class="ke-tab" data-tabid="active_orders">
                    <form>
                    <div class = "card" style="padding-top: 45px">
                        <div class = "card-header">
                            <h4 class = "card-title">
                                Active Lab Orders
                            </h4>
                        </div>

                        <div class = "card-body">
                    <div class="table-responsive" >
                        <table ng-hide="activeTestOrders.loading" class="table table-striped">
                            <tr>
                                <th>Order Date</th>
                                <th>Order No</th>
                                <th>Test Name</th>
                                <th>Ordered By</th>
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
                                    {{test.orderer.display}}

                                </td>
                            </tr>
                        </table>
                    </div>
                            </div>
                    </div>
                    </form>
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
