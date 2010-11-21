jws.RPCClientPlugIn={grantedProcs:[],spawnThreadDefault:false,NS:jws.NS_BASE+".plugins.rpc",setSpawnThreadDefault:function(bR){this.spawnThreadDefault=bR;},addGrantedProcedure:function(bw){jws.RPCClientPlugIn.grantedProcs[jws.RPCClientPlugIn.grantedProcs.length]=bw;},removeGrantedProcedure:function(bw){var g=jws.RPCClientPlugIn.grantedProcs.indexOf(bw);if(g>=0){jws.RPCClientPlugIn.grantedProcs.splice(g,1);}},processToken:function(aR){if(aR.ns==jws.RPCClientPlugIn.NS){if(aR.type=="rrpc"){this.onRRPC(aR);}}},rpc:function(K,aMthd,bl,d){if(bl!=null&& !(bl instanceof Array)){bl=[bl];}d=this.setDefaultOption(d);var bj=this.createDefaultResult();if(this.isConnected()){this.sendToken({ns:jws.RPCClientPlugIn.NS,type:"rpc",classname:K,method:aMthd,args:bl},d);}else{bj.code= -1;bj.localeKey="jws.jsc.res.notConnected";bj.msg="Not connected.";}return bj;},setDefaultOption:function(d){if(d===undefined){d={};}if(d.spawnThread===undefined){d.spawnThread=this.spawnThreadDefault;}return d;},rrpc:function(bb,K,aMthd,bl,d){if(bl!=null&& !(bl instanceof Array)){bl=[bl];}d=this.setDefaultOption(d);var bj=this.createDefaultResult();if(this.isConnected()){this.sendToken({ns:jws.RPCClientPlugIn.NS,type:"rrpc",targetId:bb,classname:K,method:aMthd,args:bl},d);}else{bj.code= -1;bj.localeKey="jws.jsc.res.notConnected";bj.msg="Not connected.";}return bj;},onRRPC:function(aR){var bF=aR.classname;var bu=aR.method;var bC=aR.args;var ad=bF+"."+bu;if(jws.RPCClientPlugIn.grantedProcs.indexOf(ad)>=0){var bA=bF.split('.');var bI=bA.length;var bx=window[bA[0]];for(var j=1;j<bI;j++){bx=bx[bA[j]];}var bj;try{bj=bx[bu].apply(null,bC);}catch(ex){bj=ex+"\nProbably a typo error (method called="+bu+") or wrong number of arguments (args: "+JSON.stringify(bC)+")";}}else{bj= +"\nAcces not granted to the="+bu;}this.sendToken({type:"send",targetId:aR.sourceId,result:bj,reqType:"rrpc",code:0},null);}};jws.oop.addPlugIn(jws.jWebSocketTokenClient,jws.RPCClientPlugIn);