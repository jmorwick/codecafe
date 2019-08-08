function populateExercise(exercise, stompClient) {

    // load html for exercise
    exercise.load("/exercises/"+exercise.attr('id')+"/raw", function() {

        // connect all history listeners to websockets for their exercises
        exercise.find('.js-history').each(function(i) {
            var exerciseId = exercise.attr('id');
            stompClient.subscribe('/user/queue/exercises/'+exerciseId+'/result', function (result) {
                var message = exercise.find('.js-message');
                message.val('');  // clear last error message
                var hist = exercise.find('.js-history');
                hist.append("\n"+result.body); // print result to terminal
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
            var term = $(this);
            var exerciseId = exercise.attr('id');

            stompClient.subscribe('/user/queue/exercises/'+exerciseId+'/error', function (result) {
                term.val("ERROR: " + result.body); // print result to terminal
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
                var progress = pmessage['completion'];
                var message = pmessage['message'];
                $(goal.find('span.progress')).html((100*progress)+'%');
                $(goal.find('span.reason')).html(message);
            });
        });

        // connect all output listeners to goals for their exercises
        exercise.find('.js-methods').each(function(i) {
            var glist = $(this);
            var exerciseId = exercise.attr('id');
            stompClient.subscribe('/user/queue/exercises/'+exerciseId+'/methods', function (message) {
                JSON.parse(message.body).forEach(function (method) {
                        var name = method[0] + ': ' + method[1];
                        var option = glist.find('option').filter(function (i,option) { return option.text == name; })[0];
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