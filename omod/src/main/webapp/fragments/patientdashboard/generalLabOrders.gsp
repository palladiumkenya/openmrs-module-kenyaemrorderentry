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
    window.sessionContext = {'locale': 'en_GB'}

</script>

<div id="general-lab-orders" data-ng-controller="LabOrdersCtrl" ng-init='init()'>



<!-- general message modal -->

<div class="modal fade" id="generalModalLabs" tabindex="-1" role="dialog" aria-labelledby="generalMessageModalCenterTitle" aria-hidden="true">
    <div class="modal-dialog modal-lg "  role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Lab Orders</h3>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>

            <div class="modal-body">
                <div id="program-tabs" class="ke-tabs">
                    <div class="ke-tabmenu">
                        <div class="ke-tabmenu-item" data-tabid="active_orders">Active Order(s)</div>

                        <div class="ke-tabmenu-item" data-tabid="new_orders">Create New Order(s)</div>

                        <div class="ke-tabmenu-item" data-tabid="lab_results">Enter Lab Result(s)</div>
                        <div class="ke-tabmenu-item" data-tabid="past_orders">Previous Lab Order(s)</div>

                    </div>
                    <div class="ke-tab" data-tabid="new_orders" style="padding-top:10px">
                        ${ui.includeFragment("kenyaemrorderentry", "patientdashboard/createLabsOrders", ["patient": patient])}


                    </div>
                    <div class="ke-tab" data-tabid="active_orders" style="padding-top: 10px">
                        ${ui.includeFragment("kenyaemrorderentry", "patientdashboard/activeTestOrders", ["patient": patient])}
                    </div>
                    <div class="ke-tab" data-tabid="lab_results" style="padding-top: 10px">
                        ${ui.includeFragment("kenyaemrorderentry", "patientdashboard/labOrdersResults", ["patient": patient])}
                    </div>

                    <div class="ke-tab" data-tabid="past_orders" style="padding-top: 10px">
                        ${ui.includeFragment("kenyaemrorderentry", "patientdashboard/pastLabOrders", ["patient": patient])}

                    </div>
                </div>


            </div>
            <div class="modal-footer">
                <button type="button"  data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

</div>
<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
   // angular.bootstrap('#general-lab-orders', ['labOrders']);

</script>