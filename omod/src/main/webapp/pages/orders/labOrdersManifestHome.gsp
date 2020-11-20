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
</style>

<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Back", items: menuItems])}
</div>

<div class="ke-page-content">
    <div align="left">

        <button type="button" id="generateManifest" class="pushLabOrders">
            <img src="${ui.resourceLink("kenyaui", "images/glyphs/report_download_excel.png")}"
                 style="display:none;"/>Push Lab Orders
        </button>
        <button type="button" class="pullLabOrders">
            <img src="${ui.resourceLink("kenyaui", "images/glyphs/report_download_excel.png")}"
                 style="display:none;"/>Pull Lab results
        </button>

        <br/>
        <br/>
        <br/>

        <span id="msgBox"></span>
        <table>
            <tr>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Courier</th>
                <th>Status</th>
                <th></th>
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
                        <img src="${ui.resourceLink("hivtestingservices", "images/glyphs/edit.png")}"/> View Orders
                    </button>
                </td>
                <td><button>Push to lab system</button></td>
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