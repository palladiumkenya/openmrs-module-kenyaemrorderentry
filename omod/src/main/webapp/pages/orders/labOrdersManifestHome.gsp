<%
	ui.decorateWith("kenyaemr", "standardPage", [ layout: "sidebar" ])

	def menuItems = [
			[ label: "Back", iconProvider: "kenyaui", icon: "buttons/back.png", label: "Back", href: ui.pageLink("kenyaemrorderentry", "orders/labOrderHome") ]
	]
%>
<style>
div.column-order-btns {
	width: 100px;
}
div.column-one {
	width: 180px;
}

div.column-two {
	width: 80px;
}

div.column-three {
	width: 200px;
}

div.column-four {
	width: 120px;
}
</style>

<div class="ke-page-sidebar">
	${ ui.includeFragment("kenyaui", "widget/panelMenu", [ heading: "Back", items: menuItems ]) }
</div>

<div class="ke-page-content">
	<div align="center">

		<button type="button" class="pushLabOrders">
			<img src="${ui.resourceLink("kenyaui", "images/glyphs/report_download_excel.png")}"
				 style="display:none;"/>Push Lab Orders
		</button>
		<button type="button" class="pullLabOrders">
			<img src="${ui.resourceLink("kenyaui", "images/glyphs/report_download_excel.png")}"
				 style="display:none;"/>Pull Lab results
		</button>

	</div>

</div>

<script type="text/javascript">

    //On ready
    jQuery(function () {

    });

</script>
