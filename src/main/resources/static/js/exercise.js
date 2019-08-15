function updateParentGoal(goal) {
    parent = $(goal).parent().closest('.js-goal');
    if(parent.length == 1) {
        parent = $(parent[0]);
        total = 0;
        count = 0;
        met = 0;
        children = parent.children('ul').children('li').children('.js-goal');
        children.children('.progress').each(function(index) {
           p = $(this);
           count++;
           completion = parseInt(p.children('.indicator').html().substring(0,p.html().length-1));
           console.log("completion: " + completion);
           total = total + completion;
           if(completion == 100) met++;
        });
        parent.children('.progress').children('.indicator').html(Math.trunc(total/count)+'%');
        parent.children('.progress').children('.reason').html(met + ' out of ' + count + ' goals met');
        parent.children('.progress').attr('data-progress',Math.trunc(total/count)+'%');

        updateParentGoal(parent);
    }
}

function toggleGoalChildren(goal) {
    $(goal).children('.expand-arrow').toggleClass('open-arrow');
    $(goal).children('ul').toggle();
}

function resetGoals(exercise) {
    $(exercise).find('.progress .indicator').html('0%');
    $(exercise).find('.progress .reason').html('code not yet executed');
    $(exercise).find('.progress').attr('data-progress','0%');
}

function populateExercise(exercise, stompClient) {

    // load html for exercise
    exercise.load("/exercises/"+exercise.attr('id')+"/raw", function() {

        // connect all history listeners to websockets for their exercises
        exercise.find('.history').each(function(i) {
            var exerciseId = exercise.attr('id');
            var table = $(this).children('table').DataTable( {
                "order": [[ 0, "desc" ]],
                "ajax": "/exercises/"+exerciseId+"/history",
                "columns": [
                    { "data": "time" },
                    { "data": "completion" },
                    { "data": "snippet" },
                    { "data": "status" },
                    { "data": "result"}
                ]
            } );

            stompClient.subscribe('/user/queue/exercises/'+exerciseId+'/result', function (result) {
                var update = JSON.parse(result.body);
                var date = new Date().toISOString();
                table.row.add({
                    'time': date.slice(0,10) + " " + date.slice(11,19),
                    'completion': ''+(update.completion*100)+'%',
                    'snippet': '<pre>'+update.snippet+'</pre>',
                    'status': ''+update.status,
                    'result': ''+update.message
            }).draw(false);
            });
        });

        // link every execute button to ajax call for their exercises
        exercise.find('.js-sendcode').on('click', function(e) {
            var sendButton = $(this);
            var exerciseId = exercise.attr('id');
            var codepad = sendButton.parent().find('textarea');
            // TODO: clear messages for this exercise
            stompClient.send("/app/exercise/"+exercise.attr('id')+"/exec", {}, codepad.val());
            // TODO: report error on failure to send ajax message
            if(codepad.attr('clearonsend') != undefined) codepad.val('');
            exercise.find('.js-message').val('... evaluating submitted code snippet ...');
            resetGoals(exercise);
        });

        // connect all variable listeners to websockets for their exercises
        exercise.find('.js-variables').each(function(i) {
            var term = $(this);
            var exerciseId = exercise.attr('id');

            stompClient.subscribe('/user/queue/exercises/'+exerciseId+'/variables', function (varmap) {
                var variables = JSON.parse(varmap.body);
                console.log(variables);
                value = "";
                Object.keys(variables).sort().forEach(function(key) {
                    value += (key + " = " + variables[key]) + "\n";
                });
                term.val(value); // print message to terminal
                // TODO: include type
                // TODO: populate variable table
            });
        });

        // connect all message listeners to websockets for their exercises
        exercise.find('.js-message').each(function(i) {
            var box = $(this);
            var exerciseId = exercise.attr('id');

            stompClient.subscribe('/user/queue/exercises/'+exerciseId+'/result', function (result) {
                message = JSON.parse(result.body);
                box.val('submitted snippet result: ' + message.status +
                    (message.message.length > 0 ? ' "' + message.message + '"' : ''));
            });
        });

        // connect all output listeners to websockets for their exercises
        exercise.find('.js-stdout').each(function(i) {
            var term = $(this);
            var exerciseId = exercise.attr('id');
            stompClient.subscribe('/user/queue/exercises/'+exerciseId+'/stdout', function (message) {
                term.append(message.body); // print result to terminal
                term.scrollTop(term[0].scrollHeight - term.height());  // scroll to bottom
                // TODO: prevent newline after every call
            });
        });

        // connect all output listeners to goals for their exercises
        exercise.find('.js-goal').each(function(i) {
            var goal = $(this);
            var goalId = goal.attr('id');
            var exerciseId = exercise.attr('id');

            stompClient.subscribe('/user/queue/exercises/'+exerciseId+'/goals/'+goalId, function (message) {
                var pmessage = JSON.parse(message.body);
                var progress = (100*pmessage['completion'])+'%';
                var message = pmessage['message'];
                var oldProgress = $(goal.find('span.progress')).html();
                $(goal.find('span.indicator')).html(progress);
                $(goal.find('span.progress')).attr('data-progress', progress);
                $(goal.find('span.reason')).html(message);
                if(oldProgress != progress)
                    updateParentGoal(goal);
            });
        });

        // connect all output listeners to goals for their exercises
        exercise.find('.js-methods').each(function(i) {
            var glist = $(this);
            var exerciseId = exercise.attr('id');
            stompClient.subscribe('/user/queue/exercises/'+exerciseId+'/methods', function (message) {
                JSON.parse(message.body).forEach(function (method) {
                        var name = method[0] + ': ' + method[1];
                        var option = glist.find('option').filter(
                                function (i,option) {return option.text == name; }
                            )[0];
                        if(option == null) {
                            glist.append($('<option>', {
                                value: method[2],
                                text: name
                            }));
                        } else {
                            option.value = method[2];
                        }
                });
            });
        });

        // callback to populate codepad with method definitions
        exercise.find('.js-loadmethod').each(function(i) {
            var loadMethod = $(this);
            var glist = exercise.find('.js-methods');
            var term = exercise.find('.js-codepad textarea');
            loadMethod.on('click', function (ev) {
                term.val(glist.val());
            });
        });

        // link every input field to send message to server
        exercise.find('.js-stdin').keyup(function(e) {
            var inputfield = $(this);
            var exerciseId = exercise.attr('id');

            if(e.keyCode == 13) {  // return was pressed
                stompClient.send("/app/exercise/"+exercise.attr('id')+"/stdin", {}, inputfield.val());
                // TODO: send inputfield.val()
                inputfield.val('');                     // clear input
            }
            // TODO: report error on failure to send ajax message
        });



        // link every reset button to ajax call for their exercises
        exercise.find('.js-sendreset').on('click', function(e) {
            var sendButton = $(this);
            var exerciseId = exercise.attr('id');
            stompClient.send("/app/exercise/"+exerciseId+"/reset", {}, "reset");
            var codepad = sendButton.parent().find('textarea');
            codepad.val('');
            var variables = exercise.find('js-variables');
            variables.val('');
            var history = exercise.find('js-history');
            history.val('');
            var methods = exercise.find('.js-methods');
            $(methods).children().remove();
            // TODO: report error on failure to send ajax message
        });
    });
};


$( document ).ready(function() {
    var socket = new SockJS('/codecafe-websocket');
    var stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        $('.js-exercise').each(function(i) {
            populateExercise($(this), stompClient);
        })
    });
});