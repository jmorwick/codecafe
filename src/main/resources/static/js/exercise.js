
var socket = new SockJS('/codecafe-websocket');
var stompClient = Stomp.over(socket);
stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);
});


function populateExercise(exercise, stompClient) {

    // load html for exercise
    exercise.load("/exercises/"+exercise.attr('id')+"/raw", function() {

        // connect all history listeners to websockets for their exercises
        exercise.find('.js-history').each(function(i) {

        });

        // link every execute button to ajax call for their exercises
        exercise.find('.js-sendcode').on('click', function(e) {
            var sendButton = $(this);
            var exerciseId = exercise.attr('id');
            var codepad = sendButton.parent().find('textarea');
            // TODO: clear messages for this exercise
            stompClient.send("/app/"+exercise.attr('id'), {}, JSON.stringify({'code': codepad}));
            // TODO: report error on failure to send ajax message
            if(codepad.attr('clearonsend') != undefined) codepad.val('');
        });

        // connect all variable listeners to websockets for their exercises
        exercise.find('.js-variables').each(function(i) {
            var term = $(this);
            var exerciseId = exercise.attr('id');
            var callback = function(message) {
                term.val(message.data); // print message to terminal
            };

            stompClient.subscribe('/user/queue/'+exerciseId+'/variables', function (greeting) {
                var variables = JSON.parse(greeting.body);
                // TODO: sort
                // TODO: populate variable table
            });
        });

        // connect all message listeners to websockets for their exercises
        exercise.find('.js-message').each(function(i) {
            var term = $(this);
            var exerciseId = exercise.attr('id');
            var callback = function(message) {
                term.val(message.data); // print error message to terminal
            };
            // TODO: register callback
            // TODO: sort
            // TODO: make in to a table
        });

        // connect all output listeners to websockets for their exercises
        exercise.find('.js-stdout').each(function(i) {
            var term = $(this);
            var exerciseId = exercise.attr('id');
            var callback = function(message) {
                term.append(message.data); // print message to terminal
                term.scrollTop(term[0].scrollHeight - term.height());  // scroll to bottom
            };
            // TODO: register callback
            // TODO: make in to a table
        });

        // connect all output listeners to goals for their exercises
        exercise.find('.js-goals').each(function(i) {
            var goalResponses = $(this);
            var exerciseId = exercise.attr('id');
            var callback = function(message) {
                var pmessage = JSON.parse(message.data);
                var gid = pmessage[0];
                var reason = pmessage[1];
                var progress = pmessage[2];
                $(goalResponses.find('span.progress')[gid]).html((100*progress)+'%');
                $(goalResponses.find('span.reason')[gid]).html(reason);
            };
            // TODO: register callback

            // TODO: make in to a table
        });

        // connect all output listeners to goals for their exercises
        exercise.find('.js-methods').each(function(i) {
            var glist = $(this);
            var exerciseId = exercise.attr('id');
            var callback = function(message) {
                JSON.parse(message.data).forEach(function (method) {
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
            };
            // TODO: register callback
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
                // TODO: send inputfield.val()
                inputfield.val('');                     // clear input
            }
            // TODO: clear messages for this exercise
            // TODO: report error on failure to send ajax message
        });



        // link every reset button to ajax call for their exercises
        exercise.find('.js-sendreset').on('click', function(e) {
            var sendButton = $(this);
            var exerciseId = exercise.attr('id');
            var codepad = sendButton.parent().find('textarea');
            codepad.val('');
            var variables = exercise.find('js-variables');
            variables.val('');
            var history = exercise.find('js-history');
            history.val('');
            // TODO: clear methods
            // TODO: reset exercise
            // TODO: report error on failure to send ajax message
            if(codepad.attr('clearonsend') != undefined) codepad.val('');
        });
    });
};

$( document ).ready(function() {
    $('.js-exercise').each(function(i) {
        populateExercise($(this));
    })
});