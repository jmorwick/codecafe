function populateExercise(exercise) {
    // load html for exercise
    exercise.load("/exercises/"+exercise.attr('id')+"/raw", function() {

    // connect all history listeners to websockets for their exercises
    exercise.find('.js-history').each(function(i) {
        var term = $(this);
        var exerciseId = exercise.attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/history');
        socket.onmessage = function(message) {
            term.append(message.data); // print message to terminal
            term.scrollTop(term[0].scrollHeight - term.height());  // scroll to bottom
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors

        // TODO: make in to a table
    });

    // link every execute button to ajax call for their exercises
    exercise.find('.js-sendcode').on('click', function(e) {
        var sendButton = $(this);added
        var exerciseId = exercise.attr('id');
        var codepad = sendButton.parent().find('textarea');
        // TODO: clear messages for this exercise
        $.post('/exercises/'+exerciseId+'/exec', {code: codepad.val()});
        // TODO: report error on failure to send ajax message
        if(codepad.attr('clearonsend') != undefined) codepad.val('');
    });

    // connect all variable listeners to websockets for their exercises
    exercise.find('.js-variables').each(function(i) {
        var term = $(this);
        var exerciseId = exercise.attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/variables');
        socket.onmessage = function(message) {
            term.val(message.data); // print message to terminal
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors
        // TODO: sort
        // TODO: make in to a table
    });

    // connect all message listeners to websockets for their exercises
    exercise.find('.js-message').each(function(i) {
        var term = $(this);
        var exerciseId = exercise.attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/errors');
        socket.onmessage = function(message) {
            term.val(message.data); // print error message to terminal
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors
        // TODO: sort
        // TODO: make in to a table
    });

    // connect all output listeners to websockets for their exercises
    exercise.find('.js-stdout').each(function(i) {
        var term = $(this);
        var exerciseId = exercise.attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/stdout');
        socket.onmessage = function(message) {
            term.append(message.data); // print message to terminal
            term.scrollTop(term[0].scrollHeight - term.height());  // scroll to bottom
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors

        // TODO: make in to a table
    });

    // connect all output listeners to goals for their exercises
    exercise.find('.js-goals').each(function(i) {
        var goalResponses = $(this);
        var exerciseId = exercise.attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/goals');
        socket.onmessage = function(message) {
            var pmessage = JSON.parse(message.data);
            var gid = pmessage[0];
            var reason = pmessage[1];
            var progress = pmessage[2];
            $(goalResponses.find('span.progress')[gid]).html((100*progress)+'%');
            $(goalResponses.find('span.reason')[gid]).html(reason);
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors

        // TODO: make in to a table
    });

    // connect all output listeners to goals for their exercises
    exercise.find('.js-methods').each(function(i) {
        var glist = $(this);
        var exerciseId = exercise.attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/methods');
        socket.onmessage = function(message) {
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
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors
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

    // link every input field to ajax call for their exercises
    exercise.find('.js-stdin').keyup(function(e) {
        var inputfield = $(this);
        var exerciseId = exercise.attr('id');

        if(e.keyCode == 13) {  // return was pressed
            $.post('/exercises/'+exerciseId+'/stdin', {data: inputfield.val()+"\n"});
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
        $.post('/exercises/'+exerciseId+'/reset');  // reset terminal
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