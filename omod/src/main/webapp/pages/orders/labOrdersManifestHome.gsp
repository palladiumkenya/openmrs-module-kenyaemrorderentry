<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])

    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to Labs", href: ui.pageLink("kenyaemrorderentry", "orders/labOrderHome")]
    ]
%>
<style>
table th {
    text-align: left;
}
</style>

<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Back", items: menuItems])}
</div>

<div class="ke-page-content">
    <div align="left">

        <h3>Manifest list</h3>
        <div>
            <button type="button"
                    onclick="ui.navigate('${ ui.pageLink("kenyaemrorderentry", "manifest/createManifest", [ returnUrl: ui.thisUrl() ])}')">
                <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/>
                Add new Manifest
            </button>
        </div>
        <br/>
        <br/>

        <table width="60%">
            <tr>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Courier</th>
                <th>Status</th>
                <th></th>
            </tr>
            <% allManifest.each { m -> %>
            <tr>
                <td>${kenyaui.formatDate(m.startDate)}</td>
                <td>${kenyaui.formatDate(m.endDate)}</td>
                <td>${m.courier}</td>
                <td>${m.status}</td>
                <td>
                    <button type="button"
                            onclick="ui.navigate('${ ui.pageLink("kenyaemrorderentry", "orders/manifestOrdersHome", [ manifest: m.id,  returnUrl: ui.thisUrl() ])}')">
                       View details
                    </button>
                    <% if(m.status != "Sent") { %>
                    <button type="button"
                            onclick="ui.navigate('${ ui.pageLink("kenyaemrorderentry", "manifest/createManifest", [ manifestId:m.id, returnUrl: ui.thisUrl() ])}')">

                        Edit details
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