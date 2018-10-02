<%
ui.decorateWith("kenyaui", "panel", [ heading: "Ordering set" ])

ui.includeJavascript("orderentryui", "regimenDispensation.js")


%>
<script type="text/javascript">
    var patient = OpenMRS.drugOrdersConfig.patient.uuid;
    var provider = OpenMRS.drugOrdersConfig.provider.uuid;
var selectedRegComponents;
jq(document).ready(function(){
jq(document).on("click", "li.program-line", function() {
  jq("li.program-line").removeClass("active");
  jq(this).addClass("active");
  jq("#drug-order-group").addClass("hide-section");
  jq("#regimen-lines,#active-regimens").removeClass("hide-section");
});
jq(document).on("click", "li.regimen-line", function() {
  jq("li.regimen-line").removeClass("active");
  jq(this).addClass("active");
  jq("#drug-order-group").addClass("hide-section");
});
jq(document).on("click", "li.regimen-item", function() {
  jq("li.regimen-item").removeClass("active");
  jq(this).addClass("active");
  jq("#drug-order-group").removeClass("hide-section");
});
jq(document).on("click", ".edit-order", function() {
    console.log("edit order clicked+++++++++++++++++++++++++++");
    jq("#drug-order-group").removeClass("hide-section");
    jq(".ke-tabmenu-item-active").removeClass("ke-tabmenu-item-active");
    jq(".new-order").addClass("ke-tabmenu-item-active");
    jq("div.ke-tab").css("display","none");
    jq("div.new-order-section").css("display","block");
    jq("#regimen-lines,#active-regimens").addClass("hide-section");
});
jq('#saveOrder').click(function(){
console.log("drugOrderMembers+++++++++++++++++++++++"+JSON.stringify(drugOrderMembers));
payload = {
            "patient": patient,
            "provider":provider,
            "drugs":drugOrderMembers,
            "orderSetId":orderSetId,
            "activeOrderGroupUuId":activeOrderGroupUuId,
            "discontinueOrderUuId":discontinueOrderUuId

        };
jq.getJSON('${ ui.actionLink("orderentryui", "patientdashboard/regimenDispensation", "saveOrderGroup") }',
    {
        'payload': JSON.stringify(payload)
    })
    .success(function(data) {
       payload={};
       jq("#drug-order-group").addClass("hide-section");
       jq('#order-group-success').modal('show');
       setTimeout(function(){
       jq('#order-group-success').modal('hide');
       }, 5000);
       jq("#regimen-lines,#active-regimens").removeClass("hide-section");
       window.location.reload(true);
    })
    .error(function(xhr, status, err) {
        console.log('AJAX error ' + JSON.stringify(xhr));
        console.log("response text: "+JSON.stringify(xhr.response));
        if(xhr.status==500){
            jq('#modal-text').text("Please fill all mandatory fields; dose,units,frequency and quantity.");
            jq('#order-group-error').modal('show');
        }
        else{
        jq('#modal-text').text("Problem encountered on the server while saving the order group, please try again.");
        jq('#order-group-error').modal('show');
        }
        setTimeout(function(){
       jq('#order-group-error').modal('hide');
       }, 5000);
    })
});
jq(document).on("click", ".dispenseOrder", function() {
payload = {
            "patient": patient,
            "provider":provider,
            "drugs":drugOrderMembers,
            "orderSetId":orderSetId,
            "discontinueOrderUuId":discontinueOrderUuId

        };
jq.getJSON('${ ui.actionLink("orderentryui", "patientdashboard/regimenDispensation", "discontintueOrderGroup") }',
    {
        'payload': JSON.stringify(payload)
    })
    .success(function(data) {
       payload={};
       window.location.reload(true);
    })
    .error(function(xhr, status, err) {
        console.log('AJAX error ' + JSON.stringify(xhr));
        console.log("status: "+JSON.stringify(err));
    })
});
jq(document).on("click", ".edit-single-drug,.dispense-single-drug", function() {
    jq(".ke-tabmenu-item-active").removeClass("ke-tabmenu-item-active");
    jq(".single-order").addClass("ke-tabmenu-item-active");
    jq("div.ke-tab").css("display","none");
    jq("div.single-order-section").css("display","block");
});

});
</script>
<div class="row panel panel-default">
  <div class="program panel-body">
  <h3>Programs</h3>
      <ul class="list-group">
      <li class="program-line button" ng-click="setRegimenLines(program.regimen_lines)"
      ng-repeat="program in programs.programs" style="margin:2px;">{{program.name}}</li>
      </ul>
  </div>
  <div class="regimen panel-body">
  <div ng-show="regimenLines.length > 0" style="border-style:solid;border-color:gray;padding:10px;" id="regimen-lines">
  <h3>Regimen Lines</h3>
      <ul class="list-group" style="display:inline;">
      <li class="button regimen-line" ng-repeat="regimen_line in regimenLines" style="margin:2px;"
      ng-click="setProgramRegimens(regimen_line.regimens)">{{regimen_line.name}}</li>
      </ul>
  </div>
  <div ng-show="activeRegimens.length > 0" style="border-style:solid;border-color:gray;padding:10px;margin-top:10px;" id="active-regimens">
  <h3 style="margin-top:5px;">Regimens</h3>
      <ul class="list-group" style="display:inline;">
      <li class="button regimen-item" ng-repeat="regimen in activeRegimens" style="margin:2px;width:200px;"
      ng-click="setRegimenMembers(regimen)">
      {{regimen.name}}
      </li>
      </ul>
  </div>
  <div id="drug-order-group" ng-show="components.length > 0" style="border-style:solid;border-color:gray;padding:10px;margin-top:10px;">
  <h3 style="margin-top:5px;">Drug Order Sets</h3>
  <div ng-repeat="component in components"  class="box-body" style="padding-top: 10px">
  Drug: <input ng-model="component.name" readonly="">
  Dose:<input ng-model="component.dose" size="5">
  Units:<select ng-model="component.units_uuid">
   <option ng-repeat="unit in doseUnits" ng-selected="component.units_uuid==unit.uuid" value="{{unit.uuid}}">{{unit.display}}</option>
   </select>
  Frequency:<select ng-model="component.frequency">
   <option ng-repeat="freq in frequencies" ng-selected="component.frequency==freq.uuid" value="{{freq.uuid}}">{{freq.display}}</option>
   </select>
  Quantity: <input ng-model="component.quantity" size="5">
  Units:<select ng-model="component.quantity_units">
     <option>Select</option>
     <option ng-repeat="unit in doseUnits" value="{{unit.uuid}}">{{unit.display}}</option>
     </select>
  </div>
  <div style="padding-top: 10px">
      <button ng-click="saveOrderSet(components)" id="saveOrder" style="width:250px;"><img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
  </div>
  </div>
<!-- Success Modal -->
<div class="modal fade" id="order-group-success" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true"
style="font-size:16px;">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="exampleModalLabel" style="color:green;">Success</h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body" style="color:green;">
        Order group saved successfully
      </div>
    </div>
  </div>
</div>
<!--Error Modal -->
<div class="modal fade" id="order-group-error" tabindex="-1" role="dialog" style="font-size:16px;">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="exampleModalLabel" style="color:red;">Server Error</h5>
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
