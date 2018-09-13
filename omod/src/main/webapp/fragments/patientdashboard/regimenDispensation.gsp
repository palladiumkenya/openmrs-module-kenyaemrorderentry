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
jq('#saveOrder').click(function(){
console.log("drugOrderMembers+++++++++++++++++++++++"+JSON.stringify(drugOrderMembers));
payload = {
            "patient": patient,
            "provider":provider,
            "drugs":drugOrderMembers,
            "orderSetId":orderSetId

        };
jq.getJSON('${ ui.actionLink("orderentryui", "patientdashboard/regimenDispensation", "saveOrderGroup") }',
    {
        'payload': JSON.stringify(payload)
    })
    .success(function(data) {
        console.log('payload submitted successfully');

    })
    .error(function(xhr, status, err) {
        console.log('AJAX error ' + JSON.stringify(xhr));
        console.log("status: "+JSON.stringify(err));
    })
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
  <div ng-show="regimenLines.length > 0" style="border-style:solid;border-color:gray;padding:10px;">
  <h3>Regimen Lines</h3>
      <ul class="list-group" style="display:inline;">
      <li class="button regimen-line" ng-repeat="regimen_line in regimenLines" style="margin:2px;"
      ng-click="setProgramRegimens(regimen_line.regimens)">{{regimen_line.name}}</li>
      </ul>
  </div>
  <div ng-show="activeRegimens.length > 0" style="border-style:solid;border-color:gray;padding:10px;margin-top:10px;">
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
  Units:<select ng-model="component.units">
   <option ng-repeat="unit in doseUnits" value="{{unit.uuid}}">{{unit.display}}</option>
   </select>
  Frequency:<select ng-model="component.frequency">
   <option ng-repeat="freq in frequencies" value="{{freq.uuid}}">{{freq.display}}</option>
   </select>
  Quantity: <input ng-model="component.quantity" size="5">
  Units:<select ng-model="component.units_uuid">
     <option ng-repeat="unit in doseUnits" value="{{unit.uuid}}">{{unit.display}}</option>
     </select>
  </div>
  <div style="padding-top: 10px">
      <button ng-click="saveOrderSet(components)" id="saveOrder" style="width:250px;"><img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
  </div>
  </div>
  </div>
</div>
