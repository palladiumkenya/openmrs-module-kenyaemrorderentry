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
    window.OpenMRS.pastLabOrdersResults =${pastLabOrdersResults}


</script>

<div id="past-orders-results" data-ng-controller="LabOrdersCtrl" ng-init='init()'>

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
                                <span ng-if="past.valueNumeric">
                                    {{past.valueNumeric}}
                                </span>
                                <span ng-if="past.valueCoded">
                                    {{past.valueCoded}}
                                </span>
                                <span ng-if="past.valueText">
                                    {{past.valueText}}
                                </span>


                            </td>
                            <td>
                                <span ng-if="past.orderReasonCoded || past.orderReasonNonCoded">
                                    {{past.orderReasonCoded}} ,{{orderReasonNonCoded}}
                                </span>

                            </td>
                            <td>
                                <button type="button" class="fa fa-edit fa-1x"
                                        data-toggle="modal" data-target="#editOrderResults"
                                        ng-click="editOrderResultsDialog(past)" style="cursor: pointer">Edit</button>
                            </td>

                        </tr>
                    </table>
                </div>
            </div>
        </div>
    </form>

    <!--Edit Results Modal -->
    <div class="modal fade" id="editOrderResults" tabindex="-1" role="dialog" style="font-size:16px;">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header modal-header-primary">
                    <h5 class="modal-title" id="exampleModalLabel">Edit Lab Results</h5>
                    <button type="button" aria-label="Close" ng-click="closeEditResultsDialogModal()">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>

                <div class="modal-body" style="color:red;" id="modal-text">
                    <label class="label label-md "><b>{{orderName}}:</b></label>
                    <div ng-if="valueNumericResults">
                        <input class="form-control" type="number" ng-model="obsValue" id="numericResults">
                    </div>
                    <div ng-if="valueTextResults">
                        <input class="form-control" type="text" ng-model="obsValue" id="textResults">
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
<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
  //  angular.bootstrap('#past-orders-results', ['labOrders']);

</script>