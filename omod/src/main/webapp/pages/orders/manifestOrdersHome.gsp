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
.sampleTypeColumn {
    width: 100px;
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
            <table width="14%">
                <tr>
            <% if (manifest.status == 'Draft') { %>

                    <td>
            <button type="button" style="background-color: cadetblue; color: white"
                    onclick="ui.navigate('${ ui.pageLink("kenyaemrorderentry", "manifest/createManifest", [ manifestId:manifest.id, returnUrl: ui.thisUrl() ])}')">

                Edit
            </button>
                    </td>
            <% } %>
            <% if (manifestOrders.size() > 0) { %>
                    <td>
                        <a href="${ ui.pageLink("kenyaemrorderentry","manifest/downloadManifest",[manifest : manifest.id]) }"   target="_blank">
                            <button style="background-color: cadetblue; color: white">
                                Print Manifest
                            </button>
                        </a>
                    </td>

            <% } %>
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
                    <th class="sampleTypeColumn">Sample type</th>
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
                    <td class="sampleTypeColumn">${o.sampleType}</td>
                    <td class="dateRequestColumn">${kenyaui.formatDate(o.order.dateActivated)}</td>
                    <td class="sampleStatusColumn">${o.status}</td>
                    <td class="dateRequestColumn">${o.result ?: "Not ready"}</td>
                    <td class="dateRequestColumn">${o.resultDate ?: ""}</td>
                    <td class="actionColumn">
                        <button class="removeManifestOrder" style="background-color: cadetblue; color: white" value="od_${o.id}" data-target="#removeManifestOrder">Remove</button>

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

        <% if (manifest.status == "Draft") { %>
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
                        <button class="addOrderToManifest" style="background-color: cadetblue; color: white" value="od_${o.orderId}" data-target="#updateSampleDetails">Add to manifest</button>
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
                    <button type="button" class="close closeDialog" data-dismiss="modal">&times;

                    </button>
                </div>
                <div class="modal-body">
                    <input hidden="text" id="selectedOrderId"/>
                    <span style="color: firebrick" id="msgBox"></span>
                    <table>
                        <tr>
                            <td>Sample type</td>
                            <td>
                                <select id="sampleType">
                                    <option>select ...</option>
                                    <option value="Frozen plasma">Frozen plasma</option>
                                    <option value="Whole Blood">Whole Blood</option>
                                    <option value="DBS">DBS</option>
                                </select>
                            </td>
                        </tr>
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
                    <button type="button" class="close closeDialog" data-dismiss="modal">Close</button>
                    <button type="button" id="addSample">
                        <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="removeManifestOrder" tabindex="-1" role="dialog" aria-labelledby="" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered" role="document">
            <div class="modal-content">
                <div class="modal-header modal-header-primary">
                    <h5 class="modal-title">Are you sure you want to remove sample?</h5>
                    <button type="button" class="close closeDialog" data-dismiss="modal">&times;

                    </button>
                </div>
                <div class="modal-body">
                    <input hidden="text" id="selectedManifestOrderId"/>
                    <span style="color: firebrick" id="alertBox"></span>

                </div>
                <div class="modal-footer">
                    <button type="button" class="close closeDialog" data-dismiss="modal">Close</button>
                    <button type="button" id="removeSample">
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
            jq(".modal-body #sampleType").val("");

            var concatOrderId = jq(this).val();
            var orderId = concatOrderId.split("_")[1];

            jq(".modal-body #selectedOrderId").val( orderId );
            jq('#updateSampleDetails').modal('show');
        });

        jq('#addSample').click(function () {

            var selOrder = jq(".modal-body #selectedOrderId").val();
            var dateSampleCollected = jq(".modal-body #dateSampleCollected").val();
            var dateSampleSeparated = jq(".modal-body #dateSampleSeparated").val();
            var sampleType = jq(".modal-body #sampleType").val();

            var dCollected = new Date(dateSampleCollected);
            var dSeparated = new Date(dateSampleSeparated);
            var dToday = new Date();

            if (dateSampleCollected == "" || dateSampleSeparated == "" || sampleType == "") {
                jq('.modal-body #msgBox').text('Please fill all fields');
            } else {
                jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/generalLabOrders", "addOrderToManifest") }',{
                    'manifestId': ${ manifest.id },
                    'orderId': selOrder,
                    'sampleType': sampleType,
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

        // a function that removes an order from a manifest
        jq('#eligibleList').on('click','.removeManifestOrder',function () {


            var concatOrderId = jq(this).val();
            var orderId = concatOrderId.split("_")[1];

            jq(".modal-body #selectedManifestOrderId").val( orderId );
            jq('#removeManifestOrder').modal('show');
        });


        jq('#removeSample').click(function () {

            var selManifestOrder = jq(".modal-body #selectedManifestOrderId").val();
            console.log('Manifest order: ' + selManifestOrder)

            jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/generalLabOrders", "removeManifestOrder") }',{
                'manifestOrderId': selManifestOrder
            })
                .success(function (data) {
                    if (data.status == 'successful') {
                        jq('#removeManifestOrder').modal('toggle');
                        kenyaui.openAlertDialog({ heading: 'Alert', message: 'Sample successfully removed from the manifest' })
                        setTimeout(function () {
                            window.location.reload();
                        }, 2000);
                    } else {
                        jq('.modal-body #alertBox').text('Could not remove sample from the manifest!.');

                    }
                })
                .error(function (xhr, status, err) {
                    jq('.modal-body #alertBox').text('The system encountered a problem while removing the sample. Please try again');
                })
        });

        jq('.closeDialog').click(function () {

            jq(".modal-body #selectedOrderId").val("");
            jq(".modal-body #dateSampleCollected").val("");
            jq(".modal-body #dateSampleCollected_date").val("");
            jq(".modal-body #dateSampleSeparated").val("");
            jq(".modal-body #dateSampleSeparated_date").val("");
            jq(".modal-body #sampleType").val("");
            jq(".modal-body #msgBox").text("");

        });
    });

</script>