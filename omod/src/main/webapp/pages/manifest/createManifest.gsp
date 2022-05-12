<%
	ui.decorateWith("kenyaemr", "standardPage")
%>
<div class="ke-page-content">
	${ ui.includeFragment("kenyaemrorderentry", "manifest/manifestForm", [ manifestId: manifest != null ? manifest.id : null, returnUrl: ui.pageLink("kenyaemrorderentry", "orders/labOrdersManifestHome") ]) }
</div>