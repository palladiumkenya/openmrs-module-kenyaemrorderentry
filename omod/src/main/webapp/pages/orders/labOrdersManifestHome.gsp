<%
    ui.decorateWith("kenyaemr", "standardPage", [layout: "sidebar"])
    ui.includeJavascript("kenyaemrorderentry", "jquery.twbsPagination.min.js")
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
table {
    width: 95%;
}
th, td {
    padding: 5px;
    text-align: left;
    height: 30px;
    border-bottom: 1px solid #ddd;
}
tr:nth-child(even) {background-color: #f2f2f2;}
#pager li{
    display: inline-block;
}

.pagination-sm .page-link {
    padding: .25rem .5rem;
    font-size: .875rem;
}
.page-link {
    position: relative;
    display: block;
    padding: .5rem .75rem;
    margin-left: -1px;
    line-height: 1.25;
    color: #0275d8;
    background-color: #fff;
    border: 1px solid #ddd;
}
.manifest-status {
    font-weight: bold;font-size: 14px;
}
.collect-new-sample {
    color: darkred;
    font-style: italic;
}
.missing-physical-sample {
    color: firebrick;
    font-style: italic;
}
.require-manual-updates {
    color: orangered;
    font-style: italic;
}
.result-not-ready {
    font-style: italic;
}
.viewButton {
    background-color: cadetblue;
    color: white;
    margin: 5px;
    padding: 5px;
}
.editButton {
    background-color: cadetblue;
    color: white;
    margin: 5px;
    padding: 5px;
}
.viewButton:hover {
    background-color: steelblue;
    color: white;
}
.editButton:hover {
    background-color: steelblue;
    color: white;
}
.page-content{
    background: #eee;
    display: inline-block;
    padding: 10px;
    max-width: 660px;
    font-weight: bold;
}

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
</div>

<div class="ke-page-content">
    <div align="left">

        <div class="bootstrap-iso container px-5">
            <div class="row">

                <div class="col">
                    <h4 style="color:steelblue;">Manifest Summary</h4>
                </div>

                <div class="col" align="right" text-end>
                    <button type="button"
                            onclick="ui.navigate('${ ui.pageLink("kenyaemrorderentry", "manifest/createManifest", [ returnUrl: ui.thisUrl() ])}')">
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
                                    <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersCompleteResultManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> 
                                        ${ manifestsComplete } 
                                    </a>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col">
                                    <h6 class="card-subtitle my-2 text-muted">Complete Manifests</h6>
                                </div>
                                <div class="col text-end">
                                    <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersCompleteWithErrorResultsManifestHome") }" class="btn btn-warning btn-sm">
                                        <svg xmlns="http://www.w3.org/2000/svg" role="img" width="24" height="24" fill="Red" class="bi bi-exclamation-triangle-fill flex-shrink-0 me-2" viewBox="0 0 16 16" role="img" aria-label="Danger:">
                                            <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
                                        </svg>
                                        ${ errorsOnComplete }
                                    </a>
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
                                    <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteResultManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> 
                                        ${ manifestsIncomplete } 
                                    </a>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col">
                                    <h6 class="card-subtitle my-2 text-muted">Incomplete Manifests</h6>
                                </div>
                                <div class="col text-end">
                                    <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersIncompleteWithErrorResultsManifestHome") }" class="btn btn-warning btn-sm">
                                        <svg xmlns="http://www.w3.org/2000/svg" role="img" width="24" height="24" fill="Red" class="bi bi-exclamation-triangle-fill flex-shrink-0 me-2" viewBox="0 0 16 16" role="img" aria-label="Danger:">
                                            <path d="M8.982 1.566a1.13 1.13 0 0 0-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 5.995A.905.905 0 0 1 8 5zm.002 6a1 1 0 1 1 0 2 1 1 0 0 1 0-2z"/>
                                        </svg>
                                        ${ errorsOnIncomplete }
                                    </a>
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
                                    <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersDraftManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsDraft } </a>
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
                                    <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersOnHoldManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsOnHold } </a>
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
                                    <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersReadyToSendManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsReadyToSend } </a>
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
                                    <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersSendingManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsSending } </a>
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
                                    <a href="${ ui.pageLink("kenyaemrorderentry", "orders/labOrdersSubmittedManifestHome") }" class="btn btn-white" style="font-size: larger; font-weight: bold;"> ${ manifestsSubmitted } </a>
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