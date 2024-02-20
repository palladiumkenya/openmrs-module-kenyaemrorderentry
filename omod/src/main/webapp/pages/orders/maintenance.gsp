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
            [label: "Settings", iconProvider: "kenyaui", icon: "", label: "Settings", href: ui.pageLink("kenyaemrorderentry", "orders/settings")],
            [label: "Maintenance", iconProvider: "kenyaui", icon: "", label: "Maintenance", href: ""],
    ]
%>

<style>
.isinvalid {
  color: red;
}

table {
    width: 100%;
}

/*
thead tr {
    display: block;
}

thead, tbody {
    display: block;
}
tbody.scrollable {
    height: 400px;
    overflow-y: auto;
}*/
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
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Manifest status", items: manifestCategories])}
    ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Action required", items: actionRequired])}
    <% if(userHasSettingsEditRole) { %>
        ${ui.includeFragment("kenyaui", "widget/panelMenu", [heading: "Configuration", items: configuration])}
    <% } %>
</div>

<div class="ke-page-content">
    <div align="left">

        <h2 style="color:steelblue">Maintenance</h2>

        <button id="cmdLdlToZero" style="height:43px;width:295px">
            <img src="${ ui.resourceLink("kenyaui", "images/glyphs/switch.png") }" width="32" height="32" /> Migrate VL LDL To Zero
        </button>

        <br/>
        <div id="showStatus">
            <span id="msgSpan"></span> &nbsp;&nbsp;<img src="${ ui.resourceLink("kenyaui", "images/loader_small.gif") }"/>
        </div>
        <div id="msg"></div>

    </div>

</div>

<script type="text/javascript">

    //On ready
    jq = jQuery;
    jq(function () {
        // mark the activePage
        showActivePageOnManifestNavigation('Maintenance');

        jq("#showStatus").hide();
        jq('#cmdLdlToZero').click(function() {
            jq("#msgSpan").text("LDL to Zero Process");
            jq("#showStatus").show();
            jq("#msg").text("");
            jq("#cmdLdlToZero").prop("disabled", true);
            jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "manifest/manifestForm", "callLdlToZero") }')
                .success(function(data) {
                    if(data.status) {
                        jq("#showStatus").hide();
                        jq("#msg").text("LDL to Zero performed successfully");
                        jq("#cmdLdlToZero").prop("disabled", false);
                    }
                })
                .error(function(xhr, status, err) {
                    jq("#showStatus").hide();
                    jq("#msg").text("There was an error during LDL to Zero. (" + err + ")");
                    jq("#cmdLdlToZero").prop("disabled", false);

                    console.log('AJAX error ' + err);
                    alert('AJAX error ' + err);
                })
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