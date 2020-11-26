<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])

    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to manifest list", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersManifestHome")]
    ]
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
    width: 150px;
}
.actionColumn {
    width: 260px;
}
</style>

<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Back", items: menuItems])}
</div>

<div class="ke-page-content">

    <div align="left" id="eligibleList">
        <fieldset>
            <legend>Manifest details</legend>
            <table width="70%">
                <tr>
                    <th>Start Date</th>
                    <th>End Date</th>
                    <th>Status</th>
                    <th>Dispatch Date</th>
                </tr>
                <tr>
                    <td>${kenyaui.formatDate(manifest.startDate)}</td>
                    <td>${kenyaui.formatDate(manifest.endDate)}</td>
                    <td>${manifest.status}</td>
                    <td>${manifest.dispatchDate != null ? kenyaui.formatDate(manifest.dispatchDate) : ""}</td>
                </tr>

                <tr></tr>
                <tr>
                    <td>
                        <button style="background-color: bisque;">Print Manifest</button>
                        <% if(manifest.status == "Ready to send") { %>
                        <button id="refresh" onclick="window.location.reload()">Refresh page</button>
                        <% } %>
                    </td>
                </tr>
            </table>
        </fieldset>
        <br/>
        <br/>
        <fieldset>
            <legend>Samples in the manifest</legend>
            <table class="simple-table" width="90%">
                <tr>
                    <th class="nameColumn">Patient Name</th>
                    <th class="cccNumberColumn">CCC Number</th>
                    <th class="dateRequestColumn">Date requested</th>
                    <th class="actionColumn"></th>
                    <th></th>
                </tr>
                <% manifestOrders.each { o -> %>
                <tr>
                    <td class="nameColumn">${o.order.patient.givenName} ${o.order.patient.familyName} </td>
                    <td class="cccNumberColumn">${o.order.patient.getPatientIdentifier(cccNumberType)}</td>
                    <td class="dateRequestColumn">${kenyaui.formatDate(o.order.dateActivated)}</td>
                    <td class="actionColumn">
                        <% if (manifest.status != "Sent" && manifest.status != "Sending") { %>
                        <button class="removeOrderFromManifest" value="od_${o.id}">Remove from manifest</button>
                        <% } %>
                    </td>
                    <td></td>
                </tr>
                <% } %>

            </table>
        </fieldset>

        <% if (manifest.status != "Sent") { %>
        <fieldset>
            <legend>Active requests</legend>
            <table class="simple-table" width="90%">
                <tr>
                    <th class="nameColumn">Patient Name</th>
                    <th class="cccNumberColumn">CCC Number</th>
                    <th class="dateRequestColumn">Date requested</th>
                    <th class="actionColumn"></th>
                    <th></th>
                </tr>
                <% eligibleOrders.each { o -> %>
                <tr>
                    <td class="nameColumn">${o.patient.givenName} ${o.patient.familyName} </td>
                    <td class="cccNumberColumn">${o.patient.getPatientIdentifier(cccNumberType)}</td>
                    <td class="dateRequestColumn">${kenyaui.formatDate(o.dateActivated)}</td>
                    <td class="actionColumn">
                        <button class="addOrderToManifest" value="od_${o.orderId}">Add to manifest</button>
                    </td>
                    <td><span id="alert_${o.orderId}"></span></td>
                </tr>
                <% } %>

            </table>
        </fieldset>
        <% } %>
    </div>

</div>

<script type="text/javascript">

    //On ready
    jq = jQuery;
    jq(function () {
        // a function that adds an order to a manifest
        jq('#eligibleList').on('click','.addOrderToManifest',function () {
            var concatOrderId = jq(this).val();
            var orderId = concatOrderId.split("_")[1];

            jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/generalLabOrders", "addOrderToManifest") }',{
                'manifestId': ${ manifest.id },
                'orderId': orderId
            })
                .success(function (data) {
                    if (data.status == 'successful') {
                        jq('#alert_' + orderId).text("Sample successfully added to manifest ");
                        jq(this).prop('disabled', true);
                    } else {
                        jq('#alert_' + orderId).text("Could not add to the manifest!. Please check that the patient's regimen line is correctly captured and try again ");
                    }
                })
                .error(function (xhr, status, err) {
                    jq('#alert_' + orderId).text("A problem was encountered when adding sample to the manifest ");
                })
        });
    });

</script>