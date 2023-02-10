function showActivePageOnManifestNavigation(labelText) {
    jq('.ke-label').filter(function(){ return jq(this).text() == labelText; }).css('color','steelblue');
    jq('.ke-label').filter(function(){ return jq(this).text() == labelText; }).parent().css('background-color','lightgray');
}