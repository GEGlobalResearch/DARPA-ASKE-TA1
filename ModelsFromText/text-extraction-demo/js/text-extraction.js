var serviceBaseURL = "http://localhost:4200/darpa/aske/"

$(document).ready(function(){
    //let's call the service and display the HTML to begin with
    $("#spinner").hide();
    $("#viz-card").hide();
    $("#alert").hide();
    extractButtonClick();
});


function extractButtonClick(){
    $("button").click(function (e) {
        $("#spinner").show();

        var docId = this.id
        var isTextAreaEmpty = false;

        if (docId == "user-text") {
            str = $("#text-area").val();
            if(str.trim().length == 0){
                $("#alert").show();
                $("#spinner").hide();
                isTextAreaEmpty = true;
            }
        }

        if(isTextAreaEmpty == false){
            makeExtractionRequest(docId);
        }
	});
}


function makeExtractionRequest(docId){

    $("#alert").hide();

    var extractionURL = serviceBaseURL + "processExampleDoc";
    var json = {"docId": docId};

   if (docId == "user-text") {
       str = $("#text-area").val(); 
       extractionURL = serviceBaseURL + "text2triples";
       json = {"text": str, "locality": "string"};
   }

    var jsonString = JSON.stringify(json);

    $.ajax({
        headers: { 'Accept': 'application/json', 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' },
        type: "POST",
        url: extractionURL,
        data: jsonString,
        crossDomain: true,
        dataType: "json"
    }).done(function(results){

        $("#result-text").empty();
        $("#cy").remove();

        //class="btn btn-primary"
        var baseCollapseStart = '<a data-toggle="collapse" href="#collapseExample" role="button" aria-expanded="false" aria-controls="collapseExample">'
        var baseHiddenHTML = '<div class="collapse" id="collapseExample"> <div class="card card-body">DARPA AKSE TA 1</div></div>'
        var spanEnd = '</a></span> '
        
        triplesAll = []

        for (idx = 0; idx < results.length; idx++){
            result = results[idx];
            //htmlString = "<p>" + result.text + "</p>";
            //$("#result-panel").append(htmlString);

            concepts = result.concepts;
            
            var add = 0;

            text = result.text;
            replaceId = "collapseExample";

            var hiddenHTMLArr = [];

            for (cIdx = 0; cIdx < concepts.length; cIdx++){
                concept = concepts[cIdx];
                var string = concept.string;
                var start = concept.start + add;
                var end = concept.end + add;
                var type = concept.type;
                var contextString = "";

                if (type == "EQUATION"){
                    var triples = concept.triples;
                    triplesAll.push(triples);
                    console.log(triples);
                }
                else if (type == "CONCEPT"){
                    contextString = getConceptContextString(concept.triples);
                }
                

                var replaceWith = "collapse_" + (idx + 1) + "_" + (add + 1);
                
                var collapseStart = baseCollapseStart.replace(/collapseExample/g, replaceWith)
                var hiddenHTML = baseHiddenHTML.replace(replaceId, replaceWith)
                hiddenHTML = hiddenHTML.replace("DARPA AKSE TA 1", contextString);
                var spanStart = '<span class="text-success">' + collapseStart

                hiddenHTMLArr.push(hiddenHTML);

                var subStringPre = text.substring(0, start) + spanStart;
                var subStringPost = spanEnd + text.substring(end)
                
                text = subStringPre + concept.string + subStringPost;
                
                add = add + spanStart.length + spanEnd.length;

                //<span class="text-success">
                //find the sub-string 
            }
            
            htmlString = "<p>" + text;

            for(index = 0; index < hiddenHTMLArr.length; index++){
                htmlString = htmlString + " " + hiddenHTMLArr[index];
            }
            htmlString = htmlString + "</p>"
            //htmlString = "<p>" + text + " " + hiddenHTML + "</p>";
            //console.log(htmlString)
            $("#viz-card").show();
            //$("#result-panel").append(htmlString);
            //$("#result-panel").append('<div id="viz-col"></div>')
            
            $("#result-text").append(htmlString);
            
            $("#viz-col").html('<div id="cy"></div>');
            viz(triplesAll);
            $("#spinner").hide();
        }

    });
}

function getConceptContextString(conceptTriples){
    wikidata_uri = "";
    wikidata_label = "";

    for (tidx = 0; tidx < conceptTriples.length; tidx++){
        ctriple = conceptTriples[tidx];

        subj = ctriple.subject;
        obj = ctriple.object;
        predicate = ctriple.predicate;
        
        if(subj.includes("match_")){
            wikidata_uri = obj;
        }
        else if (subj.includes(wikidata_uri) && predicate.includes("rdf-schema#label")){
            wikidata_label = obj;
        }
    }

    wikidata_uri = wikidata_uri.replace("<", "").replace(">", "");
    contextString = 'Matching Wikidata entity: <a href="' + wikidata_uri + '" target="_blank">' + wikidata_label + '</a>';
    return contextString;
}

function stringMod(str){
    str = str.replace("<", "");
    str = str.replace(">", "");
    str = str.replace("http://www.wikidata.org/entity/", "");
    str = str.replace("http://sadl.org/sadlimplicitmodel#", "");
    return str;
}


function viz(triplesAll){

    //console.log(triples);

    nodeArr = []
    edgeArr = []

    data_args = []
    data_aug_uri = []
    aug_sem_type = []

    for (mIdx = 0; mIdx < triplesAll.length; mIdx++) {
        triples = triplesAll[mIdx];
        eqSubjNode = "";
        for (tIdx = 0; tIdx < triples.length; tIdx++) {

            triple = triples[tIdx];

            subj = stringMod(triple.subject);
            obj = stringMod(triple.object);
            predicate = triple.predicate;

            if(subj.includes(":eq_")){
                eqSubjNode = subj;
            }
            else if(subj.includes("script_text") && predicate.includes("script")){
                nodeSubj = { data: { id: eqSubjNode, type: "equation" } };
                nodeObj = { data: { id: obj } };
                nodeArr.push(nodeSubj);
                nodeArr.push(nodeObj);

                edge = { data: { id: subj + obj, source: eqSubjNode , target: obj, label: "expression" } };
                edgeArr.push(edge);
            }
            else if(subj.includes("data_") && predicate.includes("descriptorName")){
                nodeSubj = { data: { id: eqSubjNode } };
                nodeObj = { data: { id: obj } };
                nodeArr.push(nodeSubj);
                nodeArr.push(nodeObj);

                edge = { data: { id: subj + obj, source: eqSubjNode , target: obj, label: "hasArgument" } };
                edgeArr.push(edge);

                data_args.push({"data" :subj, "arg": obj});
            }
            else if(subj.includes("return_") && predicate.includes("descriptorName")){
                nodeSubj = { data: { id: eqSubjNode } };
                nodeObj = { data: { id: obj } };
                nodeArr.push(nodeSubj);
                nodeArr.push(nodeObj);

                edge = { data: { id: subj + obj, source: eqSubjNode , target: obj, label: "hasReturnVar" } };
                edgeArr.push(edge);

                data_args.push({"data" :subj, "arg": obj});
            }
            else if( subj.includes("data_") && predicate.includes("augmentedType") ) {
                data_aug_uri.push({"data": subj, "aug": obj});
            }
            else if (subj.includes("return_") && predicate.includes("augmentedType")) {
                data_aug_uri.push({"data": subj, "aug": obj});
            }
            else if(subj.includes("aug_sem_type") && predicate.includes("semType")){
                aug_sem_type.push({"aug": subj, "uri": obj});
            }
        }
    }

    for (daIdx = 0; daIdx < data_args.length; daIdx++){
        var dataArg = data_args[daIdx];

        for (daugIdx = 0; daugIdx < data_aug_uri.length; daugIdx++){
            var dataAug = data_aug_uri[daugIdx];

            for (augSemIdx = 0; augSemIdx < aug_sem_type.length; augSemIdx++) {
                var augSem = aug_sem_type[augSemIdx];

                if(dataArg["data"] == dataAug["data"] && dataAug["aug"] == augSem["aug"]){
                    var source = { data: { id: dataArg["arg"] } };
                    var dest = { data: { id: stringMod(augSem["uri"]) } };
                    
                    nodeArr.push(source);
                    nodeArr.push(dest);

                    var edge = { data: { id: dataArg["arg"] + stringMod(augSem["uri"]), source: dataArg["arg"] , target: stringMod(augSem["uri"]), label: "hasType" } };
                    edgeArr.push(edge);
                }
            }
        }
    }

   /*  for (mIdx = 0; mIdx < triplesAll.length; mIdx++) {
        triples = triplesAll[mIdx];
        for (tIdx = 0; tIdx < triples.length; tIdx++){
            triple = triples[tIdx];

            subj = stringMod(triple.subject);
            obj = stringMod(triple.object);

            //console.log(subj + "\t" + obj);

            nodeSubj = { data: { id: subj } };
            nodeObj = { data: { id: obj } };
            nodeArr.push(nodeSubj);
            nodeArr.push(nodeObj);

            edge = { data: { id: subj + obj, source: subj , target: obj } };
            edgeArr.push(edge);
        }
    } */

    var cy = window.cy = cytoscape ({
        container: document.getElementById('cy'),

        style: [
            {
                selector: 'node',
                style: {
                    'content': 'data(id)'
                }
            },

            {
                selector: 'node[type="equation"]',
                style: {
                    'content': 'data(id)',
                    'shape': 'square',
                    'background-color': 'blue'
                }
            },

            {
                selector: 'edge',
                style: {
                    'curve-style': 'bezier',
                    'target-arrow-shape': 'triangle',
                    'label': 'data(label)'
                }
            }
        ],

        elements: {
            nodes: nodeArr,
            edges: edgeArr
        },

        layout: {
            name: 'grid'
        }
    });
}