<%
    ui.decorateWith("kenyaui", "panel", [heading: (command.original ? "Edit" : "Add") + " Lab Manifest", frameOnly: true])
    def countyName = command.county
    def manifestCoverage = [
            [
                    [object: command, property: "startDate", label: "Start Date"],
                    [object: command, property: "endDate", label: "End Date"],

            ]
    ]

    def facilityContactFields = [
            [

                    [object: command, property: "facilityEmail", label: "Facility Email"],
                    [object: command, property: "facilityPhoneContact", label: "Facility Phone contact"]

            ]
    ]

    def clinicianContactFields = [
            [

                    [object: command, property: "clinicianName", label: "Clinician Name"],
                    [object: command, property: "clinicianPhoneContact", label: "Clinician Phone contact"]

            ]
    ]

    def labPocFields = [
            [

                    [object: command, property: "labPocPhoneNumber", label: "Lab person Phone number"]
            ]
    ]
    def manifestTypeFields = [
            [
                    [object: command, property: "manifestType", label: "Manifest type", config: [style: "list", options: manifestTypeOptions]]

            ]
    ]

%>
<script type="text/javascript" src="/${ contextPath }/moduleResources/kenyaemr/scripts/KenyaAddressHierarchy.js"></script>
<form id="new-edit-manifest-form" method="post"
      action="${ui.actionLink("kenyaemrorderentry", "manifest/manifestForm", "saveManifest")}">
    <% if (command.original) { %>
    <input type="hidden" name="manifestId" value="${command.original.id}"/>
    <% } %>

    <div class="ke-panel-content">

        <div class="ke-form-globalerrors" style="display: none"></div>

        <div class="ke-form-instructions">
            <strong>*</strong> indicates a required field
        </div>

        <fieldset>
            <legend>Manifest date range</legend>
           <table>
               <tr>
                   <td>
                        <% manifestCoverage.each { %>
                        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
                        <% } %>
                   </td>
               </tr>
           </table>
        </fieldset>
        <fieldset>
            <legend>Manifest type</legend>
            <table>
                <tr>
                    <td>Type *</td>
                </tr>
                <tr>
                    <td>
                        <select name="manifestType" id="manifestType">
                            <option></option>
                            <% manifestTypeOptions.each { %>
                            <option ${
                                    (command.manifestType == null) ? "" : it.value == command.manifestType ? "selected" : ""}
                                    value="${it.value}">${it.label}</option>
                            <% } %>
                        </select>
                    </td>
                </tr>
            </table>
        </fieldset>
        <fieldset>
            <legend>Dispatch Details</legend>
            <table>
                <tr>
                    <td class="ke-field-label" style="width: 270px">Dispatch Date</td>
                    <td class="ke-field-label" style="width: 270px">Courier Name</td>
                    <td class="ke-field-label" style="width: 270px">Person handed to</td>
                </tr>
                <tr>
                    <td id="dispatchDate" name="dispatchDate" style="width: 270px">
                        ${ui.includeFragment("kenyaui", "widget/field", [object: command, property: "dispatchDate"])}
                    </td>
                    <td style="width: 270px"><input type="text" id="courier" name="courier" value="${command.courier ?: ''}"></td>
                    <td style="width: 270px"><input type="text" id="courierOfficer" name="courierOfficer" value="${command.courierOfficer ?: ''}"></td>
                </tr>
            </table>
        </fieldset>

    <fieldset>
        <legend>Address</legend>

        <table>
            <tr>
                <td class="ke-field-label" style="width: 265px">County</td>
                <td class="ke-field-label" style="width: 260px">Sub-County</td>
            </tr>

            <tr>
                <td style="width: 265px">
                    <select id="county" name="county">
                        <option></option>
                        <%countyList.each { %>
                        <option ${!countyName? "" : it.trim().toLowerCase() == countyName.trim().toLowerCase() ? "selected" : ""} value="${it}">${it}</option>
                        <%}%>
                    </select>
                </td>
                <td style="width: 260px">
                    <select id="subCounty" name="subCounty" value="${command.subCounty ?: ''}">
                        <option>${command.subCounty ?: lastSubCounty}</option>
                    </select>
                </td>

            </tr>
        </table>
        <% facilityContactFields.each { %>
        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
        <% } %>

        <% clinicianContactFields.each { %>
        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
        <% } %>

        <% labPocFields.each { %>
        ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
        <% } %>
    </fieldset>

        <fieldset>
            <legend>Manifest status</legend>
            <table>
                <tr>
                    <td class="ke-field-label">Status *</td>
                </tr>
                <tr>
                    <td>
                        <select name="status" id="status">
                            <option></option>
                            <% manifestStatusOptions.each { %>
                            <option ${(command.status == null)? "" : it == command.status ? "selected" : ""} value="${it}">${it}</option>
                            <% } %>
                        </select>
                    </td>
                </tr>
            </table>
        </fieldset>

        <div class="ke-panel-footer">
            <button type="submit" id="btnCreateManifest" disabled>
                <img src="${ui.resourceLink("kenyaui", "images/glyphs/ok.png")}"/> ${command.original ? "Save Changes" : "Create Lab Manifest"}
            </button>

            <button type="button" class="cancel-button"><img
                    src="${ui.resourceLink("kenyaui", "images/glyphs/cancel.png")}"/> Cancel</button>

        </div>
    </div>
</form>


<script type="text/javascript">

    //On ready
    jQuery(function () {
        
        //Enable save button if it is an edit
        //if("${command.original}" !== 'null') {
        if("${isAnEdit}" == 'true') {
            console.log("This is an edit");
            jq('#btnCreateManifest').attr('disabled', false);
        }

        //functions
        //checks if a value is empty or null - true if no, false if yes
        function checkVarIfEmptyOrNull(msg) {
            return((msg && (msg = msg.trim())) ? true : false);
        }

        // must select status
        jQuery('#status').change(function () {
            let status = jq("#status").val();
            let manifestType = jq("#manifestType").val();
            if(checkVarIfEmptyOrNull(status) && checkVarIfEmptyOrNull(manifestType)) {
                console.log("Status and Type is Selected");
                jq('#btnCreateManifest').attr('disabled', false);
            } else {
                console.log("Status and Type is NOT Selected");
                jq('#btnCreateManifest').attr('disabled', true);
            }
        });

        //must select manifest type
        jQuery('#manifestType').change(function () {
            //get value of manifest type
            let manifestType = jq("#manifestType").val();
            let status = jq("#status").val();
            if(checkVarIfEmptyOrNull(manifestType) && checkVarIfEmptyOrNull(status)) {
                console.log("Manifest Type and Status is Selected");
                jq('#btnCreateManifest').attr('disabled', false);
            } else {
                console.log("Manifest Type and status is NOT Selected");
                jq('#btnCreateManifest').attr('disabled', true);
            }
        });

        //defaults
        jQuery('#county').change(updateSubcounty);

        jQuery('#new-edit-manifest-form .cancel-button').click(function () {
            ui.navigate('${ config.returnUrl }');
        });

        kenyaui.setupAjaxPost('new-edit-manifest-form', {
            onSuccess: function (data) {
                if (data.manifestId) {
                    ui.navigate('kenyaemrorderentry', 'orders/labOrdersManifestHome');
                } else {
                    kenyaui.notifyError('Saving manifest was successful, but with unexpected response');
                }
            }
        });
        //Prepopulations

        // For new entries
        if('${command.county}' == 'null' && '${lastCounty}' != ""){
            jQuery('select[name=county]').val('${lastCounty}');
        }
        if('${command.subCounty}' == 'null' && '${lastSubCounty}' != ""){
            jQuery('select[name=subCounty]').val('${lastSubCounty}');
        }
        if('${command.facilityEmail}' == 'null' && '${lastFacilityEmail}' != ""){
            jQuery("input[name='facilityEmail']").val('${lastFacilityEmail}');
        }
        if('${command.facilityPhoneContact}' == 'null' && '${lastFacilityPhoneContact}' != ""){
            jQuery("input[name='facilityPhoneContact']").val('${lastFacilityPhoneContact}');
        }
        if('${command.clinicianName}' == 'null' && '${lastFacilityClinicianName}' != ""){
            jQuery("input[name='clinicianName']").val('${lastFacilityClinicianName}');
        }
        if('${command.clinicianPhoneContact}' == 'null' && '${lastFacilityClinicianPhone}' != ""){
            jQuery("input[name='clinicianPhoneContact']").val('${lastFacilityClinicianPhone}');
        }
        if('${command.labPocPhoneNumber}' == 'null' &&  '${lastFacilityLabPhone}' != ""){
            jQuery("input[name='labPocPhoneNumber']").val('${lastFacilityLabPhone}');
        }

        //updateSubcountyOnEdit();


    }); // end of jQuery initialization bloc

    function updateSubcounty() {

        jQuery('#subCounty').empty();
        var selectedCounty = jQuery('#county').val();
        var scKey;
        jQuery('#subCounty').append(jQuery("<option></option>").attr("value", "").text(""));
        for (scKey in kenyaAddressHierarchy[selectedCounty]) {
            jQuery('#subCounty').append(jQuery("<option></option>").attr("value", scKey).text(scKey));

        }
    }

    function updateSubcountyOnEdit() {

        jQuery('#subCounty').empty();
        var selectedCounty = jQuery('#county').val();
        var scKey;
        jQuery('#subCounty').append(jQuery("<option></option>").attr("value", "").text(""));
        for (scKey in kenyaAddressHierarchy[selectedCounty]) {

            jQuery('#subCounty').append(jQuery("<option></option>").attr("value", scKey).text(scKey));
        }
        jQuery('#subCounty').val('${command.subCounty}');
    }

</script>