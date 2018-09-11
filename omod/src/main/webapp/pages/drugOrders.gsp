<%
    ui.decorateWith("kenyaemr", "standardPage", [ patient: currentPatient, layout: "sidebar" ])
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
    ui.includeJavascript("orderentryui", "drugOrders.js")

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
    window.OpenMRS.orderSet=${orderSetJson}
    window.OpenMRS.labTestJsonPayload=${labTestJsonPayload}
</script>

${ ui.includeFragment("appui", "messages", [ codes: [
        "orderentryui.pastAction.REVISE",
        "orderentryui.pastAction.DISCONTINUE"
] ])}
<div class="ke-page-sidebar">
    <div class="ke-panel-frame">
        ${ ui.includeFragment("kenyaui", "widget/panelMenuItem", [ iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back", href: "" ]) }
    </div>
</div>
<div class="ke-page-content">
<div id="drug-orders-app" ng-controller="DrugOrdersCtrl" ng-init='init()'>
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
            <form>
            <h3>Lab Orders</h3>
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
                            <h5>Selected Order</h5>
                             <div class="panel">
                                 <div class="panel-body">
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
            <div style="padding-top: 10px">
            <div class="table-responsive" >
                <table ng-hide="activeDrugOrders.loading" class="table table-striped">
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


            <h3>Drug Order Types</h3>

            <span class="ke-field-content">
                <div>
                    <input type="radio" ng-model="which"  value="single" /> Single Drug

                    <input type="radio" ng-model="which"  value="regimen" /> Regimen
                </div>


             </span>
            <div ng-show="which === 'single'">
            <form id="new-order" class="sized-inputs css-form" name="newOrderForm" novalidate>
                <p>
                    <span ng-show="newDraftDrugOrder.action === 'NEW'">
                        <label>New order for:</label>
                        <select-drug ng-model="newDraftDrugOrder.drug" placeholder="Drug" size="40"></select-drug>
                    </span>
                    <strong ng-show="newDraftDrugOrder.action === 'REVISE'">
                        Revised order for: {{ newDraftDrugOrder.drug.display }}
                    </strong>
                </p>

                <p ng-show="newDraftDrugOrder.drug">
                    <label class="heading instructions">
                        <span>Instructions</span>
                        <a ng-repeat="dosingType in dosingTypes" tabindex="-1"
                           ng-click="newDraftDrugOrder.dosingType = dosingType.javaClass"
                           ng-class="{ active: newDraftDrugOrder.dosingType == dosingType.javaClass }">
                            <i class="{{ dosingType.icon }}"></i>
                            {{ dosingType.display }}
                        </a>
                    </label>

                    <span ng-if="newDraftDrugOrder.dosingType == 'org.openmrs.SimpleDosingInstructions'">
                        <input ng-model="newDraftDrugOrder.dose" type="number" placeholder="Dose" min="0" required/>
                        <select-concept-from-list ng-model="newDraftDrugOrder.doseUnits" concepts="doseUnits" placeholder="Units" size="5" required></select-concept-from-list>

                        <select-order-frequency ng-model="newDraftDrugOrder.frequency" frequencies="frequencies" placeholder="Frequency" required></select-order-frequency>

                        <select-concept-from-list ng-model="newDraftDrugOrder.route" concepts="routes" placeholder="Route" size="20" required></select-concept-from-list>
                        <br/>

                        <label ng-class="{ disabled: !newDraftDrugOrder.asNeededCondition }">As needed for</label>
                        <input ng-model="newDraftDrugOrder.asNeededCondition" type="text" size="30" placeholder="reason (optional)"/>
                        <br/>

                        <label ng-class="{ disabled: !newDraftDrugOrder.duration }">For</label>
                        <input ng-model="newDraftDrugOrder.duration" type="number" min="0" placeholder="Duration" size="20"/>
                        <select-concept-from-list ng-model="newDraftDrugOrder.durationUnits" concepts="durationUnits" placeholder="Units" size="8" required-if="newDraftDrugOrder.duration"></select-concept-from-list>
                        <label ng-class="{ disabled: !newDraftDrugOrder.duration }">total</label>
                        <br/>
                        <textarea ng-model="newDraftDrugOrder.dosingInstructions" rows="2" cols="60" placeholder="Additional instruction not covered above"></textarea>
                    </span>

                    <span ng-if="newDraftDrugOrder.dosingType == 'org.openmrs.FreeTextDosingInstructions'">
                        <textarea ng-model="newDraftDrugOrder.dosingInstructions" rows="4" cols="60" placeholder="Complete instructions"></textarea>
                    </span>
                </p>

                <p ng-if="newDraftDrugOrder.drug && careSetting.careSettingType == 'OUTPATIENT'">
                    <label class="heading">For outpatient orders</label>
                    Dispense:
                    <input ng-model="newDraftDrugOrder.quantity" type="number" min="0" placeholder="Quantity" required/>
                    <select-concept-from-list ng-model="newDraftDrugOrder.quantityUnits" concepts="quantityUnits" placeholder="Units" size="8"></select-concept-from-list>
                </p>

                <p ng-show="newDraftDrugOrder.drug">
                    <button type="submit" class="confirm" ng-disabled="newOrderForm.\$invalid" ng-click="addNewDraftOrder()">Add</button>
                    <button class="cancel" ng-click="cancelNewDraftOrder()">Cancel</button>
                </p>
            </form>
</div>

            <div id="draft-orders" ng-show="draftDrugOrders.length > 0">
                <h3>Unsaved Draft Orders ({{ draftDrugOrders.length }})</h3>
                <table class="ke-table-vertical">
                    <tr class="draft-order" ng-repeat="order in draftDrugOrders">
                        <td>
                            {{ order.action }}
                            {{ order | dates }}
                        </td>
                        <td>
                            {{ order | instructions }}
                            <span ng-show="order.action == 'DISCONTINUE'">
                                <br/>
                                For: <input ng-model="order.orderReasonNonCoded" class="dc-reason" type="text" placeholder="reason" size="40"/>
                            </span>
                        </td>
                        <td class="actions">
                            <a ng-click="editDraftDrugOrder(order)"><i class="icon-pencil edit-action"></i></a>
                            <a ng-click="cancelDraftDrugOrder(order)"><i class="icon-remove delete-action"></i></a>
                        </td>
                    </tr>
                </table>

                <div class="actions">
                    <div class="signature">
                        Signing as ${ ui.format(sessionContext.currentProvider) } on (auto-generated timestamp)
                        <img ng-show="loading" src="${ ui.resourceLink("uicommons", "images/spinner.gif") }"/>
                    </div>
                    <button class="confirm right" ng-disabled="loading" ng-click="signAndSaveDraftDrugOrders()">Sign and Save</button>
                    <button class="cancel" ng-click="cancelAllDraftDrugOrders()">
                        {{ draftDrugOrders.length > 1 ? "Discard All" : "Discard" }}
                    </button>
                </div>
            </div>

        <div ng-show="which === 'regimen'">
            <h3> Order Regimen</h3>
            <div>
            ${ ui.includeFragment("orderentryui", "patientdashboard/regimenDispensation", ["patient": patient]) }


            </div>
        </div>

            
        <div style="padding-top: 20px">
            <div class="info-section">
                <div class="info-header">
                    <i class="icon-medicine"></i>
            <h3>Active Drug Orders</h3>
                </div>
            </div>
            <span ng-show="activeDrugOrders.loading">${ ui.message("uicommons.loading.placeholder") }</span>
            <span ng-hide="activeDrugOrders.loading || activeDrugOrders.length > 0">None</span>
            <table ng-hide="activeDrugOrders.loading" class="ke-table-vertical">
                <tr>
                 <th width="30%">Dates</th>
                 <th width="50%">Instructions</th>
                 <th width="20%">Action</th>
                </tr>
                <tr ng-repeat="order in activeDrugOrders">
                    <td ng-class="{ 'will-replace': replacementFor(order) }">
                        {{ order | dates }}
                    </td>
                    <td ng-class="{ 'will-replace': replacementFor(order) }">
                        {{ order | instructions }}
                    </td>
                    <td>
                        <span style="background: white">
                        <span ng-show="!replacementFor(order)" ng-click="reviseOrder(order)">
                            <span >
                            <button style=" background: white !important; border: 0px">
                                <img src="${ ui.resourceLink("kenyaui", "images/glyphs/edit.png") }" /> Edit</button>
                            </span>
                        </span>
                        <span ng-show="!replacementFor(order)" ng-click="discontinueOrder(order)">
                            <button  style=" background: white !important; border: 0px">
                                <img src="${ ui.resourceLink("kenyaui", "images/glyphs/cancel.png") }" /> Cancel</button>
                        </span>
                        </span>

                        <span ng-show="replacementFor(order)">
                            will {{ replacementFor(order).action }}
                        </span>
                    </td>
                </tr>
            </table>
        </div>

            <div class="info-section">
                <div class="info-header">
                    <i class="icon-medicine"></i>

            <h3>Past Drug Orders</h3>
                </div>
            </div>
            <span ng-show="pastDrugOrders.loading">${ ui.message("uicommons.loading.placeholder") }</span>
            <span ng-hide="pastDrugOrders.loading || pastDrugOrders.length > 0">None</span>
            <table id="past-drug-orders" ng-hide="pastDrugOrders.loading" class="ke-table-vertical">
            <tr>
             <th width="10%">Replacement</th>
             <th width="20%">Dates</th>
             <th>Instructions</th>
            </tr>
                <tr ng-repeat="order in pastDrugOrders">
                    <td>
                        {{ replacementForPastOrder(order) | replacement }}
                    </td>
                    <td>
                        {{ order | dates }}
                    </td>
                    <td>
                        {{ order | instructions }}
                    </td>
                </tr>
            </table>

        </div>

    </div>


</div>


        ${ ui.includeFragment("orderentryui", "patientdashboard/labOrders", ["patient": patient]) }


    </div>
</div>
<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
    angular.bootstrap('#drug-orders-app', ['drugOrders']);

</script>
