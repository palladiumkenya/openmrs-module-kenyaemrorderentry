<%
ui.decorateWith("kenyaui", "panel", [ heading: "Ordering set" ])

ui.includeJavascript("orderentryui", "regimenDispensation.js")


%>
<script type="text/javascript">
    var patient = OpenMRS.drugOrdersConfig.patient.uuid;
    var provider = OpenMRS.drugOrdersConfig.provider.uuid;
    jq(document).ready(function(){
        jq("#saveButton").hide();
        jq("#cancelButton").hide();
 regimen = OpenMRS.orderSet.orderSets;
});
var selectedRegComponents;
jq(document).ready(function(){
    items = { '5' : 'Once daily', '4' : 'Once daily, at bedtime',
    '1' : 'Once daily, in the evening', '2' : 'Once daily, in the morning',
'3' : 'Twice daily','TDS' : 'Thrice daily'};
    var units = { '161553' : 'mg','162263' : 'ml','161554':'grams','1513' : 'tab'};
    var quantityUnits = {'161553' : 'mg','162263' : 'ml','161554':'grams','1513' : 'tab'};
jq(document).on("click", ".regimen-item", function() {
jq("li.regimen-item").removeClass("active");
jq(this).addClass("active");
jq("#drug-order-group").removeClass("hide-section");
selectedRegComponents ="";
    jq("#saveButton").hide();
    jq("#cancelButton").hide();
    selectedRegComponents = jq(this).text().trim();
    jq('#share').html("");
     console.log("this is selected value: " + selectedRegComponents);
     console.log("regimen payload  "+JSON.stringify(regimen));
    var nextRowID = 0;
        for (var c = 0; c < regimen.length; c++) {
          component = regimen[c].name;
          console.log("component is "+component);
         if(selectedRegComponents == component) {
         jq("#saveButton").show();
         jq("#cancelButton").show();
            console.log("selectedRegComponents is equal to component");
            jq.grep(regimen[c].components, function(item){
    jq(function() {
         nextRowID = nextRowID + 1;

    jq('#share').append(
    jq('<label />', { class: 'appm', text: 'Drugs:' }),
    jq('<input />', { id: 'drugs_'+nextRowID, name: 'drugs', placeholder: 'Name', type: 'text',readonly:'readonly' }).val ( item.name ),
    jq('<label /> ', { class: 'appm', text: 'Dosage:' }),
    jq('<input />', { id: 'doses_'+nextRowID, name: 'doses', placeholder: 'doses', type: 'number' }).val ( item.dose ),

    //    jq('<label />', { class: 'appm', text: 'Units:' }),

        ddlUnits =jq('<select />', { id: 'units_'+nextRowID, name: 'units', placeholder: 'units', type: 'text' }),
        jq.each(units, function(text, key) {
            option = new Option(key, text);
            if(text == item.units) {
                option.setAttribute('selected', true) ;

            }
            ddlUnits.append(jq(option));

        }),

    jq('<label />', { class: 'appm', text: 'Frequency:' }),

    ddlFrequency =jq('<select />', { id: 'frequency_'+nextRowID, name: 'frequency', placeholder: 'frequency', type: 'text' }),
    jq.each(items, function(text, key) {
     option = new Option(key, text);
    if(text == item.frequency) {
     option.setAttribute('selected', true) ;

    }
    ddlFrequency.append(jq(option));

    }),
        jq('<label /> ', { class: 'appm', text: 'Quantity:' }),
        jq('<input />', { id: 'quantity_'+nextRowID, name: 'quantity', placeholder: 'quantity', type: 'number' }),

        ddlQuantityUnits =jq('<select />', { id: 'quantityUnits_'+nextRowID, name: 'units', placeholder: 'units', type: 'text' }),
        jq.each(quantityUnits, function(text, key) {
            option = new Option(key, text);
            ddlQuantityUnits.append(jq(option));

        }),

        jq('<br />')
    )

    });

            });

          }

        }
    });
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
});
var doses;
var frequency;
var drugs;
var drugPayload = [];
var payload = {};
var quantities;
var dose_units;
var quantity_units;

jq(function() {
    jq('#saveButton').click(function() {
     //   actionLink("yourmoduleid", "encountersToday", "getEncounters");
        drugPayload = [];
        var rowID = 0;
         //var   selectedRegComponents = jq(".regComponents option:selected").val();
         var obj;
            for (var c = 0; c < regimen.length; c++) {
               var component = regimen[c].name;

                if (selectedRegComponents == component) {
                    jq.grep(regimen[c].components, function (item) {

                            rowID = rowID + 1;

                            doses = jq("#doses_"+rowID).val();
                            dose_units = jq("#units_"+rowID).val();
                            drugs = item.drug_id;
                            frequency = jq("#frequency_"+rowID).val();

                            quantities = jq("#quantity_"+rowID).val();
                            quantity_units = jq("#quantityUnits_"+rowID).val();
                            obj = {
                                "frequency" :frequency,
                                "drug" :drugs,
                                "dose" :doses,
                                "dose_unit":dose_units,
                                "quantity":quantities,
                                "quantity_unit":quantity_units


                            };

                        drugPayload.push(obj);


                    });


                }
            }
        payload = {
            "patient": patient,
            "provider":provider,
            "drugs":drugPayload

        };
        console.log('payload=======', JSON.stringify(payload));


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
      <li class="button regimen-item" ng-repeat="regimen in activeRegimens" style="margin:2px;">
      {{regimen.name}}
      </li>
      </ul>
  </div>
  <div id="drug-order-group" class="hide-section" style="border-style:solid;border-color:gray;padding:10px;margin-top:10px;">
  <h3 style="margin-top:5px;">Drug Order Sets</h3>
  <div class="box-body" id="share" style="padding-top: 10px"></div>
  <div style="padding-top: 10px">
      <button id="saveButton" ><img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
      <button id="cancelButton"><img src="${ ui.resourceLink("kenyaui", "images/glyphs/cancel.png") }" /> Cancel</button>
  </div>
  </div>
  </div>
</div>
