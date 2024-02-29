<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])
    def menuItems = [
            [label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to manifest list", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersManifestHome")]
    ]

    ui.includeJavascript("kenyaemrorderentry", "bootstrap/bootstrap.bundle.min.js")
    ui.includeCss("kenyaemrorderentry", "bootstrap/bootstrap-iso.css")

    ui.includeJavascript("kenyaemrorderentry", "datatables/datatables.min.js")
    ui.includeCss("kenyaemrorderentry", "datatables/datatables.min.css")

    ui.includeJavascript("kenyaemrorderentry", "jsonViewer/jquery.json-editor.min.js")
    ui.includeCss("kenyaemrorderentry", "jsonViewer/jquery.json-viewer.css")

%>
<style>

</style>

<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Back", items: menuItems])}
</div>

<div class="ke-page-content">

    <div align="left" id="eligibleList">
        <fieldset>
            <legend>Manifest details</legend>

            <div class="bootstrap-iso container">
                <div class="row">

                    <table id="manifestSummary" class="bootstrap-iso table table-striped" width="70%">
                        <thead>
                            <tr>
                                <th>Start Date</th>
                                <th>End Date</th>
                                <th>Status</th>
                                <th>Type</th>
                                <th>Dispatch Date</th>
                            </tr>
                        </thead>
                        <tr>
                            <td>${kenyaui.formatDate(manifest.startDate)}</td>
                            <td>${kenyaui.formatDate(manifest.endDate)}</td>
                            <td>${manifest.status}</td>
                            <td>${manifestType}</td>
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
                            <% if (manifestOrders.size() > 0 && (manifest.status != null && manifest.status.trim().toLowerCase() == 'submitted')) { %>
                                    <td>
                                        <a href="${ ui.pageLink("kenyaemrorderentry","manifest/downloadManifest",[manifest : manifest.id]) }"   target="_blank">
                                            <button style="background-color: cadetblue; color: white">
                                                Print Manifest
                                            </button>
                                        </a>
                                    </td>
                            <% } %>

                            <% if (manifestOrders.size() > 0 && (manifest.status != null && (manifest.status.trim().toLowerCase() == 'complete results' || manifest.status.trim().toLowerCase() == 'incomplete results'))) { %>
                                    <td>
                                        <a href="${ ui.pageLink("kenyaemrorderentry","manifest/downloadManifestLog",[manifest : manifest.id]) }"   target="_blank">
                                            <button style="background-color: cadetblue; color: white">
                                                Print LOG
                                            </button>
                                        </a>
                                    </td>
                            <% } %>
                        </tr>
                    </table>

                </div>
            </div>

        </fieldset>
        <br/>
        <br/>
        <fieldset>
            <legend>Samples in the manifest</legend>

            <div class="bootstrap-iso container">
                <div class="row">

                    <table id="availableSamplesTable" class="bootstrap-iso table table-striped" width="90%">
                        <thead>
                            <tr>
                                <th class="selectColumn"><input type="checkbox" class="selectManifestElement" id="selectManifestAll" value="selectManifestAll"></th>
                                <th class="nameColumn">Patient Name</th>
                                <% if (manifest.manifestType == 2) { %>
                                    <th class="cccNumberColumn">CCC/KDOD Number</th>
                                <% } else { %>
                                    <th class="cccNumberColumn">HEI Number</th>
                                <% } %>
                                <th class="batchNumberColumn">Batch Number</th>
                                <th class="sampleTypeColumn">Sample type</th>
                                <th class="dateRequestColumn">Date requested</th>
                                <th class="sampleStatusColumn">Status</th>
                                <th class="dateRequestColumn">Result</th>
                                <th class="dateRequestColumn">Result Date</th>
                                <th class="actionColumn">
                                    <% if (manifest.status != 'Complete') { %>
                                        <input type="button" id="removeSelectedOrders" value="Remove Selected Samples" disabled/>
                                    <% } %>
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <% manifestOrders.each { o -> %>
                                <tr>
                                    <td class="selectManifestColumn"><input type="checkbox" class="selectManifestElement" value="${o.id}"></td>
                                    <td class="nameColumn"><a href="${ ui.pageLink("kenyaemr", "chart/chartViewPatient", [ patientId: o.order.patient.id ]) }">${o.order.patient.givenName} ${o.order.patient.familyName} ${o.order.patient.middleName ?: ""}</a></td>
                                    <% if (manifest.manifestType == 2) { %>
                                        <td class="cccNumberColumn">${o.order.patient.getPatientIdentifier(cccNumberType)}</td>
                                    <% } else { %>
                                        <td class="cccNumberColumn">${o.order.patient.getPatientIdentifier(heiNumberType)}</td>
                                    <% } %>
                                    <td class="batchNumberColumn">${o.batchNumber != null ? o.batchNumber : ""}</td>
                                    <td class="sampleTypeColumn">${o.sampleType}</td>
                                    <td class="dateRequestColumn">${kenyaui.formatDate(o.order.dateActivated)}</td>
                                    <td class="sampleStatusColumn">${o.status}</td>
                                    <td class="dateRequestColumn">${o.result ?: "Not ready"}</td>
                                    <td class="dateRequestColumn">${o.resultDate != null ? kenyaui.formatDate(o.resultDate) : ""}</td>
                                    <td class="actionColumn">
                                        <% if (o.status != 'Complete') { %>
                                            <button class="removeManifestOrder" style="background-color: cadetblue; color: white" value="od_${o.id}" data-target="#removeManifestOrder">Remove</button>
                                        <% } %>
                                        <a href="${ ui.pageLink("kenyaemrorderentry","manifest/printSpecimenLabel",[manifestOrder : o.id]) }"   target="_blank">
                                            <button style="background-color: cadetblue; color: white">
                                                Print Label
                                            </button>
                                        </a>
                                        <button class="viewManifestOrderPayload" style="background-color: cadetblue; color: white" value="${o.id}" data-target="#viewManifestOrderPayload">View Payload</button>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>

                </div>
            </div>

        </fieldset>

        <% if (manifest.status == "Draft") { %>
        <fieldset>
            <legend>Active requests</legend>

            <div class="bootstrap-iso container">
                <div class="row">

                    <table id="activeRequestsTable" class="bootstrap-iso table table-striped" width="90%">
                        <thead>
                            <tr>
                                <th class="selectColumn"><input type="checkbox" class="selectGeneralElement" id="selectAll" value="selectAll"></th>
                                <th class="nameColumn">Patient Name</th>
                                <% if (manifest.manifestType == 2) { %>
                                    <th class="cccNumberColumn">CCC/KDOD Number</th>
                                <% } else { %>
                                    <th class="cccNumberColumn">HEI Number</th>
                                <% } %>
                                <th class="dateRequestColumn">Date requested</th>
                                <th class="actionColumn">
                                    <input type="button" id="addSelectedOrders" value="Add Selected Samples" disabled/>
                                </th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <% if (manifest.manifestType == 2) { %>
                                    <% eligibleVlOrders.each { load -> %>
                                        <tr>
                                            <td class="selectColumn"><input type="checkbox" class="selectGeneralElement" value=${load.order.id}></td>
                                            <td class="nameColumn"> <a href="${ ui.pageLink("kenyaemr", "clinician/clinicianViewPatient", [ patientId: load.order.patient.id ]) }"> ${load.order.patient.givenName} ${load.order.patient.familyName} ${load.order.patient.middleName ?: ""} </a></td>
                                            <td class="cccNumberColumn">${load.order.patient.getPatientIdentifier(cccNumberType)}</td>
                                            <td class="dateRequestColumn">${kenyaui.formatDate(load.order.dateActivated)}</td>
                                            <td class="actionColumn">
                                                <% if( load.hasProblem == false ) { %>
                                                    <button class="addOrderToManifest" style="background-color: cadetblue; color: white" value="od_${load.order.orderId}" data-target="#updateSampleDetails">Add to manifest</button>
                                                <% } else { %>
                                                    <span style="color: red" id="warning_${load.order.orderId}"> Warning: Patient requires CCC/KDOD No. and Regimen Line. Check Order reason. Check lab system configured (CHAI, LABWARE ..) </span>
                                                <% } %>
                                            </td>
                                            <td><span style="color: red" id="alert_${load.order.orderId}"></span></td>
                                        </tr>
                                    <% } %>
                            <% } %>
                            <% if (manifest.manifestType == 1) { %>
                                <% eligibleEidOrders.each { load -> %>
                                <tr>
                                    <td class="selectColumn"><input type="checkbox" class="selectGeneralElement" value=${load.order.id}></td>
                                    <td class="nameColumn"> <a href="${ ui.pageLink("kenyaemr", "clinician/clinicianViewPatient", [ patientId: load.order.patient.id ]) }"> ${load.order.patient.givenName} ${load.order.patient.familyName} ${load.order.patient.middleName ?: ""} </a></td>
                                    <td class="cccNumberColumn">${load.order.patient.getPatientIdentifier(heiNumberType)}</td>
                                    <td class="dateRequestColumn">${kenyaui.formatDate(load.order.dateActivated)}</td>
                                    <td class="actionColumn">
                                        <% if( load.hasProblem == false ) { %>
                                            <button class="addOrderToManifest" style="background-color: cadetblue; color: white" value="od_${load.order.orderId}" data-target="#updateSampleDetails">Add to manifest</button>
                                        <% } else { %>
                                            <span style="color: red" id="warning_${load.order.orderId}"> Warning: Patient requires HEI/KDOD number. Check Mother Exists. Check order reason. Check lab system configured (CHAI, LABWARE ..) </span>
                                        <% } %>
                                    </td>
                                    <td><span style="color: red" id="alert_${load.order.orderId}"></span></td>
                                </tr>
                                <% } %>
                            <% } %>
                        </tbody>
                    </table>

                </div>
            </div>

        </fieldset>
        <% } %>
    </div>

    <div class="bootstrap-iso">

        <!-- Modal data popup for adding single order to manifest -->
        <div class="bootstrap-iso modal fade" id="updateSampleDetails" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" role="dialog" aria-labelledby="dateModalCenterTitle" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered" role="document">
                <div class="modal-content">
                    <div class="modal-header modal-header-primary">
                        <h5 class="modal-title" id="dateVlModalCenterTitle">Update sample details</h5>
                        <button type="button" class="btn-close closeDialog" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <input hidden="text" id="selectedOrderId"/>
                        <span style="color: firebrick" id="msgBox"></span>
                        <table>
                            <tr>
                                <td>Sample type *</td>
                                <td>
                                    <select id="sampleType">
                                        <% if(manifest.manifestType == 2) { %>
                                            <option value="Frozen plasma">Frozen plasma</option>
                                            <option value="Whole Blood">Whole Blood</option>
                                        <% } else if(manifest.manifestType == 1) {%>
                                            <option value="DBS">DBS</option>
                                        <% } %>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td>Sample collection date *</td>
                                <td>${ ui.includeFragment("kenyaui", "field/java.util.Date", [ id: "dateSampleCollected", formFieldName: "dateSampleCollected"]) }</td>
                            </tr>
                            <tr>
                                <td>Sample separation/centrifugation date *</td>
                                <td>${ ui.includeFragment("kenyaui", "field/java.util.Date", [ id: "dateSampleSeparated", formFieldName: "dateSampleSeparated"]) }</td>
                            </tr>
                        </table>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-primary" id="addSample">
                            <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal data popup for adding multiple orders to manifest -->
        <div class="bootstrap-iso modal fade" id="addMultipleOrdersDialog" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" role="dialog" aria-labelledby="dateModalCenterTitle" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered" role="document">
                <div class="modal-content">
                    <div class="modal-header modal-header-primary">
                        <h5 class="modal-title" id="dateVlModalCenterTitle">Add Multiple Orders To Manifest</h5>
                        <button type="button" class="btn-close closeMultipleOrdersDialog" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <input hidden="text" id="selectedOrderIds"/>
                        <span style="color: firebrick" id="multipleMsgBox"></span>
                        <table>
                            <tr>
                                <td>Sample type *</td>
                                <td>
                                    <select id="multipleSamplesType">
                                        <% if(manifest.manifestType == 2) { %>
                                            <option value="Frozen plasma">Frozen plasma</option>
                                            <option value="Whole Blood">Whole Blood</option>
                                        <% } else if(manifest.manifestType == 1) {%>
                                            <option value="DBS">DBS</option>
                                        <% } %>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td>Sample collection date *</td>
                                <td>${ ui.includeFragment("kenyaui", "field/java.util.Date", [ id: "multipleDateSamplesCollected", formFieldName: "dateSampleCollected"]) }</td>
                            </tr>
                            <tr>
                                <td>Sample separation/centrifugation date *</td>
                                <td>${ ui.includeFragment("kenyaui", "field/java.util.Date", [ id: "multipleDateSamplesSeparated", formFieldName: "dateSampleSeparated"]) }</td>
                            </tr>
                        </table>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-primary" id="addMultipleSamples">
                            <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Add Samples</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal data popup for removing an order from manifest -->
        <div class="bootstrap-iso modal fade" id="removeManifestOrder" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" role="dialog" aria-labelledby="" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered" role="document">
                <div class="modal-content">
                    <div class="modal-header modal-header-primary">
                        <h5 class="modal-title">Remove sample from manifest</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <input hidden="text" id="selectedManifestOrderId"/>
                        <span style="color: firebrick" id="alertBox"></span>
                        <h5 class="modal-title">Are you sure you want to remove sample from the manifest?</h5>

                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-primary" id="removeSample">
                            <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Yes</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal dialog popup for removing multiple orders from manifest -->
        <div class="bootstrap-iso modal fade" id="removeMultipleManifestOrdersDialog" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" role="dialog" aria-labelledby="" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered" role="document">
                <div class="modal-content">
                    <div class="modal-header modal-header-primary">
                        <h5 class="modal-title">Remove multiple samples from manifest</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <input hidden="text" id="selectedManifestOrderId"/>
                        <span style="color: firebrick" id="alertBox"></span>
                        <h5 class="modal-title">Are you sure you want to remove multiple samples from the manifest?</h5>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                        <button type="button" class="btn btn-primary" id="removeMultipleManifestSamples">
                            <img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Yes</button>
                    </div>
                </div>
            </div>
        </div>

        <!-- Modal dialog popup for dsplay of manifest order payload -->
        <div class="modal fade" id="showViewManifestOrderPayloadDialog" tabindex="-1" role="dialog" aria-labelledby="backdropLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-xl" role="document">
                <div class="modal-content">
                    <div class="modal-header modal-header-primary">
                        <h5 class="modal-title" id="viewPayloadTitle">View Order Payload</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <span style="color: firebrick" id="msgBox"></span>
                        <pre id="order-payload-view-display"></pre>
                    </div>
                    <div class="modal-footer modal-footer-primary">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>

    </div>

</div>

<script type="text/javascript">
    var selectedOrders = [];
    var selectedManifestOrders = [];
    var manifestType = ${ manifest.manifestType };
    var generalOrderRecords = [];
    var manifestOrderRecords = ${ allManifestOrders };

    if(manifestType == 1) {
        // EID manifest
        generalOrderRecords = ${ EIDOrders };
    } else if(manifestType == 2) {
        // VL manifest
        generalOrderRecords = ${ VLOrders };
    }

    //On ready
    jq = jQuery;
    jq(function () {
        // Enable Search
        jq('#availableSamplesTable').dataTable();
        jq('#activeRequestsTable').dataTable();

        // Global Vars
        var manifestID = ${ manifest.id };
        var manifestType = ${ manifest.manifestType };
        var eligibleOrdersToInsert = [];

        if(manifestType == 1) {
            eligibleOrdersToInsert = ${ EIDOrders };
        } else if(manifestType == 2) {
            eligibleOrdersToInsert = ${ VLOrders };
        }

        //Bulk add all orders
        function addAllOrders() {
            console.log("Add all orders activated");
            var dCollected = "2022-07-22";
            var dSeparated = "2022-07-22";
            var dToday = new Date();
            var sampleType = "";
            if(manifestType == 2) {
                sampleType = "Whole Blood";
            } else if(manifestType == 1) {
                sampleType = "DBS";
            }
            //loop through the orders
            for (var i = 0; i < eligibleOrdersToInsert.length; i++) {
                var oID = eligibleOrdersToInsert[i].orderId;
                console.log("Inserting order: " + oID);
                // Push order to manifest
                jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/generalLabOrders", "addOrderToManifest") }',{
                    'manifestId': manifestID,
                    'orderId': oID,
                    'sampleType': sampleType,
                    'dateSampleCollected': dCollected,
                    'dateSampleSeparated': dSeparated
                })
                    .success(function (data) {
                        if (data.status == 'successful') {
                            console.log('Sample successfully added to the manifest');
                        } else {
                            console.log('Could not add to the manifest!. Please check that patient has correct regimen line and/or identifier');
                            console.log(data.cause);
                        }
                    })
                    .error(function (xhr, status, err) {
                        console.log('The system encountered a problem while adding the sample. Please try again');
                    })
            }
        }

        jq('#eligibleList').on('click','.addAllOrders',function () {
            addAllOrders();
        });

        jq(document).on('click','.viewManifestOrderPayload',function () {
            var orderId = jq(this).val();
            console.log("Checking for manfest order with id: " + orderId);

            ui.getFragmentActionAsJson('kenyaemrorderentry', 'manifest/manifestForm', 'getManifestOrderPayload', { orderId : orderId }, function (result) {
                let payloadObject = [];
                try {
                    console.log("Success got the order payload: " + result.payload);
                    payloadObject = JSON.parse(result.payload);
                } catch(ex) {
                    console.log("Failed to get the order payload");
                    payloadObject = JSON.parse("{}")
                }
                
                jq('#order-payload-view-display').empty();
                jq('#order-payload-view-display').jsonViewer(payloadObject,{
                    withQuotes:true,
                    rootCollapsable:true
                });
                jq('#viewPayloadTitle').empty();
                jq('#viewPayloadTitle').html("Payload For Manifest Order No.: " + orderId);
            });

            jq('#showViewManifestOrderPayloadDialog').modal('show');
        });

        // Button action to add an order to a manifest
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

        // Button action to add multiple orders to a manifest
        jq('#addSelectedOrders').click(function () {
            // clear previously entered values
            jq(".modal-body #selectedOrderIds").val("");
            jq(".modal-body #dateSamplesCollected").val("");
            jq(".modal-body #dateSamplesSeparated").val("");
            jq(".modal-body #samplesType").val("");

            // ensure there are selected orders before showing dialog
            if(selectedOrders.length > 0) {
                console.log("Selected orders are: " + selectedOrders.length);
                jq('#addMultipleOrdersDialog').modal('show');
            }
        });

        jq('#addSample').click(function () {

            var selOrder = jq(".modal-body #selectedOrderId").val();
            var dateSampleCollected = jq(".modal-body #dateSampleCollected").val();
            var dateSampleSeparated = jq(".modal-body #dateSampleSeparated").val();
            var sampleType = jq(".modal-body #sampleType").val();

            var dCollected = new Date(dateSampleCollected);
            var dSeparated = new Date(dateSampleSeparated);
            var dToday = new Date();

            if ( dateSampleCollected == "" || dateSampleSeparated == "" || sampleType == "" || sampleType == null || !sampleType ) {
                jq('.modal-body #msgBox').text('Please fill all fields');
            }else if (dateSampleCollected > dToday){
                jq('.modal-body #msgBox').text('Sample collection date cannot be in future');
            }else if (dSeparated > dToday){
                jq('.modal-body #msgBox').text('Sample separation date cannot be in future');
            }else if (dCollected > dSeparated ){
                jq('.modal-body #msgBox').text('Sample separation date cannot be before sample collection');
            }
            else {
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
                            jq('.modal-body #msgBox').text('Could not add to the manifest! ' + data.cause);

                        }
                    })
                    .error(function (xhr, status, err) {
                        jq('.modal-body #msgBox').text('The system encountered a problem while adding the sample. Please try again');
                    })
            }
        });

        jq('#addMultipleSamples').click( async function () {

            var dateSamplesCollected = jq("#multipleDateSamplesCollected").val();
            var dateSamplesSeparated = jq("#multipleDateSamplesSeparated").val();
            var samplesType = jq("#multipleSamplesType").val();

            var dCollected = new Date(dateSamplesCollected);
            var dSeparated = new Date(dateSamplesSeparated);
            var dToday = new Date();

            var multipleAddSuccess = 0;
            var multipleAddErrors = 0;

            if ( dateSamplesCollected == "" || dateSamplesSeparated == "" || samplesType == "" || samplesType == null || !samplesType ) {
                jq('.modal-body #multipleMsgBox').text('Please fill all fields');
                return;
            } else if ( dateSamplesCollected > dToday) {
                jq('.modal-body #multipleMsgBox').text('Sample collection date cannot be in future');
                return;
            } else if ( dSeparated > dToday) {
                jq('.modal-body #multipleMsgBox').text('Sample separation date cannot be in future');
                return;
            } else if ( dCollected > dSeparated ) {
                jq('.modal-body #multipleMsgBox').text('Sample separation date cannot be before sample collection');
                return;
            } else {
                jq('#addMultipleSamples').attr('disabled', true);
                // Loop through the selected orders
                for (var i = 0; i < selectedOrders.length; i++) {
                    let id = selectedOrders[i];
                    console.log("Adding sample id: " + id);
                    jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/generalLabOrders", "addOrderToManifest") }',{
                        'manifestId': ${ manifest.id },
                        'orderId': id,
                        'sampleType': samplesType,
                        'dateSampleCollected': dateSamplesCollected,
                        'dateSampleSeparated': dateSamplesSeparated
                    })
                        .success(function (data) {
                            if (data.status == 'successful') {
                                console.log("Success adding sample id: " + id);
                                multipleAddSuccess++;
                            } else {
                                console.log("Failed adding sample id: " + id);
                                multipleAddErrors++;
                            }
                        })
                        .error(function (xhr, status, err) {
                            console.log("Failed adding sample id: " + id);
                            multipleAddErrors++;
                        })
                    // Wait for a second
                    await new Promise(r => setTimeout(r, 1000));
                }
            }

            jq('#addMultipleOrdersDialog').modal('toggle');
            var userMsg = "Samples successfully added to the manifest are: " + multipleAddSuccess + ". Samples that failed are: " + multipleAddErrors;
            console.log(userMsg);
            kenyaui.openAlertDialog({ heading: 'Alert', message: userMsg })
            setTimeout(function () {
                window.location.reload();
            }, 2000);
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

        jq('.closeMultipleOrdersDialog').click(function () {

            jq(".modal-body #selectedOrderIds").val("");
            jq(".modal-body #dateSamplesCollected").val("");
            jq(".modal-body #dateSamplesCollected_date").val("");
            jq(".modal-body #dateSamplesSeparated").val("");
            jq(".modal-body #dateSamplesSeparated_date").val("");
            jq(".modal-body #samplesType").val("");
            jq(".modal-body #multipleMsgBox").text("");

        });

        // On selecting the select all checkbox
        jq("#selectAll").change(function() {
            let len = jq('.selectGeneralElement:checked').length;
            if (len > 0) {
                jq('#addSelectedOrders').attr('disabled', false);
            } else {
                jq('#addSelectedOrders').attr('disabled', true);
            }
        });

        // On selecting an order
        jq(".selectGeneralElement").change(function() {
            let len = jq('.selectGeneralElement:checked').length;
            if (len > 0) {
                jq('#addSelectedOrders').attr('disabled', false);
            } else {
                jq('#addSelectedOrders').attr('disabled', true);
            }
        });

        // On selecting an order checkbox
        jq(document).on('click','.selectGeneralElement',function () {
            var orderId = jq(this).val();
            if (jq(this).is(":checked")) {
                selectedOrders.push(orderId);
            }
            else {
                 var elemIndex = selectedOrders.indexOf(orderId);
                 if (elemIndex > -1) {
                    selectedOrders.splice(elemIndex, 1);
                 }
                 jq('#selectAll').prop('checked', false);
             }
        });

        // handle select all orders
        jq(document).on('click','#selectAll',function () {
            //clear selection list
            selectedOrders = [];
            if(jq(this).is(':checked')) {
                jq('.selectGeneralElement').prop('checked', true);
                // populate the list with all elements
                for (var i = 0; i < generalOrderRecords.length; i++) {
                    let id = generalOrderRecords[i].orderId;
                    selectedOrders.push(id);
                }
            }
            else {
                jq('.selectGeneralElement').prop('checked', false);
            }
        });

        // Samples in manifest section

        // On selecting the select all manifest orders checkbox
        jq("#selectManifestAll").change(function() {
            let len = jq('.selectManifestElement:checked').length;
            if (len > 0) {
                jq('#removeSelectedOrders').attr('disabled', false);
            } else {
                jq('#removeSelectedOrders').attr('disabled', true);
            }
        });

        // On selecting a manifest order
        jq(".selectManifestElement").change(function() {
            let len = jq('.selectManifestElement:checked').length;
            if (len > 0) {
                jq('#removeSelectedOrders').attr('disabled', false);
            } else {
                jq('#removeSelectedOrders').attr('disabled', true);
            }
        });

        // On selecting a manifest order checkbox
        jq(document).on('click','.selectManifestElement',function () {
            var orderId = jq(this).val();
            if (jq(this).is(":checked")) {
                selectedManifestOrders.push(orderId);
            }
            else {
                 var elemIndex = selectedManifestOrders.indexOf(orderId);
                 if (elemIndex > -1) {
                    selectedManifestOrders.splice(elemIndex, 1);
                 }
                 jq('#selectManifestAll').prop('checked', false);
             }
        });

        // handle select all manifest orders
        jq(document).on('click','#selectManifestAll',function () {
            //clear selection list
            selectedManifestOrders = [];
            if(jq(this).is(':checked')) {
                jq('.selectManifestElement').prop('checked', true);
                // populate the list with all manifest orders
                for (var i = 0; i < manifestOrderRecords.length; i++) {
                    let id = manifestOrderRecords[i].orderId;
                    selectedManifestOrders.push(id);
                }
            }
            else {
                jq('.selectManifestElement').prop('checked', false);
            }
        });

        // Button action to remove multiple orders from a manifest
        jq('#removeSelectedOrders').click(function () {
            // ensure there are selected manifest orders before showing dialog
            if(selectedManifestOrders.length > 0) {
                console.log("Selected manifest orders are: " + selectedManifestOrders.length);
                jq('#removeMultipleManifestOrdersDialog').modal('show');
            }
        });

        jq('#removeMultipleManifestSamples').click( async function () {
            var multipleRemoveSuccess = 0;
            var multipleRemoveErrors = 0;
            jq('#removeMultipleManifestSamples').attr('disabled', true);

            // Loop through the selected manifest orders
            for (var i = 0; i < selectedManifestOrders.length; i++) {
                let id = selectedManifestOrders[i];
                console.log("Removing sample id: " + id);
                jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/generalLabOrders", "removeManifestOrder") }',{
                    'manifestOrderId': id
                })
                    .success(function (data) {
                        if (data.status == 'successful') {
                            console.log("Success removing sample id: " + id);
                            multipleRemoveSuccess++;
                        } else {
                            console.log("Failed removing sample id: " + id);
                            multipleRemoveErrors++;
                        }
                    })
                    .error(function (xhr, status, err) {
                        console.log("Failed removing sample id: " + id);
                        multipleRemoveErrors++;
                    })
                // Wait for a second
                await new Promise(r => setTimeout(r, 1000));
            }

            jq('#removeMultipleManifestOrdersDialog').modal('toggle');
            var userMsg = "Samples successfully removed from the manifest are: " + multipleRemoveSuccess + ". Samples that failed are: " + multipleRemoveErrors;
            console.log(userMsg);
            kenyaui.openAlertDialog({ heading: 'Alert', message: userMsg })
            setTimeout(function () {
                window.location.reload();
            }, 2000);

        });

    });

</script>