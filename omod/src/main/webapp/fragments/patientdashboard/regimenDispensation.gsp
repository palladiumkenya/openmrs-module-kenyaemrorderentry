<%
    ui.decorateWith("kenyaui", "panel", [heading: "Standard Regimen"])

    ui.includeJavascript("kenyaemrorderentry", "regimenDispensation.js")


%>
<script type="text/javascript">
    var patient = OpenMRS.drugOrdersConfig.patient.uuid;
    var provider = OpenMRS.drugOrdersConfig.provider.uuid;

    jq(document).ready(function () {
        jq(document).on("click", ".edit-order", function () {
            jq("div.new-order-section").css("display", "block");
        });
        jq(document).on("click", ".cancel-order", function () {
            jq("div.new-order-section").css("display", "none");
        });
        jq('.saveOrder').click(function () {
            payload = {
                "patient": patient,
                "provider": provider,
                "drugs": drugOrderMembers,
                "orderSetId": orderSetId,
                "activeOrderGroupUuId": activeOrderGroupUuId,
                "discontinueOrderUuId": discontinueOrderUuId,
                "orderDate": jq("#orderDate_date").val(),
                "regimenDosingInstructions": regimenDosingInstructions
            };
            jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/regimenDispensation", "saveOrderGroup") }',
                {
                    'payload': JSON.stringify(payload)
                })
                .success(function (data) {
                    payload = {};
                    jq("#drug-order-group").addClass("hide-section");
                    jq('#order-group-success').modal('show');
                    setTimeout(function () {
                        jq('#order-group-success').modal('hide');
                    }, 5000);
                    jq("#regimen-lines,#active-regimens").removeClass("hide-section");
                    window.location.reload(true);
                })
                .error(function (xhr, status, err) {
                    console.log('AJAX error ' + JSON.stringify(xhr));
                    console.log("response text: " + JSON.stringify(xhr.statusText));
                    if (xhr.status == 400) {
                        jq('#modal-text').text("Request to server has invalid syntax, please contact the system administrator.");
                        jq('#order-group-error').modal('show');
                    }
                    else if (xhr.status == 401) {
                        jq('#modal-text').text("You must be authenticated to access this page, please login with your username and password.");
                        jq('#order-group-error').modal('show');
                    }
                    else if (xhr.status == 403) {
                        jq('#modal-text').text("You are not authorized to access this page, please contact the system administrator.");
                        jq('#order-group-error').modal('show');
                    }
                    else if (xhr.status == 404) {
                        jq('#modal-text').text("The requested page cannot be found, please contact the system administrator.");
                        jq('#order-group-error').modal('show');
                    }
                    else if (xhr.status == 500) {
                        jq('#modal-text').text("Please fill all mandatory fields and ensure the drug(s) you are ordering are not currently active orders.");
                        jq('#order-group-error').modal('show');
                    }
                    else if (xhr.status == 503) {
                        jq('#modal-text').text("The server is currently unavailable, please try again later.");
                        jq('#order-group-error').modal('show');
                    }
                    else {
                        jq('#modal-text').text("Problem encountered on the server while saving the order group, please try again.");
                        jq('#order-group-error').modal('show');
                    }
                    setTimeout(function () {
                        jq('#order-group-error').modal('hide');
                    }, 10000);
                })
        });
        jq(document).on("click", ".dispenseOrder,.stopOrder", function () {
            payload = {
                "patient": patient,
                "provider": provider,
                "drugs": drugOrderMembers,
                "orderSetId": orderSetId,
                "discontinueOrderUuId": discontinueOrderUuId,
                "orderDate": jq("#orderDate_date").val()
            };
            jq.getJSON('${ ui.actionLink("kenyaemrorderentry", "patientdashboard/regimenDispensation", "discontintueOrderGroup") }',
                {
                    'payload': JSON.stringify(payload)
                })
                .success(function (data) {
                    payload = {};
                    window.location.reload(true);
                })
                .error(function (xhr, status, err) {
                    console.log('AJAX error ' + JSON.stringify(xhr));
                    console.log("status: " + JSON.stringify(err));
                })
        });
        jq(document).on("click", ".edit-single-drug,.dispense-single-drug", function () {
            jq(".ke-tabmenu-item-active").removeClass("ke-tabmenu-item-active");
            jq(".single-order").addClass("ke-tabmenu-item-active");
            jq("div.ke-tab").css("display", "none");
            jq("div.single-order-section").css("display", "block");
        });
        jq(document).on("click", ".change-regimen", function () {
            jq(".disable-on-regimen-change").hide();
            jq("#drug-order-group").addClass("hide-section");
        });
        jq(document).on("click", ".refill-regimen", function () {
            jq("#drug-order-group").removeClass("hide-section");
        });
    });
</script>

<div class="row panel panel-default">


    <div class="regimen panel-body">
        Date: ${ui.includeFragment("kenyaui", "field/java.util.Date", [id: "orderDate", formFieldName: "orderDate"])}
        <div style="margin-top:5px;" ;></div>

        <div ng-show="regimenLines.length > 0" style="border-style:groove; border-width:2px;border-color:gray;padding:10px;">
            <div style="font-size: 16px">
                <b>Program:</b>{{programName}} | <b>Regimen Line:</b>{{regimenLines}} | <b>Regimen:</b>{{regimenNames}}
            </div>

        </div>

        <div id="drug-order-group" ng-show="components.length > 0"
             style="border-style:groove; border-width:2px;border-color:gray;padding:10px;margin-top:10px;">
            <h4 style="margin-top:5px;">Standard Regimen Drugs</h4>

            <div ng-repeat="component in components" class="box-body" style="padding-top: 10px">
                Drug: <input ng-model="component.name" readonly="">
                Dose:<input ng-model="component.dose" size="5">
                Units:<select ng-model="component.units_uuid">
                <option ng-repeat="unit in doseUnits" ng-selected="component.units_uuid==unit.uuid"
                        value="{{unit.uuid}}">{{unit.display}}</option>
            </select>
                Frequency:<select ng-model="component.frequency">
                <option ng-repeat="freq in frequencies" ng-selected="component.frequency==freq.uuid"
                        value="{{freq.uuid}}">{{freq.display}}</option>
            </select>
                Quantity: <input ng-model="component.quantity" size="5">
                Units:<select ng-model="component.quantity_units">
                <option ng-repeat="unit in quantityUnits" ng-selected="component.quantity_units==unit.uuid"
                        value="{{unit.uuid}}">{{unit.display}}</option>
            </select>
            </div>

            <div style="margin-top:5px;"><textarea ng-model="regimenDosingInstructions" rows="2" cols="80"
                                                   placeholder="Additional instruction not covered above"></textarea>
            </div>

            <div style="padding-top: 10px" ng-show="regimenStatus=='absent'">
                <button ng-click="saveOrderSet(components)" class="saveOrder"
                        style="width:250px;">Start Regimen</button>
            </div>



            <div style="padding-top: 10px" ng-show="regimenStatus=='stopped'">
                <button ng-click="saveOrderSet(components)" class="saveOrder"
                        style="width:250px;">Restart Regimen</button>
            </div>

            <div style="padding-top: 10px" ng-show="regimenStatus=='change'">
                <button ng-click="saveOrderSet(components)" class="saveOrder"
                        style="width:250px;">Change Regimen</button>
            </div>



        </div>
        <div style="padding-top: 10px" >

            <button ng-click="cancelView()" class="cancel-order" ng-show="regimenStatus=='edit'">Cancel</button>
            <button ng-click="saveOrderSet(components)" ng-show="regimenStatus=='edit'"  class="saveOrder"
                    class="saveOrder" >Edit Regimen</button>
                <button ng-click="cancelViewOrderRegimen()" ng-show="regimenStatus=='active'" >Cancel</button>
            <button ng-click="saveOrderSet(components)" ng-show="regimenStatus=='active'" class="saveOrder"
            >Order Regimen</button>




        </div>
        <!-- Success
        Modal -->
        <div class="modal fade" id="order-group-success" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel"
             aria-hidden="true"
             style="font-size:16px;">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header modal-header-primary">
                        <h5 class="modal-title" id="exampleModalLabel">Success</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>

                    <div class="modal-body">
                        Order saved successfully
                    </div>
                </div>
            </div>
        </div>
        <!--Error Modal -->
        <div class="modal fade" id="order-group-error" tabindex="-1" role="dialog" style="font-size:16px;">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header modal-header-primary">
                        <h5 class="modal-title" id="exampleModalLabel">Server Error</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>

                    <div class="modal-body" style="color:red;" id="modal-text"></div>
                </div>
            </div>
        </div>
    </div>
</div>
