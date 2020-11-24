<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])

    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to manifest list", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersManifestHome")]
    ]
%>
<style>
table th {
    text-align: left;
}
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
            <table width="30%">
                <tr>
                    <td><b>Start date:</b> ${kenyaui.formatDate(manifest.startDate)}</td>
                </tr>
                <tr>
                    <td><b>End date:</b>  ${kenyaui.formatDate(manifest.endDate)}</td>
                </tr>
                <tr>
                    <td><b>Status:</b>  ${manifest.status}</td>
                </tr>
                <% if(manifest.dispatchDate != null) { %>
                <tr>
                    <td><b>Dispatch date:</b>  ${kenyaui.formatDate(manifest.dispatchDate)}</td>
                </tr>
                <% } %>
            </table>
        </fieldset>
        <br/>
        <br/>
        <fieldset>
            <legend>Samples in the manifest</legend>
            <table width="70%">
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
            <table width="70%">
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
                        jq('#alert_' + orderId).text("There was a problem adding to the manifest. Please confirm that the patient's regimen code is correctly mapped ");
                    }
                })
                .error(function (xhr, status, err) {
                    jq('#alert_' + orderId).text("A problem was encountered when adding sample to the manifest ");
                })
        });
    });

</script>