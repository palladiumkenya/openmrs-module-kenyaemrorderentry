/*
 Highcharts JS v11.1.0 (2023-06-28)

 (c) 2010-2021 Highsoft AS
 Author: Sebastian Domas

 License: www.highcharts.com/license
*/
'use strict';(function(a){"object"===typeof module&&module.exports?(a["default"]=a,module.exports=a):"function"===typeof define&&define.amd?define("highcharts/modules/histogram-bellcurve",["highcharts"],function(e){a(e);a.Highcharts=e;return a}):a("undefined"!==typeof Highcharts?Highcharts:void 0)})(function(a){function e(a,c,m,n){a.hasOwnProperty(c)||(a[c]=n.apply(null,m),"function"===typeof CustomEvent&&window.dispatchEvent(new CustomEvent("HighchartsModuleLoaded",{detail:{path:c,module:a[c]}})))}
a=a?a._modules:{};e(a,"Series/DerivedComposition.js",[a["Core/Globals.js"],a["Core/Series/Series.js"],a["Core/Utilities.js"]],function(a,c,m){const {noop:n}=a,{addEvent:k,defined:e}=m;var p;(function(a){function b(){c.prototype.init.apply(this,arguments);this.initialised=!1;this.baseSeries=null;this.eventRemovers=[];this.addEvents()}function q(){const a=this.chart,t=this.options.baseSeries;this.baseSeries=e(t)&&(a.series[t]||a.get(t))||null}function d(){this.eventRemovers.push(k(this.chart,"afterLinkSeries",
()=>{this.setBaseSeries();this.baseSeries&&!this.initialised&&(this.setDerivedData(),this.addBaseSeriesEvents(),this.initialised=!0)}))}function r(){this.eventRemovers.push(k(this.baseSeries,"updatedData",()=>{this.setDerivedData()}),k(this.baseSeries,"destroy",()=>{this.baseSeries=null;this.initialised=!1}))}function h(){this.eventRemovers.forEach(a=>{a()});c.prototype.destroy.apply(this,arguments)}const g=[];a.hasDerivedData=!0;a.setDerivedData=n;a.compose=function(a){if(m.pushUnique(g,a)){const f=
a.prototype;f.addBaseSeriesEvents=r;f.addEvents=d;f.destroy=h;f.init=b;f.setBaseSeries=q}return a};a.init=b;a.setBaseSeries=q;a.addEvents=d;a.addBaseSeriesEvents=r;a.destroy=h})(p||(p={}));return p});e(a,"Series/Histogram/HistogramSeries.js",[a["Series/DerivedComposition.js"],a["Core/Series/SeriesRegistry.js"],a["Core/Utilities.js"]],function(a,c,e){function n(a){return function(f){let b=1;for(;a[b]<=f;)b++;return a[--b]}}const {seriesTypes:{column:k}}=c,{arrayMax:m,arrayMin:p,correctFloat:d,extend:b,
isNumber:q,merge:l,objectEach:r}=e,h={"square-root":function(a){return Math.ceil(Math.sqrt(a.options.data.length))},sturges:function(a){return Math.ceil(Math.log(a.options.data.length)*Math.LOG2E)},rice:function(a){return Math.ceil(2*Math.pow(a.options.data.length,1/3))}};class g extends k{constructor(){super(...arguments);this.userOptions=this.points=this.options=this.data=void 0}binsNumber(){const a=this.options.binsNumber,b=h[a]||"function"===typeof a&&a;return Math.ceil(b&&b(this.baseSeries)||
(q(a)?a:h["square-root"](this.baseSeries)))}derivedData(a,b,l){let f=d(m(a)),c=d(p(a)),h=[],e={},g=[],k;l=this.binWidth=d(q(l)?l||1:(f-c)/b);this.options.pointRange=Math.max(l,0);for(b=c;b<f&&(this.userOptions.binWidth||d(f-b)>=l||0>=d(d(c+h.length*l)-b));b=d(b+l))h.push(b),e[b]=0;0!==e[c]&&(h.push(c),e[c]=0);k=n(h.map(function(a){return parseFloat(a)}));a.forEach(function(a){a=d(k(a));e[a]++});r(e,function(a,b){g.push({x:Number(b),y:a,x2:d(Number(b)+l)})});g.sort(function(a,b){return a.x-b.x});g[g.length-
1].x2=f;return g}setDerivedData(){var a=this.baseSeries.yData;a.length?(a=this.derivedData(a,this.binsNumber(),this.options.binWidth),this.setData(a,!1)):this.setData([])}}g.defaultOptions=l(k.defaultOptions,{binsNumber:"square-root",binWidth:void 0,pointPadding:0,groupPadding:0,grouping:!1,pointPlacement:"between",tooltip:{headerFormat:"",pointFormat:'<span style="font-size: 0.8em">{point.x} - {point.x2}</span><br/><span style="color:{point.color}">\u25cf</span> {series.name} <b>{point.y}</b><br/>'}});
b(g.prototype,{hasDerivedData:a.hasDerivedData});a.compose(g);c.registerSeriesType("histogram",g);"";return g});e(a,"Series/Bellcurve/BellcurveSeries.js",[a["Series/DerivedComposition.js"],a["Core/Series/SeriesRegistry.js"],a["Core/Utilities.js"]],function(a,c,e){const {seriesTypes:{areaspline:n}}=c,{correctFloat:k,isNumber:m,merge:p}=e;class d extends n{constructor(){super(...arguments);this.points=this.options=this.data=void 0}static mean(a){const b=a.length;a=a.reduce(function(a,b){return a+b},
0);return 0<b&&a/b}static standardDeviation(a,c){let b=a.length;c=m(c)?c:d.mean(a);a=a.reduce(function(a,b){b-=c;return a+b*b},0);return 1<b&&Math.sqrt(a/(b-1))}static normalDensity(a,c,d){a-=c;return Math.exp(-(a*a)/(2*d*d))/(d*Math.sqrt(2*Math.PI))}derivedData(a,c){var b=this.options.intervals,e=this.options.pointsInInterval;let h=a-b*c;b=b*e*2+1;e=c/e;let g=[],f;for(f=0;f<b;f++)g.push([h,d.normalDensity(h,a,c)]),h+=e;return g}setDerivedData(){1<this.baseSeries.yData.length&&(this.setMean(),this.setStandardDeviation(),
this.setData(this.derivedData(this.mean,this.standardDeviation),!1))}setMean(){this.mean=k(d.mean(this.baseSeries.yData))}setStandardDeviation(){this.standardDeviation=k(d.standardDeviation(this.baseSeries.yData,this.mean))}}d.defaultOptions=p(n.defaultOptions,{intervals:3,pointsInInterval:3,marker:{enabled:!1}});a.compose(d);c.registerSeriesType("bellcurve",d);"";return d});e(a,"masters/modules/histogram-bellcurve.src.js",[],function(){})});
//# sourceMappingURL=histogram-bellcurve.js.map