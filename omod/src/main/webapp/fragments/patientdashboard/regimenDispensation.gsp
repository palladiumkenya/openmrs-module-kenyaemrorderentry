<%
ui.decorateWith("kenyaui", "panel", [ heading: "Ordering set" ])

ui.includeJavascript("orderentryui", "regimenDispensation.js")


%>


<body>
        <input type="button" id="btnGenerate" value="Populate DropDownList" onclick="PopulateDropDownList()" />
        <hr />
        Regimen: <select id="ddlRegimen" class="regComponents" onchange="PopulateComponents()>
        </select>
        <br />
        <div>
Drug: <input id="drug" name="drug" type="hidden"/>
// Dose: <input id="dose" name="dose" type="text"/>
       </div>
       <div class="box-body" id="share">
       </div>


    <script type="text/javascript">
function PopulateDropDownList() {

    //Build an array containing e regimen records.
     regimen = [{
                       "name":"TDF + 3TC + NVP (300mg OD/150mg BD/200mg BD)",
                       "components": [
                {
                    "name":"TDF",
                    "dose":"300",
                    "units":"mg",
                    "frequency":"OD"
                },
                {
                    "name":"3TC",
                    "dose":"150",
                    "units":"mg",
                    "frequency":"BD"
                },
                {
                    "name":"NVP",
                    "dose":"200",
                    "units":"mg",
                    "frequency":"BD"
                }
                   ]

                   },
                   {
                       "name":"TDF + 3TC(BD) + EFV (300mg OD/150mg BD/600mg OD)",
                       "components": [
                           {
                               "name":"TDF",
                               "dose":"300",
                               "units":"mg",
                               "frequency":"OD"
                           },
                           {
                               "name":"3TC",
                               "dose":"150",
                               "units":"mg",
                               "frequency":"BD"
                           },
                           {
                               "name":"EFV",
                               "dose":"600",
                               "units":"mg",
                               "frequency":"OD"
                           }
                   ]

                   }]

    var ddlRegimen = jq("#ddlRegimen");
    jq(regimen).each(function () {
        var option = jq("<option />");

        option.html(this.name);
        option.val(this.name);
        ddlRegimen.append(option);
    });
}

jq(document).ready(function(){
    items = { 'OD' : 'Once daily', 'NOCTE' : 'Once daily, at bedtime',
    'qPM' : 'Once daily, in the evening', 'qAM' : 'Once daily, in the morning',
'BD' : 'Twice daily','TDS' : 'Thrice daily'};

jq("select.regComponents").change(function(){

        selectedRegComponents = jq(".regComponents option:selected").val();

        jq('#share').html("");
         console.log("this is selected value: " + selectedRegComponents);

        for (var c = 0; c < regimen.length; c++) {
          component = regimen[c].name;

         if(selectedRegComponents == component) {


            jq.grep(regimen[c].components, function(item){


    jq(function() {
    jq('#share').append(
    jq('<body />', { method: 'POST' }).append(
    jq('<label />', { class: 'appm', text: 'Drugs:' }),
    jq('<input />', { id: 'drugs', name: 'drugs', placeholder: 'Name', type: 'text',readonly:'readonly' }).val ( item.name ),
    jq('<label />', { class: 'appm', text: 'Doses:' }),
    jq('<input />', { id: 'doses', name: 'doses', placeholder: 'doses', type: 'text' }).val ( item.dose ),
    jq('<label />', { class: 'appm', text: 'Frequency:' }),

ddlFrequency =jq('<select />', { id: 'frequency', name: 'frequency', placeholder: 'frequency', type: 'text' }),
jq.each(items, function(text, key) {
 option = new Option(key, text);
if(text == item.frequency) {
 option.setAttribute('selected', true) ;

}
ddlFrequency.append(jq(option));

}),
    jq('<br />')
    )
    )
    })

 console.log("items - " + item.name + item.dose + item.frequency);
           });

          }

        }
    });

});
</script>
</body>




