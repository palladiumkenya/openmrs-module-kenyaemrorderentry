<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])
    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to manifest list", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersManifestHome")]
    ]

    ui.includeJavascript("kenyaemrorderentry", "bootstrap.min.js")
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
    width: 150px;
}
.actionColumn {
    width: 150px;
}
.sampleStatusColumn {
    width: 280px;
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
            </table>
            <table>
                <tr>
                    <td>
                        <a href="${ ui.pageLink("kenyaemrorderentry","manifest/downloadManifest",[manifest : manifest.id]) }"   target="_blank">
                            <button style="background-color: cadetblue; color: white">
                                Download Manifest
                            </button>
                        </a>
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
                    <th class="sampleStatusColumn">Status</th>
                    <th class="dateRequestColumn">Result</th>
                    <th class="dateRequestColumn">Result Date</th>
                    <th class="actionColumn"></th>
                </tr>
                <% manifestOrders.each { o -> %>
                <tr>
                    <td class="nameColumn">${o.order.patient.givenName} ${o.order.patient.familyName} </td>
                    <td class="cccNumberColumn">${o.order.patient.getPatientIdentifier(cccNumberType)}</td>
                    <td class="dateRequestColumn">${kenyaui.formatDate(o.order.dateActivated)}</td>
                    <td class="sampleStatusColumn">${o.status}</td>
                    <td class="dateRequestColumn">${o.result ?: "Not ready"}</td>
                    <td class="dateRequestColumn">${o.resultDate ?: ""}</td>
                    <td class="actionColumn">
                        <% if (o.status == "Pending") { %>
                        <button class="removeOrderFromManifest" value="od_${o.id}">Remove</button>
                        <% } %>
                        <a href="${ ui.pageLink("kenyaemrorderentry","manifest/printSpecimenLabel",[manifestOrder : o.id]) }"   target="_blank">
                            <button style="background-color: cadetblue; color: white">
                                Print Label
                            </button>
                        </a>
                    </td>
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
                        <button class="addOrderToManifest" value="od_${o.orderId}" data-target="#updateSampleDetails">Add to manifest</button>
                    </td>
                    <td><span id="alert_${o.orderId}"></span></td>
                </tr>
                <% } %>

            </table>
        </fieldset>
        <% } %>
    </div>

    <!-- Modal date for vl results -->
    <div class="modal fade" id="updateSampleDetails" tabindex="-1" role="dialog" aria-labelledby="dateModalCenterTitle" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header modal-header-primary">
                    <h5 class="modal-title" id="dateVlModalCenterTitle">Update sample details</h5>
                    <button type="button" class="close" data-dismiss="modal">&times;

                    </button>
                </div>
                <div class="modal-body">
                    <input hidden="text" id="selectedOrderId"/>
                    <span style="color: firebrick" id="msgBox"></span>
                    <table>
                        <tr>
                            <td>Sample collection date</td>
                            <td>${ ui.includeFragment("kenyaui", "field/java.util.Date", [ id: "dateSampleCollected", formFieldName: "dateSampleCollected"]) }</td>
                        </tr>
                        <tr>
                            <td>Sample separation/centrifugation date</td>
                            <td>${ ui.includeFragment("kenyaui", "field/java.util.Date", [ id: "dateSampleSeparated", formFieldName: "dateSampleSeparated"]) }</td>
                        </tr>
                    </table>
                </div>
                <div class="modal-footer">
                    <button type="button" data-dismiss="modal">Close</button>
                    <button type="button" id="addSample">
                        <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
                </div>
            </div>
        </div>
    </div>

</div>

<script type="text/javascript">

    //On ready
    jq = jQuery;
    jq(function () {
        // a function that adds an order to a manifest
        jq('#eligibleList').on('click','.addOrderToManifest',function () {
            // clear previously entered values
            jq(".modal-body #selectedOrderId").val("");
            jq(".modal-body #dateSampleCollected").val("");
            jq(".modal-body #dateSampleSeparated").val("");

            var concatOrderId = jq(this).val();
            var orderId = concatOrderId.split("_")[1];

            jq(".modal-body #selectedOrderId").val( orderId );
            jq('#updateSampleDetails').modal('show');
        });

        jq('#addSample').click(function () {

            var selOrder = jq(".modal-body #selectedOrderId").val();
            var dateSampleCollected = jq(".modal-body #dateSampleCollected").val();
            var dateSampleSeparated = jq(".modal-body #dateSampleSeparated").val();

            if (dateSampleCollected == "" || dateSampleSeparated == "") {
                jq('.modal-body #msgBox').text('Please provide dates for sample collection and separation');
            } else {
                jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/generalLabOrders", "addOrderToManifest") }',{
                    'manifestId': ${ manifest.id },
                    'orderId': selOrder,
                    'dateSampleCollected': dateSampleCollected,
                    'dateSampleSeparated': dateSampleSeparated
                })
                    .success(function (data) {
                        if (data.status == 'successful') {
                            jq('#updateSampleDetails').modal('toggle');
                            kenyaui.openAlertDialog({ heading: 'Alert', message: 'Sample successfully added to the manifest' })
                            setTimeout(function () {
                                window.location.reload();
                            }, 2000);
                        } else {
                            jq('.modal-body #msgBox').text('Could not add to the manifest!. Please check that current regimen shows the regimen line');

                        }
                    })
                    .error(function (xhr, status, err) {
                        jq('.modal-body #msgBox').text('The system encountered a problem while adding the sample. Please try again');
                    })
            }
        });
    });

</script>