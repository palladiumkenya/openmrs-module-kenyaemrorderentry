<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])
    ui.includeJavascript("kenyaemrorderentry", "jquery.twbsPagination.min.js")
    ui.includeJavascript("kenyaemrorderentry", "ordersUtils.js")

    ui.includeJavascript("kenyaemrorderentry", "bootstrap/bootstrap.bundle.min.js")
    ui.includeCss("kenyaemrorderentry", "bootstrap/bootstrap-iso.css")

    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to home", href: ui.pageLink("kenyaemr", "userHome")]
    ]

    def manifestCategories = [
            [label: "Summary", iconProvider: "kenyaui", icon: "", label: "Summary", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersManifestHome")],
            [label: "Draft", iconProvider: "kenyaui", icon: "", label: "Draft", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersDraftManifestHome")],
            [label: "Ready to send", iconProvider: "kenyaui", icon: "", label: "Ready to send", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersReadyToSendManifestHome")],
            [label: "On hold", iconProvider: "kenyaui", icon: "", label: "On hold", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersOnHoldManifestHome")],
            [label: "Sending", iconProvider: "kenyaui", icon: "", label: "Sending", href: ""],
            [label: "Submitted", iconProvider: "kenyaui", icon: "", label: "Submitted", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersSubmittedManifestHome")],
            [label: "Incomplete With Errors", iconProvider: "kenyaui", icon: "", label: "Incomplete With Errors", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteWithErrorResultsManifestHome")],
            [label: "Incomplete results", iconProvider: "kenyaui", icon: "", label: "Incomplete results", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteResultManifestHome")],
            [label: "Complete With Errors", iconProvider: "kenyaui", icon: "", label: "Complete With Errors", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersCompleteWithErrorResultsManifestHome")],
            [label: "Complete results", iconProvider: "kenyaui", icon: "", label: "Complete results", href: ""],
    ]

    def actionRequired = [
            [label: "Collect new sample", iconProvider: "kenyaui", icon: "", label: "Collect new sample", href: ui.pageLink("kenyaemrorderentry", "orders/manifestOrdersCollectSampleHome")],
            [label: "Missing samples", iconProvider: "kenyaui", icon: "", label: "Missing samples", href: ui.pageLink("kenyaemrorderentry", "orders/manifestOrdersMissingSamplesHome")],
    ]

    def configuration = [
            [label: "Settings", iconProvider: "kenyaui", icon: "", label: "Settings", href: ""],
            [label: "Maintenance", iconProvider: "kenyaui", icon: "", label: "Maintenance", href: ui.pageLink("kenyaemrorderentry", "orders/maintenance")],
    ]
%>

<style>
.isinvalid {
  color: red;
}
</style>

<div class="ke-page-sidebar">
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Back", items: menuItems])}
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Manifest status", items: manifestCategories])}
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Action required", items: actionRequired])}
    <% if(userHasSettingsEditRole) { %>
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Configuration", items: configuration])}
    <% } %>
</div>

<div class="ke-page-content">
    <div align="left">

        <h2 style="color:steelblue">Settings</h2>

        <div class="bootstrap-iso container px-5">
            <table class="bootstrap-iso table table-hover table-success table-striped table-bordered border-primary">
                <thead class="table-danger">
                    <tr>
                        <th scope="col">Setting</th>
                        <th scope="col">Value</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>System Type (${ gpSystemType }):</td>
                        <td>${ SystemType }</td>
                    </tr>
                    <tr>
                        <td class="table-primary" colspan="2"><p class="fw-bold">EID:</p></td>
                    </tr>
                    <tr>
                        <td>EID Enabled (${ gpEnableEIDFunction }):</td>
                        <td>${ EnableEIDFunction }</td>
                    </tr>
                    <tr>
                        <td>TOKEN (${ gpEIDToken }):</td>
                        <td>${ EIDToken }</td>
                    </tr>
                    <tr>
                        <td>PULL URL (${ gpEIDPullURL }):</td>
                        <td>${ EIDPullURL }</td>
                    </tr>
                    <tr>
                        <td>PUSH URL (${ gpEIDPushURL }):</td>
                        <td>${ EIDPushURL }</td>
                    </tr>
                    <tr>
                        <td class="table-primary" colspan="2"><p class="fw-bold">VL:</p></td>
                    </tr>
                    <tr>
                        <td>TOKEN (${ gpVLToken }):</td>
                        <td>${ VLToken }</td>
                    </tr>
                    <tr>
                        <td>PULL URL (${ gpVLPullURL }):</td>
                        <td>${ VLPullURL }</td>
                    </tr>
                    <tr>
                        <td>PUSH URL (${ gpVLPushURL }):</td>
                        <td>${ VLPushURL }</td>
                    </tr>
                    <tr>
                        <td class="table-primary" colspan="2"><p class="fw-bold">COMMS:</p></td>
                    </tr>
                    <tr>
                        <td>Local Endpoint (${ gpLocalResultEndpoint }):</td>
                        <td>${ LocalResultEndpoint }</td>
                    </tr>
                    <tr>
                        <td>Endpoint User Name (${ gpSchedulerUsername }):</td>
                        <td>${ SchedulerUsername }</td>
                    </tr>
                    <tr>
                        <td>Endpoint Password (${ gpSchedulerPassword }):</td>
                        <td>${ SchedulerPassword }</td>
                    </tr>
                    <tr>
                        <td>SSL Verification Enabled (${ gpSSLVerification }):</td>
                        <td>${ SSLVerification }</td>
                    </tr>
                    <tr>
                        <td class="table-primary" colspan="2"><p class="fw-bold">TIMING:</p></td>
                    </tr>
                    <tr>
                        <td>TAT in Days (${ gpLabTATForVLResults }):</td>
                        <td>${ LabTATForVLResults }</td>
                    </tr>
                    <tr>
                        <td>Retry period in Days for incomplete orders (${ gpRetryPeriodForIncompleteResults }):</td>
                        <td>${ RetryPeriodForIncompleteResults }</td>
                    </tr>
                    <tr>
                        <td class="table-primary" colspan="2"><p class="fw-bold">SCHEDULERS:</p></td>
                    </tr>
                    <tr>
                        <td>PUSH Scheduler (${ PushTaskClass }):</td>
                        <td class="bootstrap-iso p-0">
                            <table class="bootstrap-iso table table-hover table-success table-striped table-bordered border-primary m-0">
                                <tbody>
                                    <tr>
                                        <td>Started: ${ PushTaskStartOnStartup }</td>
                                        <td>Interval: ${ PushTaskInterval + " Min" }</td>
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td>PULL Scheduler (${ PullTaskClass }):</td>
                        <td class="bootstrap-iso p-0">
                            <table class="bootstrap-iso table table-hover table-success table-striped table-bordered border-primary m-0">
                                <tbody>
                                    <tr>
                                        <td>Started: ${ PullTaskStartOnStartup }</td>
                                        <td>Interval: ${ PullTaskInterval + " Min" }</td>
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </tbody>
                <tfoot>
                    <td colspan="2">
                        <% if(userHasSettingsEditRole) { %>
                            <button type="button" class="btn btn-primary btn-sm float-end editSettingsButton">
                                EDIT
                            </button>
                        <% } %>
                    </td>
                </tfoot>
            </table>
        </div>
    </div>

    <div class="bootstrap-iso">
        <div class="modal fade" id="showEditSettingsDialog" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" role="dialog" aria-labelledby="backdropLabel" aria-hidden="true">
            <div class="modal-dialog modal-dialog-scrollable modal-dialog-centered modal-xl" role="document">
                <div class="modal-content">
                    <div class="modal-header modal-header-primary">
                        <h5 class="modal-title" id="backdropLabel">Edit Manifest Settings</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <form>
                            <div class="mb-3 row">
                                <label for="selSystemType" class="col-sm-2 col-form-label">System Type</label>
                                <div class="col-sm-10">
                                    <select id="selSystemType" class="form-select" aria-label="Select System Type" aria-describedby="systemTypeHelp">
                                        <option value=""></option>
                                        <option value="CHAI" <% if (SystemType == 'CHAI') {%> selected <% } %> >CHAI</option>
                                        <option value="LABWARE" <% if (SystemType == 'LABWARE') {%> selected <% } %> >LABWARE</option>
                                        <option value="EDARP" <% if (SystemType == 'EDARP') {%> selected <% } %> >EDARP</option>
                                    </select>
                                    <div id="systemTypeHelp" class="form-text">The system type e.g CHAI, LABWARE, EDARP</div>
                                    <div class="invalid-feedback" id="systemTypeError">System Type is mandatory</div>
                                </div>
                            </div>

                            <div class="mb-3 form-check">
                                    <label class="form-check-label" for="chkEIDEnabled">EID Enabled</label>
                                    <input type="checkbox" class="form-check-input" id="chkEIDEnabled" aria-describedby="chkEIDEnabledHelp" <% if (EnableEIDFunction == 'true') {%> checked <% } %> >
                                    <div id="chkEIDEnabledHelp" class="form-text">Enable/Disable EID</div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtEIDToken" class="col-sm-2 col-form-label">EID TOKEN</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtEIDToken" aria-describedby="eidTokenHelp" value="${ EIDToken }">
                                    <div id="eidTokenHelp" class="form-text">The EID Token</div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtEIDPullURL" class="col-sm-2 col-form-label">EID Pull URL</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtEIDPullURL" aria-describedby="eidPullURLHelp" value="${ EIDPullURL }">
                                    <div id="eidPullURLHelp" class="form-text">The EID Pull URL</div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtEIDPushURL" class="col-sm-2 col-form-label">EID Push URL</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtEIDPushURL" aria-describedby="eidPushURLHelp" value="${ EIDPushURL }">
                                    <div id="eidPushURLHelp" class="form-text">The EID Push URL</div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtVLToken" class="col-sm-2 col-form-label">VL TOKEN</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtVLToken" aria-describedby="vlTokenHelp" value="${ VLToken }">
                                    <div id="vlTokenHelp" class="form-text">The VL Token</div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtVLPullURL" class="col-sm-2 col-form-label">VL Pull URL</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtVLPullURL" aria-describedby="vlPullURLHelp" value="${ VLPullURL }">
                                    <div id="vlPullURLHelp" class="form-text">The VL Pull URL</div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtVLPushURL" class="col-sm-2 col-form-label">VL Push URL</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtVLPushURL" aria-describedby="vlPushURLHelp" value="${ VLPushURL }">
                                    <div id="vlPushURLHelp" class="form-text">The VL Push URL</div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtLocalResultEndpoint" class="col-sm-2 col-form-label">Local Result Endpoint</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtLocalResultEndpoint" aria-describedby="LocalResultEndpointHelp" value="${ LocalResultEndpoint }">
                                    <div id="LocalResultEndpointHelp" class="form-text">The Local Result Endpoint</div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtSchedulerUsername" class="col-sm-2 col-form-label">Scheduler Username</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtSchedulerUsername" aria-describedby="SchedulerUsernameHelp" value="${ SchedulerUsername }">
                                    <div id="SchedulerUsernameHelp" class="form-text">The Scheduler Username</div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtSchedulerPassword" class="col-sm-2 col-form-label">Scheduler Password</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtSchedulerPassword" aria-describedby="SchedulerPasswordHelp" value="${ SchedulerPassword }">
                                    <div id="SchedulerPasswordHelp" class="form-text">The Scheduler Password</div>
                                </div>
                            </div>

                            <div class="mb-3 form-check">
                                    <label class="form-check-label" for="chkSSLVerificationEnabled">SSL Verification Enabled</label>
                                    <input type="checkbox" class="form-check-input" id="chkSSLVerificationEnabled" aria-describedby="chkSSLVerificationEnabledHelp" <% if (SSLVerification == 'true') {%> checked <% } %> >
                                    <div id="chkSSLVerificationEnabledHelp" class="form-text">Enable/Disable SSLVerification</div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtLabTATForVLResults" class="col-sm-2 col-form-label">Lab TAT For VL Results</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtLabTATForVLResults" aria-describedby="LabTATForVLResultsHelp" value="${ LabTATForVLResults }">
                                    <div id="LabTATForVLResultsHelp" class="form-text">The Lab TAT For VL Results</div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <label for="txtRetryPeriodForIncompleteResults" class="col-sm-2 col-form-label">Retry Period For Incomplete Results</label>
                                <div class="col-sm-10">
                                    <input type="text" class="form-control" id="txtRetryPeriodForIncompleteResults" aria-describedby="RetryPeriodForIncompleteResultsHelp" value="${ RetryPeriodForIncompleteResults }">
                                    <div id="RetryPeriodForIncompleteResultsHelp" class="form-text">The Retry Period For Incomplete Results</div>
                                </div>
                            </div>
                            
                            <div class="mb-3 row">
                                <div class="container">
                                    <div class="row">
                                        <div class="col-sm-8">
                                            <div class="row">
                                                <label for="txtPullSchedulerInterval" class="col-sm-4 col-form-label">Pull Scheduler Interval (seconds)</label>
                                                <div class="col-sm-4">
                                                    <input type="number" min="0" step="1" class="form-control" id="txtPullSchedulerInterval" oninput="validateInputPullSchedulerInterval()" pattern="[0-9]+" title="Please enter only numeric characters." aria-describedby="txtPullSchedulerIntervalHelp" value="${ PullTaskInterval }">
                                                    <div id="txtPullSchedulerIntervalHelp" class="form-text">The Pull Scheduler Interval (MIN)</div>
                                                    <div class="invalid-feedback"><span id="txtPullSchedulerIntervalMessage" style="color: red;"></span></div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-sm-4 form-check">
                                            <label class="form-check-label" for="chkPullSchedulerEnabled">Pull Scheduler Enabled</label>
                                            <input type="checkbox" class="form-check-input" id="chkPullSchedulerEnabled" <% if (PullTaskStartOnStartup == 'true') {%> checked <% } %> >
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="mb-3 row">
                                <div class="container">
                                    <div class="row">
                                        <div class="col-sm-8">
                                            <div class="row">
                                                <label for="txtPushSchedulerInterval" class="col-sm-4 col-form-label">Push Scheduler Interval (seconds)</label>
                                                <div class="col-sm-4">
                                                    <input type="number" min="0" step="1" class="form-control" id="txtPushSchedulerInterval" oninput="validateInputPushSchedulerInterval()" pattern="[0-9]+" title="Please enter only numeric characters." aria-describedby="txtPushSchedulerIntervalHelp" value="${ PushTaskInterval }">
                                                    <div id="txtPushSchedulerIntervalHelp" class="form-text">The Push Scheduler Interval (MIN)</div>
                                                    <div class="invalid-feedback"><span id="txtPushSchedulerIntervalMessage" style="color: red;"></span></div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-sm-4 form-check">
                                            <label class="form-check-label" for="chkPushSchedulerEnabled">Push Scheduler Enabled</label>
                                            <input type="checkbox" class="form-check-input" id="chkPushSchedulerEnabled" <% if (PushTaskStartOnStartup == 'true') {%> checked <% } %> >
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                        </form>
                    </div>
                    <div class="modal-footer modal-footer-primary">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="saveSettingsButton btn btn-primary">Save</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

</div>

<script type="text/javascript">

    //On ready
    jq = jQuery;
    jq(function () {
        // mark the activePage
        showActivePageOnManifestNavigation('Settings');
        
        // On EDIT button press
        jq(document).on('click','.editSettingsButton',function () {
            // show the edit dialog
            jq('#showEditSettingsDialog').modal('show');
        });

        function checkVarIfEmptyOrNull(msg) {
            return((msg && (msg = msg.trim())) ? true : false);
        }

        function validateVars() {
            // System Type
            var selSystemType = jq("#selSystemType").val();
            if( checkVarIfEmptyOrNull(selSystemType) == false ) { 
                console.log("System Type not selected: " + selSystemType);
                jq("#systemTypeError").text('Please select the system type.');
                jq("#systemTypeError").addClass('isinvalid');
                return(false);
            }

            // EID Enabled
            var chkEIDEnabled = jq("#chkEIDEnabled").val();
            console.log("EID enabled? : " + chkEIDEnabled);

            var txtEIDToken = jq("#txtEIDToken").val();
            var txtEIDPullURL = jq("#txtEIDPullURL").val();
            var txtEIDPushURL = jq("#txtEIDPushURL").val();

            if(chkEIDEnabled == "on") {
                if( checkVarIfEmptyOrNull(txtEIDToken) == false || checkVarIfEmptyOrNull(txtEIDPullURL) == false || checkVarIfEmptyOrNull(txtEIDPushURL) == false ) { 
                    console.log("EID is selected but no EID token and URLs given : " + chkEIDEnabled);
                    jq("#systemTypeError").text('Please select the system type.');
                    jq("#systemTypeError").addClass('isinvalid');
                    return(false);
                }
            }

            var txtVLToken = jq("#txtVLToken").val();
            var txtVLPullURL = jq("#txtVLPullURL").val();
            var txtVLPushURL = jq("#txtVLPushURL").val();

            var txtLocalResultEndpoint = jq("#txtLocalResultEndpoint").val();
            if( checkVarIfEmptyOrNull(txtLocalResultEndpoint) == false ) { 
                console.log("Local Result Endpoint cannot be empty");
                jq("#systemTypeError").text('Local result endpoint cannot be empty.');
                jq("#systemTypeError").addClass('isinvalid');
                return(false);
            }
            var txtSchedulerUsername = jq("#txtSchedulerUsername").val();
            if( checkVarIfEmptyOrNull(txtSchedulerUsername) == false ) { 
                console.log("Scheduler Username cannot be empty");
                jq("#systemTypeError").text('Scheduler Username cannot be empty.');
                jq("#systemTypeError").addClass('isinvalid');
                return(false);
            }
            var txtSchedulerPassword = jq("#txtSchedulerPassword").val();
            if( checkVarIfEmptyOrNull(txtSchedulerPassword) == false ) { 
                console.log("Scheduler Password cannot be empty");
                jq("#systemTypeError").text('Scheduler Password cannot be empty.');
                jq("#systemTypeError").addClass('isinvalid');
                return(false);
            }

            var chkSSLVerificationEnabled = jq("#chkSSLVerificationEnabled").val();

            var txtLabTATForVLResults = jq("#txtLabTATForVLResults").val();
            if( checkVarIfEmptyOrNull(txtLabTATForVLResults) == false ) { 
                console.log("Lab TAT For VL Results cannot be empty");
                jq("#systemTypeError").text('Lab TAT For VL Results cannot be empty');
                jq("#systemTypeError").addClass('isinvalid');
                return(false);
            }
            var txtRetryPeriodForIncompleteResults = jq("#txtRetryPeriodForIncompleteResults").val();
            if( checkVarIfEmptyOrNull(txtRetryPeriodForIncompleteResults) == false ) { 
                console.log("Retry Period For Incomplete Results cannot be empty");
                jq("#systemTypeError").text('Retry Period For Incomplete Results cannot be empty');
                jq("#systemTypeError").addClass('isinvalid');
                return(false);
            }

            var txtPullSchedulerInterval = jq("#txtPullSchedulerInterval").val();
            var chkPullSchedulerEnabled = jq("#chkPullSchedulerEnabled").val();
            var txtPushSchedulerInterval = jq("#txtPushSchedulerInterval").val();
            var chkPushSchedulerEnabled = jq("#chkPushSchedulerEnabled").val();

            return(true);
        }

        // Function to validate the input for numeric values
        function validateNumericInput(event) {
            console.log("KeyDown Trapped");
            const key = event.which || event.keyCode;
            const isNumericKey = (key >= 48 && key <= 57) || (key >= 96 && key <= 105);
            const allowedKeys = [8, 46, 35, 36, 37, 39]; // Backspace, Delete, End, Home, Left Arrow, Right Arrow
    
            // Block unwanted keys
            if (!isNumericKey && !allowedKeys.includes(key)) {
                event.preventDefault();
            }

            var component = event.data.component;
            var componentError = event.data.componentError;
            
            const value = component.val().trim();
            
            if (value === '' || isNaN(value)) {
                componentError.text('Please enter a valid numeric value.');
                component.addClass('isinvalid');
            } else {
                componentError.text('');
                component.removeClass('isinvalid');
            }
        }

        // Bind the validation function to the txtLabTATForVLResults keydown event
        jq('#txtLabTATForVLResults').on('keydown', { component: jq('#txtLabTATForVLResults'), componentError: jq('#txtLabTATForVLResultsMessage') }, validateNumericInput);

        // Bind the validation function to the txtRetryPeriodForIncompleteResults keydown event
        jq('#txtRetryPeriodForIncompleteResults').on('keydown', { component: jq('#txtRetryPeriodForIncompleteResults'), componentError: jq('#txtRetryPeriodForIncompleteResultsMessage') }, validateNumericInput);

        // Bind the validation function to the txtPullSchedulerInterval keydown event
        jq('#txtPullSchedulerInterval').on('keydown', { component: jq('#txtPullSchedulerInterval'), componentError: jq('#txtPullSchedulerIntervalMessage') }, validateNumericInput);

        // Bind the validation function to the txtPushSchedulerInterval keydown event
        jq('#txtPushSchedulerInterval').on('keydown', { component: jq('#txtPushSchedulerInterval'), componentError: jq('#txtPushSchedulerIntervalMessage') }, validateNumericInput);

        jq(document).on('click','.saveSettingsButton',function () {

            if(validateVars()) {
                let newPayload = "";
                var payload = {
                    selSystemType : jq("#selSystemType").val().trim(),
                    chkEIDEnabled : jq("#chkEIDEnabled").val().trim(),
                    txtEIDToken : jq("#txtEIDToken").val().trim(),
                    txtEIDPullURL : jq("#txtEIDPullURL").val().trim(),
                    txtEIDPushURL : jq("#txtEIDPushURL").val().trim(),
                    txtVLToken : jq("#txtVLToken").val().trim(),
                    txtVLPullURL : jq("#txtVLPullURL").val().trim(),
                    txtVLPushURL : jq("#txtVLPushURL").val().trim(),
                    txtLocalResultEndpoint : jq("#txtLocalResultEndpoint").val().trim(),
                    txtSchedulerUsername : jq("#txtSchedulerUsername").val().trim(),
                    txtSchedulerPassword : jq("#txtSchedulerPassword").val().trim(),
                    chkSSLVerificationEnabled : jq("#chkSSLVerificationEnabled").val().trim(),
                    txtLabTATForVLResults : jq("#txtLabTATForVLResults").val().trim(),
                    txtRetryPeriodForIncompleteResults : jq("#txtRetryPeriodForIncompleteResults").val().trim(),
                    txtPullSchedulerInterval : jq("#txtPullSchedulerInterval").val().trim(),
                    chkPullSchedulerEnabled : jq("#chkPullSchedulerEnabled").val().trim(),
                    txtPushSchedulerInterval : jq("#txtPushSchedulerInterval").val().trim(),
                    chkPushSchedulerEnabled : jq("#chkPushSchedulerEnabled").val().trim()
                };

                try {
                    newPayload = JSON.stringify(payload);
                    console.log("Sending the settings payload: " + newPayload);
                    ui.getFragmentActionAsJson('kenyaemrorderentry', 'manifest/settings', 'saveSettings', { payload : newPayload }, function (result) {
                        if(result)
                        {
                            if(result.success == true) {
                                console.log("Settings Successfully Edited");
                                jq('#showEditSettingsDialog').modal('hide');
                                kenyaui.openAlertDialog({ heading: 'Alert', message: 'Settings Successfully Edited' });
                                setTimeout(function () {
                                    window.location.reload();
                                }, 2000);
                            } else {
                                console.log("Error Saving Settings: " + result.message);
                                kenyaui.openAlertDialog({ heading: 'Alert', message: "Error Saving Settings: " + result.message });
                            }
                        } else {
                            console.log("Error Saving Settings");
                            kenyaui.openAlertDialog({ heading: 'Alert', message: 'Error Saving Settings' });
                        }
                    });
                } catch (ex) {
                    console.log("Saving Settings Error: " + ex);
                    kenyaui.openAlertDialog({ heading: 'Alert', message: "Saving Settings Error: " + ex });
                }
            } else {
                console.log("Cannot Save. Validation Failed. Fill all fields");
                kenyaui.openAlertDialog({ heading: 'Alert', message: 'Cannot Save. Validation Failed. Fill all fields' });
            }
        });

    });

    function validateVars() {
        return(true);
    }

    function validateInputPullSchedulerInterval() {
        console.log("Typing A");
    }

    function validateInputPushSchedulerInterval() {
        console.log("Typing B");
    }

</script>