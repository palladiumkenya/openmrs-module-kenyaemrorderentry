<%
ui.decorateWith("kenyaui", "panel", [ heading: "Ordering set" ])

ui.includeJavascript("orderentryui", "regimenDispensation.js")


%>


<body>
        <input type="button" id="btnGenerate" value="Populate DropDownList" onclick="populateDropDownList()" />
        <hr />
        Regimen: <select id="ddlRegimen" class="regComponents">
        </select>
        <br />
        <div>

       </div>
       <div class="box-body" id="share">


       </div>
<div id="saveButton">
    <button><img src="${ ui.resourceLink("kenyaui", "images/glyphs/ok.png") }" /> Save</button>
</div>



    <script type="text/javascript">
function populateDropDownList() {

    //Build an array containing e regimen records.
     regimen = [{
                       "name":"TDF + 3TC + NVP (300mg OD/150mg BD/200mg BD)",
                       "components": [
                {
                    "name":"TDF",
                    "dose":"300",
                    "drug_concept_id":"1",
                    "units":"mg",
                    "frequency":"5"
                },
                {
                    "name":"3TC",
                    "dose":"150",
                    "drug_concept_id":"2",
                    "units":"mg",
                    "frequency":"3"
                },
                {
                    "name":"NVP",
                    "dose":"200",
                    "drug_concept_id":"4",
                    "units":"mg",
                    "frequency":"3"
                }
                   ]

                   },
                   {
                       "name":"TDF + 3TC(BD) + EFV (300mg OD/150mg BD/600mg OD)",
                       "components": [
                           {
                               "name":"TDF",
                               "drug_concept_id":"1",
                               "dose":"300",
                               "units":"mg",
                               "frequency":"5"
                           },
                           {
                               "name":"3TC",
                               "drug_concept_id":"2",
                               "dose":"150",
                               "units":"mg",
                               "frequency":"3"
                           },
                           {
                               "name":"EFV",
                               "drug_concept_id":"3",
                               "dose":"600",
                               "units":"mg",
                               "frequency":"5"
                           }
                   ]

                   }];
     regimen.unshift({"name": "Select regimen"});

    var ddlRegimen = jq("#ddlRegimen");
    jq(regimen).each(function () {
        var option = jq("<option />");

        option.html(this.name);
        option.val(this.name);
        ddlRegimen.append(option);
    });
}

jq(document).ready(function(){
    items = { '5' : 'Once daily', '4' : 'Once daily, at bedtime',
    '1' : 'Once daily, in the evening', '2' : 'Once daily, in the morning',
'3' : 'Twice daily','TDS' : 'Thrice daily'};

jq("select.regComponents").change(function(){

        selectedRegComponents = jq(".regComponents option:selected").val();

        jq('#share').html("");
         console.log("this is selected value: " + selectedRegComponents);
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
    jq('<label />', { class: 'appm', text: 'Doses:' }),
    jq('<input />', { id: 'doses_'+nextRowID, name: 'doses', placeholder: 'doses', type: 'text' }).val ( item.dose ),
    jq('<label />', { class: 'appm', text: 'Frequency:' }),

    ddlFrequency =jq('<select />', { id: 'frequency_'+nextRowID, name: 'frequency', placeholder: 'frequency', type: 'text' }),
    jq.each(items, function(text, key) {
     option = new Option(key, text);
    if(text == item.frequency) {
     option.setAttribute('selected', true) ;

    }
    ddlFrequency.append(jq(option));

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
var payload = [];


jq(function() {
    jq('#saveButton').click(function() {
        payload = [];
        var rowID = 0;
         var   selectedRegComponents = jq(".regComponents option:selected").val();
         var obj;
            for (var c = 0; c < regimen.length; c++) {
               var component = regimen[c].name;

                if (selectedRegComponents == component) {
                    jq.grep(regimen[c].components, function (item) {

                            rowID = rowID + 1;

                            doses = jq("#doses_"+rowID).val();
                            drugs = item.drug_concept_id;
                            console.log('item concept'+ item.drug_concept_id);
                            frequency = jq("#frequency_"+rowID).val();
                            obj = {
                                "frequency" :frequency,
                                "drug" :drugs,
                                "dose" :doses


                            };

                        payload.push(obj);
                        console.log('payload=======', payload);

                    });


                }
            }




    });
});
</script>
</body>




