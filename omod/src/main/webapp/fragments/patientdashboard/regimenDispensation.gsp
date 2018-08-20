<%
ui.decorateWith("kenyaui", "panel", [ heading: "Ordering set" ])

ui.includeJavascript("kenyaemr", "controllers/account.js")

def defaultWhich = config.defaultWhich ?: "current"
def id = config.id ?: ui.generateId();
def frequencies = [
        OD: "Once daily",
        NOCTE: "Once daily, at bedtime",
        qPM: "Once daily, in the evening",
        qAM: "Once daily, in the morning",
        BD: "Twice daily",
        TDS: "Thrice daily"
]
def drugs = [
        {
              'TDF' : {
                drugCode:"TDF",
                dose:"300",
                units:"mg",
                frequency:"OD"

                },

                '3TC' : {
                drugCode:"TDF",
                dose:"300",
                units:"mg",
                frequency:"BD"
        }
        }

]
def units = [ "mg", "g", "ml", "tab" ]
def frequencyOptions = frequencies.collect( { """<option value="${ it.key }">${ it.value }</option>""" } ).join()
def unitsOptions = units.collect( { """<option value="${ it }">${ it }</option>""" } ).join()


%>
<form id="${ id } ng-controller="DispenseRegimenForm" ng-init="init('${ defaultWhich }')">
        <label  class="ke-field-label">Which regimen</label>

	<span class="ke-field-content">
		<p><input type="radio" ng-model="which" ng-change="updateRegimen()" value="current" /> Current Regimen </p>

        <p><input type="radio" ng-model="which" ng-change="updateRegimen()" value="others" /> Others </>




</span>
        <div id="fr3030-container">
        <div class="regimen-component">
        Dosage: <input class="regimen-component-dose" type="text" size="5"/><select class="regimen-component-units">${ unitsOptions }</select>
        </div>
        <div class="regimen-component">
                Frequency: <select class="regimen-component-frequency">${ frequencyOptions }</select>
        </div>
        </div>


</form>