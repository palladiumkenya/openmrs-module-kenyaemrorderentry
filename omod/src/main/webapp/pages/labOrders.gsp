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
    patientId = ${ patient.patientId };

    jq(document).ready(function() {
        jq("#btnBack").click(function(){
            ui.navigate('${ ui.pageLink("kenyaemr", "clinician/clinicianViewPatient", [patient: patient, patientId: patient]) }');
           //window.location="http://localhost:8080/openmrs/kenyaemr/clinician/clinicianViewPatient.page?patientId=" +patientId +'&'
        });
    });


</script>

${ui.includeFragment("appui", "messages", [codes: [
        "kenyaemrorderentry.pastAction.REVISE",
        "kenyaemrorderentry.pastAction.DISCONTINUE"
]])}

<div class="ke-page-content">
    <div id="lab-orders-app">
        <div class="ui-tabs">

            <div class="ui-tabs-panel ui-widget-content">

                <div>
                    <button type="button" class="fa fa-arrow-left " style="float: left" id="btnBack">
                        Back to client home
                    </button>
                    <label id="orderHeader"> <h3>Lab Orders</h3></label>
                </div>
                

                <div id="program-tabs" class="ke-tabs" style="padding-top: 10px">
                    <div class="ke-tabmenu">
                        <div class="ke-tabmenu-item" data-tabid="active_orders">Active Order(s)</div>

                        <div class="ke-tabmenu-item" data-tabid="new_orders">Create New Order(s)</div>

                        <div class="ke-tabmenu-item" data-tabid="lab_results">Enter Lab Result(s)</div>
                        <div class="ke-tabmenu-item" data-tabid="past_orders">Previous Lab Order(s)</div>

                    </div>

                    <div class="ke-tab" data-tabid="new_orders" style="padding-top:10px">

                        ${ui.includeFragment("kenyaemrorderentry", "patientdashboard/createLabsOrders", ["patient": patient])}
                    </div>

                    <div class="ke-tab" data-tabid="lab_results">

                        ${ui.includeFragment("kenyaemrorderentry", "patientdashboard/labOrdersResults", ["patient": patient])}

                    </div>


                    <div class="ke-tab" data-tabid="active_orders" style="padding-top: 10px">
                        ${ui.includeFragment("kenyaemrorderentry", "patientdashboard/activeTestOrders", ["patient": patient])}
                    </div>
                    <div class="ke-tab" data-tabid="past_orders" style="padding-top: 10px">
                        ${ui.includeFragment("kenyaemrorderentry", "patientdashboard/pastLabOrders", ["patient": patient])}

                    </div>

                </div>

            </div>

        </div>


    </div>


</div>

</div>
<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
  //  angular.bootstrap('#lab-orders-app', ['labOrders']);

</script>
