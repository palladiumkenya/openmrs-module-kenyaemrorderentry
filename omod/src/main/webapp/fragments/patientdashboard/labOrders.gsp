<%
    ui.decorateWith("kenyaui", "panel", [ heading: "Lab Orders" ])


    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.11.2.js")
    ui.includeJavascript("orderentryui", "angular-material.js")
    ui.includeJavascript("orderentryui", "angular-material.min.js")
    ui.includeJavascript("orderentryui", "bootstrap.min.js")

    ui.includeJavascript("orderentryui", "labOrders.js")

    ui.includeCss("orderentryui", "angular-material.css")
    ui.includeCss("orderentryui", "angular-material.min.css")
    ui.includeCss("orderentryui", "bootstrap.min.css")
    ui.includeCss("orderentryui", "labOrders.css")


%>


<body>

<div>
    <span class="md-headline">Lab Orders</span>


    <div>
        Beginning of lab orders
        <div id="second" ng-controller="SecondController" ng-init='init()'>
        <div class="row">
            <ul class="list-group">
                <li class="list-group-item">First item</li>
                <li class="list-group-item">Second item</li>
                <li class="list-group-item">Third item</li>
            </ul>

        </div>


            <p>2: {{ desc[0].name }}</p>
        <div>
            list2
            <ul class="example-animate-container">
                <li class="animate-repeat" ng-repeat="friend in desc">
                    {{friend.name}} years old.
                </li>
            </ul>
        </div>



    </div>


    </div>

</div>

<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
    angular.bootstrap('#second', ['secondApp']);
</script>

</body>