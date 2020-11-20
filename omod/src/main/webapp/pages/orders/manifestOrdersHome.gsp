<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])

    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back", href: ui.pageLink("kenyaemrorderentry", "orders/labOrderHome")]
    ]
%>
<style>
div.column-order-btns {
    width: 100px;
}

div.column-one {
    width: 180px;
}

div.column-two {
    width: 80px;
}

div.column-three {
    width: 200px;
}

div.column-four {
    width: 120px;
}
table th {
    text-align: left;
}
</style>

<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Back", items: menuItems])}
</div>

<div class="ke-page-content">

    <div align="left">
        <fieldset>
            <legend>Manifest details</legend>
            <table width="30%">
                <tr>
                    <td>Start date</td>
                    <td>${kenyaui.formatDate(manifest.startDate)}</td>
                </tr>
                <tr>
                    <td>End date</td>
                    <td>${kenyaui.formatDate(manifest.endDate)}</td>
                </tr>
            </table>
        </fieldset>
        <br/>
        <br/>
        <fieldset>
            <legend>Request with samples taken</legend>
            <table width="50%">
                <tr>
                    <th>Patient Name</th>
                    <th>CCC Number</th>
                    <th>Date requested</th>
                    <th></th>
                </tr>
                <% allOrders.each { o -> %>
                <tr>
                    <td>${o.order.patient.givenName} ${o.order.patient.familyName} </td>
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
                    <th>Patient Name</th>
                    <th>CCC Number</th>
                    <th>Date requested</th>
                    <th></th>
                </tr>
                <% allOrders.each { o -> %>
                <tr>
                    <td>${o.order.patient.givenName} ${o.order.patient.familyName} </td>
                    <td>${o.order.patient.getPatientIdentifier(cccNumberType)}</td>
                    <td>${kenyaui.formatDate(o.order.dateActivated)}</td>
                    <td><button>Add to manifest</button></td>
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
        jq('#generateManifest').click(function () {
            jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/generalLabOrders", "generateViralLoadPayload") }')
                .success(function (data) {
                    jq('#msgBox').html("Successfully generated payload");
                })
                .error(function (xhr, status, err) {
                    jq('#msgBox').html("Could not generate payload for lab");
                })
        });
    });

</script>