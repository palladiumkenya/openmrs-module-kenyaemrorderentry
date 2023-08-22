<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])
    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to home", href: ui.pageLink("kenyaemr", "userHome")]
    ]

    def manifestCategories = [
            [label: "Summary", iconProvider: "kenyaui", icon: "", label: "Summary", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersManifestHome")],
            [label: "Draft", iconProvider: "kenyaui", icon: "", label: "Draft", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersDraftManifestHome")],
            [label: "Ready to send", iconProvider: "kenyaui", icon: "", label: "Ready to send", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersReadyToSendManifestHome")],
            [label: "On hold", iconProvider: "kenyaui", icon: "", label: "On hold", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersOnHoldManifestHome")],
            [label: "Sending", iconProvider: "kenyaui", icon: "", label: "Sending", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersSendingManifestHome")],
            [label: "Submitted", iconProvider: "kenyaui", icon: "", label: "Submitted", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersSubmittedManifestHome")],
            [label: "Incomplete With Errors", iconProvider: "kenyaui", icon: "", label: "Incomplete With Errors", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteWithErrorResultsManifestHome")],
            [label: "Incomplete results", iconProvider: "kenyaui", icon: "", label: "Incomplete results", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteResultManifestHome")],
            [label: "Complete With Errors", iconProvider: "kenyaui", icon: "", label: "Complete With Errors", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersCompleteWithErrorResultsManifestHome")],
            [label: "Complete results", iconProvider: "kenyaui", icon: "", label: "Complete results", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersCompleteResultManifestHome")],
    ]

    def actionRequired = [
            [label: "Collect new sample", iconProvider: "kenyaui", icon: "", label: "Collect new sample", href: ui.pageLink("kenyaemrorderentry", "orders/manifestOrdersCollectSampleHome")],
            [label: "Missing samples", iconProvider: "kenyaui", icon: "", label: "Missing samples", href: ""],
    ]

    def configuration = [
            [label: "Settings", iconProvider: "kenyaui", icon: "", label: "Settings", href: ui.pageLink("kenyaemrorderentry", "orders/settings")],
    ]

    ui.includeJavascript("kenyaemrorderentry", "bootstrap.min.js")
    ui.includeJavascript("kenyaemrorderentry", "ordersUtils.js")
    ui.includeCss("kenyaemrorderentry", "bootstrap.min.css")
%>
<style>
.simple-table {
    border: solid 1px #DDEEEE;
    border-collapse: collapse;
    border-spacing: 0;
    font: normal 13px Arial, sans-serif;
}
.simple-table thead th {

    border: solid 1px #DDEEEE;
    color: #336B6B;
    padding: 10px;
    text-align: left;
    text-shadow: 1px 1px 1px #fff;
}
.simple-table td {
    border: solid 1px #DDEEEE;
    color: #333;
    padding: 5px;
    text-shadow: 1px 1px 1px #fff;
}
/*table {
    width: 90%;
}*/
th, td {
    padding: 5px;
    text-align: left;
    height: 30px;
    border-bottom: 1px solid #ddd;
}
tr:nth-child(even) {background-color: #f2f2f2;}

.nameColumn {
    width: 260px;
}
.cccNumberColumn {
    width: 150px;
}
.dateRequestColumn {
    width: 120px;
}
.actionColumn {
    width: 250px;
}
.sampleStatusColumn {
    width: 150px;
}
.sampleTypeColumn {
    width: 100px;
}
</style>

<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Back", items: menuItems])}
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Manifest status", items: manifestCategories])}
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Action required", items: actionRequired])}
    <% if(userHasSettingsEditRole) { %>
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Configuration", items: configuration])}
    <% } %>
</div>

<div class="ke-page-content">

    <div align="left" id="eligibleList">
        <fieldset>
            <legend style="color:steelblue">Samples missing in the lab [ total = ${sampleListSize}]</legend>

            <table class="simple-table" width="90%">
                <tr>
                    <th class="nameColumn">Patient Name</th>
                    <th class="cccNumberColumn">Patient Identifier</th>
                    <th class="sampleTypeColumn">Sample type</th>
                    <th class="dateRequestColumn">Date requested</th>
                    <th class="sampleStatusColumn">Status</th>
                    <th class="dateRequestColumn">Result</th>
                    <th class="dateRequestColumn">Result Date</th>
                    <th class="actionColumn"></th>
                </tr>
                <% sampleList.each { o -> %>
                <tr>
                    <td class="nameColumn"><a href="${ ui.pageLink("kenyaemr", "chart/chartViewPatient", [ patientId: o.order.patient.id ]) }">${o.order.patient.givenName} ${o.order.patient.familyName} ${o.order.patient.middleName ?: ""}</a></td>

                    <td class="cccNumberColumn">${o.order.patient.getPatientIdentifier(cccNumberType)}</td>

                    <td class="sampleTypeColumn">${o.sampleType}</td>
                    <td class="dateRequestColumn">${kenyaui.formatDate(o.order.dateActivated)}</td>
                    <td class="sampleStatusColumn">${o.status}</td>
                    <td class="dateRequestColumn">${o.result ?: "Not ready"}</td>
                    <td class="dateRequestColumn">${o.resultDate != null ? kenyaui.formatDate(o.resultDate) : ""}</td>
                    <td class="actionColumn">
                    </td>
                </tr>
                <% } %>

            </table>
        </fieldset>

    </div>
</div>

<script type="text/javascript">

    //On ready
    jq = jQuery;
    jq(function () {
        //Global Vars
        showActivePageOnManifestNavigation('Missing samples');
    });

</script>