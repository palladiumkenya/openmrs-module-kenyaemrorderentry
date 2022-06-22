<%
    ui.decorateWith("kenyaui", "panel", [heading: (command.original ? "Edit" : "Add") + " Lab Manifest", frameOnly: true])
    def countyName = command.county
    def manifestCoverage = [
            [
                    [object: command, property: "startDate", label: "Start Date"],
                    [object: command, property: "endDate", label: "End Date"],

            ]
    ]


    def manifestDispatch = [
            [

                    [object: command, property: "dispatchDate", label: "Dispatch Date"],
                    [object: command, property: "identifier", label: "Manifest/Dispatch ID"],
                    [object: command, property: "courier", label: "Courier Name"],
                    [object: command, property: "courierOfficer", label: "Person handed to"]

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
            <% manifestDispatch.each { %>
            ${ui.includeFragment("kenyaui", "widget/rowOfFields", [fields: it])}
            <% } %>
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
                    <input type="text" id="subCounty" name="subCounty" value="${command.subCounty ?: ''}" size="30"></input>
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
                    <td class="ke-field-label">Status</td>
                </tr>
                <tr>
                    <td>
                        <select name="status">
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
            <button type="submit">
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
        //defaults
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

    }); // end of jQuery initialization bloc


</script>