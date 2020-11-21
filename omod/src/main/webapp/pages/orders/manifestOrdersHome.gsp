<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])

    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back", href: ui.pageLink("kenyaemrorderentry", "orders/labOrderHome")]
    ]
%>
<style>
table th {
    text-align: left;
}
.nameColumn {
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
                    <td>Start date: ${kenyaui.formatDate(manifest.startDate)}</td>
                </tr>
                <tr>
                    <td>End date:  ${kenyaui.formatDate(manifest.endDate)}</td>
                </tr>
            </table>
        </fieldset>
        <br/>
        <br/>
        <fieldset>
            <legend>Request with samples taken</legend>
            <table width="50%">
                <tr>
                    <th class="nameColumn">Patient Name</th>
                    <th>CCC Number</th>
                    <th>Date requested</th>
                    <th></th>
                </tr>
                <% allOrders.each { o -> %>
                <tr>
                    <td class="nameColumn">${o.order.patient.givenName} ${o.order.patient.familyName} </td>
                    <td>${o.order.patient.getPatientIdentifier(cccNumberType)}</td>
                    <td>${kenyaui.formatDate(o.order.dateActivated)}</td>
                    <td><button>Remove</button></td>
                </tr>
                <% } %>

            </table>
        </fieldset>

        <fieldset>
            <legend>Requests with samples yet to be taken</legend>
            <table width="50%">
                <tr>
                    <th class="nameColumn">Patient Name</th>
                    <th>CCC Number</th>
                    <th>Date requested</th>
                    <th></th>
                    <th></th>
                </tr>
                <% eligibleOrders.each { o -> %>
                <tr>
                    <td class="nameColumn">${o.patient.givenName} ${o.patient.familyName} </td>
                    <td>${o.patient.getPatientIdentifier(cccNumberType)}</td>
                    <td>${kenyaui.formatDate(o.dateActivated)}</td>
                    <td><button id="addOrderToManifest" value="od_${o.orderId}">Add to manifest</button></td>
                    <td><span id="alert_${o.orderId}"></span></td>
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
        console.log("hitting the body ====================================");
        jq('#eligibleList').on('click','#addOrderToManifest',function () {
            var orderId = jq(this).val();
            console.log("Value: " + orderId);
            jq('#alert_' + orderId).text("Button clicked for order id: " + orderId);
        });
    });

</script>