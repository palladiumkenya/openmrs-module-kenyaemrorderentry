<%
ui.decorateWith("kenyaui", "panel", [ heading: "Ordering set" ])

ui.includeJavascript("orderentryui", "regimenDispensation.js")


%>


<body>

        <hr />
        Regimen: <select id="ddlRegimen" class="regComponents">
        </select>
        <br />
        <div>

       </div>
       <div class="box-body" id="share" style="padding-top: 10px">


       </div>
<div>
<div style="padding-top: 10px">
    <button id="saveButton" ><img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
    <button id="cancelButton"><img src="${ ui.resourceLink("kenyaui", "images/glyphs/cancel.png") }" /> Cancel</button>
</div>
</div>



    <script type="text/javascript">

        var patient = OpenMRS.drugOrdersConfig.patient.uuid;
        var provider = OpenMRS.drugOrdersConfig.provider.uuid;
        jq(document).ready(function(){
            jq("#saveButton").hide();
            jq("#cancelButton").hide();

    //Build an array containing e regimen records.
     regimenTest = [{
                       "name":"TDF + 3TC + NVP (300mg OD/150mg BD/200mg BD)",
                       "components": [
                {
                    "name":"TDF",
                    "dose":"300",
                    "drug_id":"1",
                    "units":"mg",
                    "units_uuid": '',
                    "frequency":"5"
                },
                {
                    "name":"3TC",
                    "dose":"150",
                    "drug_id":"2",
                    "units":"mg",
                    "units_uuid": '',
                    "frequency":"3"
                },
                {
                    "name":"NVP",
                    "dose":"200",
                    "drug_id":"4",
                    "units":"mg",
                    "units_uuid": '',
                    "frequency":"3"
                }
                   ]

                   },
                   {
                       "name":"TDF + 3TC(BD) + EFV (300mg OD/150mg BD/600mg OD)",
                       "components": [
                           {
                               "name":"TDF",
                               "drug_id":"1",
                               "dose":"300",
                               "units":"mg",
                               "units_uuid": '',
                               "frequency":"5"
                           },
                           {
                               "name":"3TC",
                               "drug_id":"2",
                               "dose":"150",
                               "units":"mg",
                               "units_uuid": '',
                               "frequency":"3"
                           },
                           {
                               "name":"EFV",
                               "drug_id":"3",
                               "dose":"600",
                               "units":"mg",
                               "units_uuid": '',
                               "frequency":"5"
                           }
                   ]

                   }];

    //Build an array containing e regimen records.
     regimen = OpenMRS.orderSet.orderSets;
     regimen.unshift({"name": "Select regimen"});


    var ddlRegimen = jq("#ddlRegimen");
    jq(regimen).each(function () {
        var option = jq("<option />");
        option.html("select regimens");

        option.html(this.name);
        option.val(this.name);
        ddlRegimen.append(option);
    });
});

jq(document).ready(function(){
    items = { '5' : 'Once daily', '4' : 'Once daily, at bedtime',
    '1' : 'Once daily, in the evening', '2' : 'Once daily, in the morning',
'3' : 'Twice daily','TDS' : 'Thrice daily'};
    var units = { '161553' : 'mg',
        '162263' : 'ml'};
    var quantityUnits = { '1513' : 'tab'};

jq("select.regComponents").change(function(){
    jq("#saveButton").show();
    jq("#cancelButton").show();


        var selectedRegComponents = jq(".regComponents option:selected").val();
        if(selectedRegComponents == 'Select regimen') {
            jq("#saveButton").hide();
            jq("#cancelButton").hide();

        }

        jq('#share').html("");
    var nextRowID = 0;
        for (var c = 0; c < regimen.length; c++) {
          component = regimen[c].name;

         if(selectedRegComponents == component) {


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
        jq('<input />', { id: 'quantity_'+nextRowID, name: 'quantity', placeholder: 'quantity',
            type: 'number',class: 'quantity' }),
        jq('quantity_'+nextRowID).each(function () {
            jq(this).rules("add", {
                required: true
            });
        }),

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
         var   selectedRegComponents = jq(".regComponents option:selected").val();
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

        jq(function() {
            jq('#cancelButton').click(function () {
                jq('#share').html("");
                jq("#ddlRegimen").val("Select regimen");
                jq("#saveButton").hide();
                jq("#cancelButton").hide();

            })
        });
</script>
</body>
