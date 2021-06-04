<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])

    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back", href: ui.pageLink("kenyaemr", "userHome")]
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
table {
    width: 95%;
}
th, td {
    padding: 5px;
    text-align: left;
    height: 30px;
    border-bottom: 1px solid #ddd;
}
tr:nth-child(even) {background-color: #f2f2f2;}

</style>

<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Back", items: menuItems])}
</div>

<div class="ke-page-content">
    <div align="left">

        <h2>Manifest list</h2>
        <div>
            <button type="button"
                    onclick="ui.navigate('${ ui.pageLink("kenyaemrorderentry", "manifest/createManifest", [ returnUrl: ui.thisUrl() ])}')">
                <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/>
                Add new Manifest
            </button>
        </div>
        <br/>
        <br/>

        <table class="simple-table">
            <tr>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Courier</th>
                <th>County/Sub-county</th>
                <th>Facility Email</th>
                <th>Facility Phone</th>
                <th>Clinician name</th>
                <th>Clinician contact</th>
                <th>Lab person contact</th>
                <th>Status</th>
                <th>Dispatch</th>
                <th>Action</th>
            </tr>
            <% allManifest.each { m -> %>
            <tr>
                <td>${kenyaui.formatDate(m.manifest.startDate)}</td>
                <td>${kenyaui.formatDate(m.manifest.endDate)}</td>
                <td>${m.manifest.courier ?: ""}</td>
                <td>${m.manifest.county ?: ""} / ${m.manifest.subCounty ?: ""}</td>
                <td>${m.manifest.facilityEmail ?: ""}</td>
                <td>${m.manifest.facilityPhoneContact ?: ""}</td>
                <td>${m.manifest.clinicianName ?: ""}</td>
                <td>${m.manifest.clinicianPhoneContact ?: ""}</td>
                <td>${m.manifest.labPocPhoneNumber ?: ""}</td>
                <td>
                    <span style="font-weight: bold;font-size: 14px">${m.manifest.status ?: ""}</span>
                    <% if (m.collectNewSample > 0) {%> <br/> <span style="color: darkred;font-style: italic">New samples required: ${m.collectNewSample} </span><%}%>
                <% if (m.missingPhysicalSample > 0) {%> <br/> <span style="color: firebrick;font-style: italic">Missing physical sample: ${m.missingPhysicalSample} </span><%}%>
                <% if (m.manualUpdates > 0) {%> <br/> <span style="color: orangered;font-style: italic">Manual updates required: ${m.manualUpdates} </span><%}%>
                <% if (m.incompleteSample > 0) {%> <br/> <span style="font-style: italic;">Result not ready: ${m.incompleteSample} </span> <%}%>
                </td>
                <td>${m.manifest.dispatchDate != null ? kenyaui.formatDate(m.manifest.dispatchDate) : ""}</td>

            <td>
                    <button type="button" style="background-color: cadetblue; color: white"
                            onclick="ui.navigate('${ ui.pageLink("kenyaemrorderentry", "orders/manifestOrdersHome", [ manifest: m.manifest.id,  returnUrl: ui.thisUrl() ])}')">
                       View
                    </button>
                    <% if(m.status == "Draft") { %>
                    <button type="button" style="background-color: cadetblue; color: white"
                            onclick="ui.navigate('${ ui.pageLink("kenyaemrorderentry", "manifest/createManifest", [ manifestId:m.manifest.id, returnUrl: ui.thisUrl() ])}')">

                        Edit
                    </button>
                    <% } %>
                </td>
            </tr>
            <% } %>

        </table>

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