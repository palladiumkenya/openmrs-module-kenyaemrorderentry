<%
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

<style type="text/css">

</style>
<div>
<div style="float: left">
    ${ui.includeFragment("kenyaemrorderentry", "patientdashboard/generalLabOrders", ["patient": patient])}
    <div >
    <ul>
    <li class="dropdown">
        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
            <i class="fa fa-th"></i>
            <span class="hidden-xs hidden-sm">More</span>
        </a>
        <ul class="dropdown-content linguas" style="background-color:#7f7b72">
            <li style="cursor: pointer;background-color:#7f7b72" >
                <a data-toggle="modal" data-target="#generalModalLabs">
                    <i class="fa fa-medkit"></i>
                    <span>Lab Orders</span>
                </a>
            </li>
            <li style="cursor: pointer;background-color:#7f7b72">
                <a data-toggle="modal" data-target="#generalModalLabk">
                    <i class="fa fa-medkit"></i>
                    <span>Drug Orders</span>
                </a>
            </li>
        </ul>
    </li>
</ul>
</div>
</div>

</div>
</div>