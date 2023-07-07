/*
 Highcharts JS v11.1.0 (2023-06-28)

 Item series type for Highcharts

 (c) 2019 Torstein Honsi

 License: www.highcharts.com/license
*/
'use strict';(function(a){"object"===typeof module&&module.exports?(a["default"]=a,module.exports=a):"function"===typeof define&&define.amd?define("highcharts/modules/item-series",["highcharts"],function(b){a(b);a.Highcharts=b;return a}):a("undefined"!==typeof Highcharts?Highcharts:void 0)})(function(a){function b(a,f,d,b){a.hasOwnProperty(f)||(a[f]=b.apply(null,d),"function"===typeof CustomEvent&&window.dispatchEvent(new CustomEvent("HighchartsModuleLoaded",{detail:{path:f,module:a[f]}})))}a=a?a._modules:
{};b(a,"Series/Item/ItemPoint.js",[a["Core/Series/SeriesRegistry.js"],a["Core/Utilities.js"]],function(a,f){var d=this&&this.__extends||function(){var a=function(b,e){a=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(a,e){a.__proto__=e}||function(a,e){for(var b in e)Object.prototype.hasOwnProperty.call(e,b)&&(a[b]=e[b])};return a(b,e)};return function(b,e){function f(){this.constructor=b}if("function"!==typeof e&&null!==e)throw new TypeError("Class extends value "+String(e)+" is not a constructor or null");
a(b,e);b.prototype=null===e?Object.create(e):(f.prototype=e.prototype,new f)}}(),b=a.series;f=f.extend;a=function(a){function b(){var b=null!==a&&a.apply(this,arguments)||this;b.options=void 0;b.series=void 0;return b}d(b,a);return b}(a.seriesTypes.pie.prototype.pointClass);f(a.prototype,{haloPath:b.prototype.pointClass.prototype.haloPath});return a});b(a,"Series/Item/ItemSeries.js",[a["Core/Globals.js"],a["Series/Item/ItemPoint.js"],a["Core/Defaults.js"],a["Core/Series/SeriesRegistry.js"],a["Core/Utilities.js"]],
function(a,b,d,w,k){var f=this&&this.__extends||function(){var a=function(b,c){a=Object.setPrototypeOf||{__proto__:[]}instanceof Array&&function(a,c){a.__proto__=c}||function(a,c){for(var b in c)Object.prototype.hasOwnProperty.call(c,b)&&(a[b]=c[b])};return a(b,c)};return function(b,c){function e(){this.constructor=b}if("function"!==typeof c&&null!==c)throw new TypeError("Class extends value "+String(c)+" is not a constructor or null");a(b,c);b.prototype=null===c?Object.create(c):(e.prototype=c.prototype,
new e)}}(),e=d.defaultOptions,x=w.seriesTypes.pie,I=k.defined,y=k.extend,K=k.fireEvent,p=k.isNumber,C=k.merge,L=k.pick;d=function(b){function d(){var a=null!==b&&b.apply(this,arguments)||this;a.data=void 0;a.options=void 0;a.points=void 0;return a}f(d,b);d.prototype.animate=function(a){a?this.group.attr({opacity:0}):this.group.animate({opacity:1},this.options.animation)};d.prototype.drawDataLabels=function(){this.center&&this.slots?a.seriesTypes.pie.prototype.drawDataLabels.call(this):this.points.forEach(function(a){a.destroyElements({dataLabel:1})})};
d.prototype.drawPoints=function(){var a=this,b=this.options,d=a.chart.renderer,e=b.marker,f=this.borderWidth%2?.5:1,z=0,r=this.getRows(),k=Math.ceil(this.total/r),t=this.chart.plotWidth/k,u=this.chart.plotHeight/r,v=this.itemSize||Math.min(t,u);this.points.forEach(function(c){var F;var l=c.marker||{};var J=l.symbol||e.symbol,m=L(l.radius,e.radius),H=I(m)?2*m:v,q=H*b.itemPadding,B;c.graphics=l=c.graphics||[];a.chart.styledMode||(F=a.pointAttribs(c,c.selected&&"select"));if(!c.isNull&&c.visible){c.graphic||
(c.graphic=d.g("point").add(a.group));for(var n=0;n<(c.y||0);n++){if(a.center&&a.slots){var h=a.slots.shift();var g=h.x-v/2;h=h.y-v/2}else"horizontal"===b.layout?(g=z%k*t,h=u*Math.floor(z/k)):(g=t*Math.floor(z/r),h=z%r*u);g+=q;h+=q;var p=B=Math.round(H-2*q);a.options.crisp&&(g=Math.round(g)-f,h=Math.round(h)+f);h={x:g,y:h,width:B,height:p};"undefined"!==typeof m&&(h.r=m);F&&y(h,F);(g=l[n])?g.animate(h):g=d.symbol(J,void 0,void 0,void 0,void 0,{backgroundSize:"within"}).attr(h).add(c.graphic);g.isActive=
!0;l[n]=g;z++}}for(c=0;c<l.length;c++){g=l[c];if(!g)break;g.isActive?g.isActive=!1:(g.destroy(),l.splice(c,1),c--)}})};d.prototype.getRows=function(){var a=this.options.rows;if(!a){var b=this.chart.plotWidth/this.chart.plotHeight;a=Math.sqrt(this.total);if(1<b)for(a=Math.ceil(a);0<a;){var d=this.total/a;if(d/a>b)break;a--}else for(a=Math.floor(a);a<this.total;){d=this.total/a;if(d/a<b)break;a++}}return a};d.prototype.getSlots=function(){function a(a){0<E&&(a.row.colCount--,E--)}for(var b=this.center,
d=b[2],e=b[3],f,k=this.slots,r,p,t,u,v,w,D,l,A=0,m,G=this.endAngleRad-this.startAngleRad,q=Number.MAX_VALUE,B,n,h,g=this.options.rows,x=(d-e)/d,y=0===G%(2*Math.PI),C=this.total||0;q>C+(n&&y?n.length:0);)for(B=q,q=k.length=0,n=h,h=[],A++,m=d/A/2,g?(e=(m-g)/m*d,0<=e?m=g:(e=0,x=1)):m=Math.floor(m*x),f=m;0<f;f--)t=(e+f/m*(d-e-A))/2,u=G*t,v=Math.ceil(u/A),h.push({rowRadius:t,rowLength:u,colCount:v}),q+=v+1;if(n){for(var E=B-this.total-(y?n.length:0);0<E;)n.map(function(a){return{angle:a.colCount/a.rowLength,
row:a}}).sort(function(a,b){return b.angle-a.angle}).slice(0,Math.min(E,Math.ceil(n.length/2))).forEach(a);n.forEach(function(a){var c=a.rowRadius;w=(a=a.colCount)?G/a:0;for(l=0;l<=a;l+=1)D=this.startAngleRad+l*w,r=b[0]+Math.cos(D)*c,p=b[1]+Math.sin(D)*c,k.push({x:r,y:p,angle:D})},this);k.sort(function(a,b){return a.angle-b.angle});this.itemSize=A;return k}};d.prototype.translate=function(b){0===this.total&&p(this.options.startAngle)&&p(this.options.endAngle)&&(this.center=this.getCenter());this.slots||
(this.slots=[]);p(this.options.startAngle)&&p(this.options.endAngle)?(a.seriesTypes.pie.prototype.translate.apply(this,arguments),this.slots=this.getSlots()):(this.generatePoints(),K(this,"afterTranslate"))};d.defaultOptions=C(x.defaultOptions,{endAngle:void 0,innerSize:"40%",itemPadding:.1,layout:"vertical",marker:C(e.plotOptions.line.marker,{radius:null}),rows:void 0,crisp:!1,showInLegend:!0,startAngle:void 0});return d}(x);y(d.prototype,{markerAttribs:void 0});d.prototype.pointClass=b;w.registerSeriesType("item",
d);"";return d});b(a,"masters/modules/item-series.src.js",[],function(){})});
//# sourceMappingURL=item-series.js.map