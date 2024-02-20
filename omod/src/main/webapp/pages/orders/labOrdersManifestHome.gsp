<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])
    ui.includeJavascript("kenyaemrorderentry", "ordersUtils.js")

    ui.includeJavascript("kenyaemrorderentry", "highcharts/highcharts.js")
    ui.includeJavascript("kenyaemrorderentry", "highcharts/modules/exporting.js")
    ui.includeJavascript("kenyaemrorderentry", "highcharts/modules/export-data.js")
    ui.includeJavascript("kenyaemrorderentry", "highcharts/modules/accessibility.js")

    ui.includeJavascript("kenyaemrorderentry", "bootstrap/bootstrap.bundle.min.js")
    ui.includeCss("kenyaemrorderentry", "bootstrap/bootstrap-iso.css")

    def menuItems = [
            [label: "Back to home", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back to home", href: ui.pageLink("kenyaemr", "userHome")]
    ]

    def manifestCategories = [
            [label: "Summary", iconProvider: "kenyaui", icon: "", label: "Summary", href: ""],
            [label: "Draft", iconProvider: "kenyaui", icon: "", label: "Draft", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersDraftManifestHome")],
            [label: "Ready to send", iconProvider: "kenyaui", icon: "", label: "Ready to send", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersReadyToSendManifestHome")],
            [label: "On hold", iconProvider: "kenyaui", icon: "", label: "On hold", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersOnHoldManifestHome")],
            [label: "Sending", iconProvider: "kenyaui", icon: "", label: "Sending", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersSendingManifestHome")],
            [label: "Submitted", iconProvider: "kenyaui", icon: "", label: "Submitted", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersSubmittedManifestHome")],
            [label: "Incomplete With Errors", iconProvider: "kenyaui", icon: "", label: "Incomplete With Errors", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteWithErrorResultsManifestHome")],
            [label: "Incomplete results", iconProvider: "kenyaui", icon: "", label: "Incomplete results", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteResultManifestHome")],
            [label: "Complete With Errors", iconProvider: "kenyaui", icon: "", label: "Complete With Errors", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersCompleteWithErrorResultsManifestHome")],
            [label: "Complete results", iconProvider: "kenyaui", icon: "", label: "Complete results", href: ui.pageLink("kenyaemrorderentry", "orders/labOrdersCompleteResultManifestHome")],
    ]

    def actionRequired = [
            [label: "Collect new sample", iconProvider: "kenyaui", icon: "", label: "Collect new sample", href: ui.pageLink("kenyaemrorderentry", "orders/manifestOrdersCollectSampleHome")],
            [label: "Missing samples", iconProvider: "kenyaui", icon: "", label: "Missing samples", href: ui.pageLink("kenyaemrorderentry", "orders/manifestOrdersMissingSamplesHome")],
    ]

    def configuration = [
            [label: "Settings", iconProvider: "kenyaui", icon: "", label: "Settings", href: ui.pageLink("kenyaemrorderentry", "orders/settings")],
            [label: "Maintenance", iconProvider: "kenyaui", icon: "", label: "Maintenance", href: ui.pageLink("kenyaemrorderentry", "orders/maintenance")],
    ]
%>

<style>
.highcharts-figure,
.highcharts-data-table table {
    min-width: 310px;
    max-width: 800px;
}

#container {
    height: 800px;
}

.highcharts-data-table table {
    font-family: Verdana, sans-serif;
    border-collapse: collapse;
    border: 1px solid #ebebeb;
    margin: 10px auto;
    text-align: center;
    width: 100%;
    max-width: 500px;
}

.highcharts-data-table caption {
    padding: 1em 0;
    font-size: 1.2em;
    color: #555;
}

.highcharts-data-table th {
    font-weight: 600;
    padding: 0.5em;
}

.highcharts-data-table td,
.highcharts-data-table th,
.highcharts-data-table caption {
    padding: 0.5em;
}

.highcharts-data-table thead tr,
.highcharts-data-table tr:nth-child(even) {
    background: #f8f8f8;
}

.highcharts-data-table tr:hover {
    background: #f1f7ff;
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

        <div class="bootstrap-iso container px-5">
            <div class="row">

                <div class="col">
                    <h4 style="color:steelblue;">Manifest Summary</h4>
                </div>

                <div class="col" align="right" text-end>
                    <button type="button" onclick="ui.navigate('${ ui.pageLink("kenyaemrorderentry", "manifest/createManifest", [ returnUrl: ui.thisUrl() ])}')">
                        <img src="${ui.resourceLink("kenyaui", "images/glyphs/add.png")}"/>
                        Add new Manifest
                    </button>
                </div>

            </div>
        </div>

        <div class="bootstrap-iso container px-5">
            <div class="row">

                <div class="col">
                    <div class="card rounded-3 text-white bg-primary mx-2">
                        <div class="card-body">
                            <div class="row">
                                <div class="col">
                                    <div class="container completeContainer" id="completeContainer" name="completeContainer">
                                        <span class="bootstrap-iso completePopover d-inline-block" tabindex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" data-bs-content="All Complete Manifests">
                                            <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersCompleteResultManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> 
                                                ${ manifestsComplete } 
                                            </a>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col">
                                    <h6 class="card-subtitle my-2 text-muted">Complete Manifests</h6>
                                </div>
                                <div class="bootstrap-iso col text-end">
                                    <div class="container completeErrorsContainer" id="completeErrorsContainer" name="completeErrorsContainer">
                                        <span class="bootstrap-iso completeErrorsPopover d-inline-block" tabindex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" data-bs-content="Complete Manifests With Errors">
                                            <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersCompleteWithErrorResultsManifestHome") }" class="btn btn-warning btn-sm">
                                                <svg xmlns="http://www.w3.org/2000/svg" role="img" width="24" height="24" fill="Red" class="bi bi-exclamation-triangle-fill flex-shrink-0 me-2" viewBox="0 0 16 16" role="img" aria-label="Danger:">
                                                    <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
                                                </svg>
                                                ${ errorsOnComplete }
                                            </a>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col">
                    <div class="card rounded-3 text-white bg-primary mx-2">
                        <div class="card-body">
                            <div class="row">
                                <div class="col">
                                    <div class="container incompleteContainer" id="incompleteContainer" name="incompleteContainer">
                                        <span class="bootstrap-iso incompletePopover d-inline-block" tabindex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" data-bs-content="All Incomplete Manifests">
                                            <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteResultManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> 
                                                ${ manifestsIncomplete } 
                                            </a>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col">
                                    <h6 class="card-subtitle my-2 text-muted">Incomplete Manifests</h6>
                                </div>
                                <div class="col text-end">
                                    <div class="container incompleteErrorsContainer" id="incompleteErrorsContainer" name="incompleteErrorsContainer">
                                        <span class="bootstrap-iso incompleteErrorsPopover d-inline-block" tabindex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" data-bs-content="Incomplete Manifests With Errors">
                                            <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteWithErrorResultsManifestHome") }" class="btn btn-warning btn-sm">
                                                <svg xmlns="http://www.w3.org/2000/svg" role="img" width="24" height="24" fill="Red" class="bi bi-exclamation-triangle-fill flex-shrink-0 me-2" viewBox="0 0 16 16" role="img" aria-label="Danger:">
                                                    <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
                                                </svg>
                                                ${ errorsOnIncomplete }
                                            </a>
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

            <div class="row">

                <div class="col mt-3">
                    <div class="card rounded-3 border-top-0 border-bottom-0 border-start-0 text-white bg-primary mx-2">
                        <div class="card-body">
                            <div class="row">
                                <div class="col text-center">
                                    <div class="container manifestsDraftContainer" id="manifestsDraftContainer" name="manifestsDraftContainer">
                                        <span class="bootstrap-iso manifestsDraftPopover d-inline-block" tabindex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" data-bs-content="Draft Manifests">
                                            <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersDraftManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsDraft } </a>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col text-center">
                                    <h6 class="card-subtitle my-2 text-muted">Draft</h6>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col mt-3">
                    <div class="card rounded-3 border-top-0 border-bottom-0 border-start-0 text-white bg-primary mx-2">
                        <div class="card-body">
                            <div class="row">
                                <div class="col text-center">
                                    <div class="container manifestsOnHoldContainer" id="manifestsOnHoldContainer" name="manifestsOnHoldContainer">
                                        <span class="bootstrap-iso manifestsOnHoldPopover d-inline-block" tabindex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" data-bs-content="Manifests On Hold">
                                            <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersOnHoldManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsOnHold } </a>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col text-center">
                                    <h6 class="card-subtitle my-2 text-muted">On Hold</h6>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col mt-3">
                    <div class="card rounded-3 border-top-0 border-bottom-0 border-start-0 text-white bg-primary mx-2">
                        <div class="card-body">
                            <div class="row">
                                <div class="col text-center">
                                    <div class="container manifestsReadyToSendContainer" id="manifestsReadyToSendContainer" name="manifestsReadyToSendContainer">
                                        <span class="bootstrap-iso manifestsReadyToSendPopover d-inline-block" tabindex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" data-bs-content="Manifests Ready To Send">
                                            <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersReadyToSendManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsReadyToSend } </a>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col text-center">
                                    <h6 class="card-subtitle my-2 text-muted">Ready to send</h6>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col mt-3">
                    <div class="card rounded-3 border-top-0 border-bottom-0 border-start-0 text-white bg-primary mx-2">
                        <div class="card-body">
                            <div class="row">
                                <div class="col text-center">
                                    <div class="container manifestsSendingContainer" id="manifestsSendingContainer" name="manifestsSendingContainer">
                                        <span class="bootstrap-iso manifestsSendingPopover d-inline-block" tabindex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" data-bs-content="Manifests In Sending Status">
                                            <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersSendingManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsSending } </a>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col text-center">
                                    <h6 class="card-subtitle my-2 text-muted">Sending</h6>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="col mt-3">
                    <div class="card rounded-3 border-top-0 border-bottom-0 border-start-0 text-white bg-primary mx-2">
                        <div class="card-body">
                            <div class="row">
                                <div class="col text-center">
                                    <div class="container manifestsSubmittedContainer" id="manifestsSubmittedContainer" name="manifestsSubmittedContainer">
                                        <span class="bootstrap-iso manifestsSubmittedPopover d-inline-block" tabindex="0" data-bs-toggle="popover" data-bs-trigger="hover focus" data-bs-content="Submitted Manifests">
                                            <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersSubmittedManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsSubmitted } </a>
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col text-center">
                                    <h6 class="card-subtitle my-2 text-muted">Submitted</h6>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

            </div>

        </div>

        <figure class="highcharts-figure">
            <div id="container"></div>
            <p class="highcharts-description">
            </p>
        </figure>

    </div>

</div>

<script type="text/javascript">

    //On ready
    jq = jQuery;
    jq(function () {
        showActivePageOnManifestNavigation('Summary');

        // Enable popovers
        var popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        var num = 1;
        var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
            // console.log("Enabling popover: " + num);
            num++;
            return new bootstrap.Popover(popoverTriggerEl);
        })

        var completeErrorsContainer = jq('#completeErrorsContainer');
        var completeErrorsPopover = new bootstrap.Popover(document.querySelector('.completeErrorsPopover'), {
            container: completeErrorsContainer
        })

        var incompleteErrorsContainer = jq('#incompleteErrorsContainer');
        var incompleteErrorsPopover = new bootstrap.Popover(document.querySelector('.incompleteErrorsPopover'), {
            container: incompleteErrorsContainer
        })

        var completeContainer = jq('#completeContainer');
        var completePopover = new bootstrap.Popover(document.querySelector('.completePopover'), {
            container: completeContainer
        })

        var incompleteContainer = jq('#incompleteContainer');
        var incompletePopover = new bootstrap.Popover(document.querySelector('.incompletePopover'), {
            container: incompleteContainer
        })

        var manifestsDraftContainer = jq('#manifestsDraftContainer');
        var manifestsDraftPopover = new bootstrap.Popover(document.querySelector('.manifestsDraftPopover'), {
            container: manifestsDraftContainer
        })

        var manifestsOnHoldContainer = jq('#manifestsOnHoldContainer');
        var manifestsOnHoldPopover = new bootstrap.Popover(document.querySelector('.manifestsOnHoldPopover'), {
            container: manifestsOnHoldContainer
        })

        var manifestsReadyToSendContainer = jq('#manifestsReadyToSendContainer');
        var manifestsReadyToSendPopover = new bootstrap.Popover(document.querySelector('.manifestsReadyToSendPopover'), {
            container: manifestsReadyToSendContainer
        })

        var manifestsSendingContainer = jq('#manifestsSendingContainer');
        var manifestsSendingPopover = new bootstrap.Popover(document.querySelector('.manifestsSendingPopover'), {
            container: manifestsSendingContainer
        })

        var manifestsSubmittedContainer = jq('#manifestsSubmittedContainer');
        var manifestsSubmittedPopover = new bootstrap.Popover(document.querySelector('.manifestsSubmittedPopover'), {
            container: manifestsSubmittedContainer
        })

    });

    function getChartData() {
        // chart data
        var summaryGraph = ${ summaryGraph };
        // console.log('Received graph data ' + JSON.stringify(summaryGraph));
        var myFullData = [];
        for (var i = 0; i < summaryGraph.length; i++) {
            var myYear = summaryGraph[i].year;
            var myJan = summaryGraph[i].jan;
            var myFeb = summaryGraph[i].feb;
            var myMar = summaryGraph[i].mar;
            var myApr = summaryGraph[i].apr;
            var myMay = summaryGraph[i].may;
            var myJun = summaryGraph[i].jun;
            var myJul = summaryGraph[i].jul;
            var myAug = summaryGraph[i].aug;
            var mySep = summaryGraph[i].sep;
            var myOct = summaryGraph[i].oct;
            var myNov = summaryGraph[i].nov;
            var myDec = summaryGraph[i].dec;

            //Construct the time series
            var myData = [myJan, myFeb, myMar, myApr, myMay, myJun, myJul, myAug, mySep, myOct, myNov, myDec];
            var obj = {
                name: "Year " + myYear,
                data: myData
            };
            myFullData.push(obj);
        }
        // console.log('Constructed series object: ' + JSON.stringify(myFullData));
        return(myFullData);
    }

    var chartSeriesData = getChartData();

    Highcharts.chart('container', {
    chart: {
        type: 'bar'
    },
    title: {
        text: 'Average Turn Around Time',
        align: 'left'
    },
    subtitle: {
        text: '',
        align: 'left'
    },
    xAxis: {
        categories: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
        title: {
            text: null
        },
        gridLineWidth: 1,
        lineWidth: 0
    },
    yAxis: {
        min: 0,
        title: {
            text: 'Duration (days)',
            align: 'high'
        },
        labels: {
            overflow: 'justify'
        },
        gridLineWidth: 0
    },
    tooltip: {
        valueSuffix: ' days'
    },
    plotOptions: {
        bar: {
            borderRadius: '50%',
            dataLabels: {
                enabled: true
            },
            groupPadding: 0.1
        }
    },
    legend: {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'top',
        x: -40,
        y: 80,
        floating: true,
        borderWidth: 1,
        backgroundColor:
            Highcharts.defaultOptions.legend.backgroundColor || '#FFFFFF',
        shadow: true
    },
    credits: {
        enabled: false
    },
    series: chartSeriesData
});

</script>