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

</script>

<div id="active-lab-orders" data-ng-controller="LabOrdersCtrl" ng-init='init()'>

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
                    <button type="button" class="close" data-dismiss="modal2" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <label >Reason(s) for voiding orders</label>
                    <div>
                        <textarea class="form-control" ng-model="voidOrders">
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

    <!-- spinner modal -->

</div>
<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
    angular.bootstrap('#active-lab-orders', ['labOrders']);

</script>