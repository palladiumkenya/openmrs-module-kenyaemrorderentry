KenyaEMR Distribution - OrderEntry Module
=====================================
<a href="http://ci.kenyaemr.org/viewType.html?buildTypeId=bt2"><img src="http://ci.kenyaemr.org/app/rest/builds/buildType:bt2/statusIcon"/></a>

Overview
--------
Initial I-TECH work on an OpenMRS-based EMR for the Kenya MoH.

Project homepage: https://sites.google.com/site/kenyahealthinformatics/

Developer documentation: https://wiki.openmrs.org/display/projects/KenyaEMR+Distribution

Installation
------------
To create a distribution zip of all required modules, build as follows:

	mvn clean package -DbuildDistro

This can then be extracted into your OpenMRS modules repository directory, e.g.

	MODULE_DIR=/usr/share/tomcat6/.OpenMRS/modules/
	rm $MODULE_DIR/*.omod
	unzip -oj distro/target/kenyaemr-13.3-distro.zip -d $MODULE_DIR

Global Params
-------------
### Set the system type
Setting Name:						Sample:			types:
kemrorder.labsystem_identifier		LABWARE			LABWARE/CHAI

LABWARE LAB SYSTEM
Setting Name:						Sample:			
chai_vl_server_url					http://41.89.93.242/api/vl_complete
chai_vl_server_result_url			http://41.89.93.242/api/function
chai_vl_server_api_token			123456
chai_eid_server_url					http://41.89.93.242/api/eid_complete
chai_eid_server_result_url			http://41.89.93.242/api/function
chai_eid_server_api_token			123456

CHAI LAB SYSTEM
Setting Name:						Sample:			
labware_vl_server_url				https://covid2.nphl.go.ke/api/emr-exchange
labware_vl_server_result_url		https://covid2.nphl.go.ke/api/emr-exchange
labware_vl_server_api_token			123456
labware_eid_server_url				https://nhrl.nphl.go.ke/api/eid-exchange
labware_eid_server_result_url		https://nhrl.nphl.go.ke/api/eid-exchange
labware_eid_server_api_token		123456

Accreditation
-------------
* Highcharts graphing library by Highsoft used under Creative Commons Licence 3.0 (http://www.highcharts.com/)
* Pretty Office Icons used with permission from CustomIconDesign (http://www.customicondesign.com)